package br.com.project.structs.lsm.memtable;

import br.com.project.structs.lsm.types.ByteArrayPair;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

import java.util.Iterator;

import static br.com.project.structs.lsm.comparator.ByteArrayComparator.compare;
import static java.lang.Math.ceil;
import static java.lang.Math.log;

/**
 * Implementação de uma Skip List para armazenar pares chave-valor do tipo ByteArrayPair.
 * Essa estrutura permite buscas, inserções e remoções eficientes, mantendo os dados ordenados.
 */
public class SkipList implements Iterable<ByteArrayPair> {

    static final int DEFAULT_ELEMENTS = 1 << 20;

    final Node sentinel;

    private final Node[] buffer;
    private final XoRoShiRo128PlusRandom rn;

    int levels;
    int size;

    /**
     * Cria uma Skip List com um número padrão de elementos (2^20).
     */
    public SkipList() {
        this(DEFAULT_ELEMENTS);
    }

    /**
     * Cria uma Skip List dimensionada para um número específico de elementos.
     *
     * @param numElements Número estimado de elementos a serem armazenados.
     */
    public SkipList(int numElements) {
        levels = (int) ceil(log(numElements) / log(2)); // Define o número de níveis com base no número de elementos
        size = 0;
        sentinel = new Node(null, levels);
        rn = new XoRoShiRo128PlusRandom();
        buffer = new Node[levels];
    }

    /**
     * Adiciona um item à Skip List.
     *
     * @param item O par chave-valor a ser adicionado.
     */
    public void add(ByteArrayPair item) {
        Node current = sentinel;
        for (int i = levels - 1; i >= 0; i--) {
            while (current.next[i] != null && current.next[i].val.compareTo(item) < 0)
                current = current.next[i];
            buffer[i] = current;
        }

        // Se a chave já existe, apenas atualiza o valor
        if (current.next[0] != null && current.next[0].val.compareTo(item) == 0) {
            current.next[0].val = item;
            return;
        }

        // Criação de um novo nó e atualização dos ponteiros nos níveis adequados
        Node newNode = new Node(item, levels);
        for (int i = 0; i < randomLevel(); i++) {
            newNode.next[i] = buffer[i].next[i];
            buffer[i].next[i] = newNode;
        }
        size++;
    }

    /**
     * Gera aleatoriamente o nível de um novo nó, com base em um gerador de números pseudoaleatórios.
     *
     * @return O nível gerado para o novo nó.
     */
    private int randomLevel() {
        int level = 1;
        long n = rn.nextLong();
        while (level < levels && (n & (1L << level)) != 0)
            level++;
        return level;
    }

    /**
     * Busca um item na Skip List a partir da chave fornecida.
     *
     * @param key A chave do item a ser recuperado.
     * @return O valor associado à chave ou null se não encontrado.
     */
    public byte[] get(byte[] key) {
        Node current = sentinel;
        for (int i = levels - 1; i >= 0; i--) {
            while (current.next[i] != null && compare(current.next[i].val.key(), key) < 0)
                current = current.next[i];
        }

        if (current.next[0] != null && compare(current.next[0].val.key(), key) == 0)
            return current.next[0].val.value();

        return null;
    }

    /**
     * Remove um item da Skip List a partir da chave fornecida.
     *
     * @param key A chave do item a ser removido.
     */
    public void remove(byte[] key) {
        Node current = sentinel;
        for (int i = levels - 1; i >= 0; i--) {
            while (current.next[i] != null && compare(current.next[i].val.key(), key) < 0)
                current = current.next[i];
            buffer[i] = current;
        }

        if (current.next[0] != null && compare(current.next[0].val.key(), key) == 0) {
            boolean last = current.next[0].next[0] == null;
            for (int i = 0; i < levels; i++) {
                if (buffer[i].next[i] != current.next[0])
                    break;
                buffer[i].next[i] = last ? null : current.next[0].next[i];
            }
            size--;
        }
    }

    /**
     * Retorna a quantidade de itens na Skip List.
     *
     * @return O número de elementos armazenados.
     */
    public int size() {
        return size;
    }

    /**
     * Retorna um iterador sobre os elementos armazenados na Skip List, percorrendo o nível mais baixo.
     *
     * @return Um iterador para os itens armazenados.
     */
    @Override
    public Iterator<ByteArrayPair> iterator() {
        return new SkipListIterator(sentinel);
    }

    /**
     * Retorna uma representação em string da Skip List, exibindo os níveis e os nós armazenados.
     *
     * @return Uma string representando a estrutura da Skip List.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = levels - 1; i >= 0; i--) {
            sb.append(String.format("Nível %2d: ", i));
            Node current = sentinel;
            while (current.next[i] != null) {
                sb.append(current.next[i].val).append(" -> ");
                current = current.next[i];
            }
            sb.append("FIM\n");
        }
        return sb.toString();
    }

    /**
     * Classe interna representando um nó da Skip List.
     */
    private static final class Node {
        ByteArrayPair val; // Par chave-valor armazenado no nó
        Node[] next; // Array de ponteiros para os próximos nós em cada nível

        Node(ByteArrayPair val, int numLevels) {
            this.val = val;
            this.next = new Node[numLevels];
        }
    }

    /**
     * Classe interna que implementa um iterador sobre os elementos da Skip List.
     */
    private static class SkipListIterator implements Iterator<ByteArrayPair> {
        Node node;

        SkipListIterator(Node node) {
            this.node = node;
        }

        @Override
        public boolean hasNext() {
            return node.next[0] != null;
        }

        @Override
        public ByteArrayPair next() {
            if (node.next[0] == null)
                return null;

            ByteArrayPair res = node.next[0].val;
            node = node.next[0];

            return res;
        }
    }
}
