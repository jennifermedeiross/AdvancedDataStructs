package br.com.project.structs.lsm.memtable;

import br.com.project.structs.lsm.types.ByteArrayPair;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import java.util.Iterator;
import static br.com.project.structs.lsm.comparator.ByteArrayComparator.compare;
import static java.lang.Math.ceil;
import static java.lang.Math.log;

/**
 * Implementação de uma Skip List, uma estrutura de dados que permite inserção,
 * remoção e busca eficientes em tempo logarítmico. Cada elemento é armazenado
 * como um par de arrays de bytes (chave e valor).
 */
public class SkipList implements Iterable<ByteArrayPair> {

    private static final int DEFAULT_CAPACITY = (int) Math.pow(2, 20);

    private final Node head;
    private final Node[] predecessors;
    private final XoRoShiRo128PlusRandom randomGenerator;

    private int maxLevels;
    private int size;

    /**
     * Cria uma Skip List com a capacidade padrão.
     */
    public SkipList() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Cria uma Skip List com uma capacidade específica, determinando o número
     * máximo de níveis com base nesse valor.
     *
     * @param capacity Capacidade estimada da estrutura.
     */
    public SkipList(int capacity) {
        this.maxLevels = (int) ceil(log(capacity) / log(2));
        this.size = 0;
        this.head = new Node(null, maxLevels);
        this.randomGenerator = new XoRoShiRo128PlusRandom();
        this.predecessors = new Node[maxLevels];
    }

    /**
     * Adiciona um novo item à Skip List. Se a chave já existir, o valor é atualizado.
     *
     * @param newItem O par chave-valor a ser inserido.
     */
    public void add(ByteArrayPair newItem) {
        Node currentNode = head;

        for (int level = maxLevels - 1; level >= 0; level--) {
            while (currentNode.next[level] != null && currentNode.next[level].val.compareTo(newItem) < 0) {
                currentNode = currentNode.next[level];
            }
            predecessors[level] = currentNode;
        }

        if (currentNode.next[0] != null && currentNode.next[0].val.compareTo(newItem) == 0) {
            currentNode.next[0].val = newItem;
            return;
        }

        Node newNode = new Node(newItem, maxLevels);
        int newNodeLevel = generateRandomLevel();

        for (int level = 0; level < newNodeLevel; level++) {
            newNode.next[level] = predecessors[level].next[level];
            predecessors[level].next[level] = newNode;
        }
        size++;
    }

    /**
     * Gera um nível aleatório para um novo nó, determinando em qual altura
     * ele será inserido na Skip List.
     *
     * @return O nível gerado aleatoriamente.
     */
    private int generateRandomLevel() {
        int level = 1;
        long randomValue = randomGenerator.nextLong();

        while (level < maxLevels && (randomValue & (long) Math.pow(2, level)) != 0) {
            level++;
        }
        return level;
    }

    /**
     * Busca um item na Skip List com base na chave fornecida.
     *
     * @param searchKey A chave do elemento a ser buscado.
     * @return O valor correspondente à chave, ou null se não encontrado.
     */
    public byte[] get(byte[] searchKey) {
        Node currentNode = head;

        for (int level = maxLevels - 1; level >= 0; level--) {
            while (currentNode.next[level] != null && compare(currentNode.next[level].val.key(), searchKey) < 0) {
                currentNode = currentNode.next[level];
            }
        }

        if (currentNode.next[0] != null && compare(currentNode.next[0].val.key(), searchKey) == 0) {
            return currentNode.next[0].val.value();
        }
        return null;
    }

    /**
     * Remove um item da Skip List com base na chave fornecida.
     *
     * @param keyToRemove A chave do elemento a ser removido.
     */
    public void remove(byte[] keyToRemove) {
        Node currentNode = head;

        for (int level = maxLevels - 1; level >= 0; level--) {
            while (currentNode.next[level] != null && compare(currentNode.next[level].val.key(), keyToRemove) < 0) {
                currentNode = currentNode.next[level];
            }
            predecessors[level] = currentNode;
        }

        if (currentNode.next[0] != null && compare(currentNode.next[0].val.key(), keyToRemove) == 0) {
            Node targetNode = currentNode.next[0];
            boolean isLastElement = targetNode.next[0] == null;

            for (int level = 0; level < maxLevels; level++) {
                if (predecessors[level].next[level] != targetNode) {
                    break;
                }
                predecessors[level].next[level] = isLastElement ? null : targetNode.next[level];
            }
            size--;
        }
    }

    /**
     * Retorna o número de elementos armazenados na Skip List.
     *
     * @return A quantidade de elementos.
     */
    public int size() {
        return size;
    }

    @Override
    public Iterator<ByteArrayPair> iterator() {
        return new SkipListIterator(head);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int level = maxLevels - 1; level >= 0; level--) {
            sb.append(String.format("Level %2d: ", level));
            Node currentNode = head;
            while (currentNode.next[level] != null) {
                sb.append(currentNode.next[level].val).append(" -> ");
                currentNode = currentNode.next[level];
            }
            sb.append("END\n");
        }
        return sb.toString();
    }

    private static final class Node {
        ByteArrayPair val;
        Node[] next;

        Node(ByteArrayPair val, int levels) {
            this.val = val;
            this.next = new Node[levels];
        }
    }

    private static class SkipListIterator implements Iterator<ByteArrayPair> {
        Node currentNode;

        SkipListIterator(Node head) {
            this.currentNode = head;
        }

        @Override
        public boolean hasNext() {
            return currentNode.next[0] != null;
        }

        @Override
        public ByteArrayPair next() {
            if (currentNode.next[0] == null) return null;
            ByteArrayPair result = currentNode.next[0].val;
            currentNode = currentNode.next[0];
            return result;
        }
    }
}
