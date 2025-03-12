package br.com.project.structs.lsm.memtable;

import br.com.project.structs.lsm.comparator.ByteArrayComparator;
import br.com.project.structs.lsm.types.ByteArrayPair;
import br.com.project.structs.lsm.utils.UniqueSortedIterator;

import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Memtable é a estrutura de dados que armazena temporariamente os pares chave-valor
 * na memória antes de serem persistidos em disco como SSTables.
 * Utiliza um ConcurrentSkipListMap para armazenar os dados de forma ordenada.
 */
public class Memtable implements Iterable<ByteArrayPair> {

    private final ConcurrentSkipListMap<byte[], byte[]> map;
    private long byteSize;

    /**
     * Inicializa uma Memtable com um ConcurrentSkipListMap vazio.
     */
    public Memtable() {
        map = new ConcurrentSkipListMap<>(ByteArrayComparator::compare); // Comparação de bytes
        byteSize = 0L;
    }

    /**
     * Adiciona um item à Memtable.
     *
     * @param item O par chave-valor a ser armazenado.
     */
    public void add(ByteArrayPair item) {
        map.put(item.key(), item.value());
        byteSize += item.size();
    }

    /**
     * Recupera um item armazenado na Memtable a partir da chave fornecida.
     *
     * @param key A chave do elemento desejado.
     * @return O valor correspondente à chave ou null caso não exista.
     */
    public byte[] get(byte[] key) {
        return map.get(key);
    }

    /**
     * Remove um elemento da Memtable, inserindo um "tombstone" para indicar a exclusão lógica.
     *
     * @param key A chave do elemento a ser removido.
     */
    public void remove(byte[] key) {
        map.remove(key);
    }

    /**
     * Retorna o tamanho da Memtable em bytes.
     *
     * @return O tamanho total em bytes da lista subjacente.
     */
    public long byteSize() {
        return byteSize;
    }

    /**
     * Retorna um iterador sobre os elementos da Memtable, eliminando duplicatas.
     *
     * @return Um iterador modificado da lista subjacente.
     */
    @Override
    public Iterator<ByteArrayPair> iterator() {
        return new UniqueSortedIterator<>(map.entrySet().stream()
                .map(e -> new ByteArrayPair(e.getKey(), e.getValue()))
                .iterator());
    }
}
