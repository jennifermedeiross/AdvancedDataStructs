package br.com.project.structs.lsm.memtable;

import br.com.project.structs.lsm.types.ByteArrayPair;
import br.com.project.structs.lsm.types.ByteArrayWrapper;
import br.com.project.structs.lsm.utils.UniqueSortedIterator;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A Memtable é uma estrutura de dados que mantém pares chave-valor em memória.
 * Utiliza uma AVL para armazenar os dados de forma ordenada e eficiente.
 * A Memtable é a primeira camada de escrita na arquitetura LSM-Tree,
 * acumulando dados antes de serem persistidos em disco.
 */
public class Memtable implements Iterable<ByteArrayPair> {

    AVLTree<ByteArrayWrapper, ByteArrayPair> tree;
    long byteSize;

    /**
     * Inicializa uma Memtable com tamanho padrão para a lista subjacente.
     */
    public Memtable() {
        tree = new AVLTree<>(ByteArrayPair::getKey);
        byteSize = 0L;
    }

    /**
     * Adiciona um item à lista subjacente.
     *
     * @param item o item a ser adicionado.
     */
    public void add(ByteArrayPair item) {
        tree.add(item);
        byteSize += item.size();
    }

    /**
     * Recupera um item da lista subjacente com base na chave fornecida.
     *
     * @param key a chave do elemento desejado.
     * @return o valor do elemento encontrado ou null se não existir.
     */
    public byte[] get(byte[] key) {
        ByteArrayPair pair = tree.get(new ByteArrayWrapper(key));
        return (pair != null) ? pair.value() : null;
    }

    /**
     * Remove um elemento da lista ao inserir um tombstone (new byte[]{}).
     * Um tombstone indica que a chave foi removida e será tratada posteriormente na compactação.
     *
     * @param key a chave do elemento a ser removido.
     */
    public void remove(byte[] key) {
        tree.add(new ByteArrayPair(key, new byte[]{}));
    }

    /**
     * Retorna o tamanho total em bytes da AVL.
     *
     * @return o tamanho em bytes da lista subjacente.
     */
    public long byteSize() {
        return byteSize;
    }

    /**
     * Retorna um iterador que descarta elementos duplicados para percorrer todos os dados da Memtable,
     * sem repetir chaves
     *
     * @return um iterador modificado da lista subjacente.
     */
    @Override
    public Iterator<ByteArrayPair> iterator() {
        return new UniqueSortedIterator<>(tree.iterator());
    }
}
