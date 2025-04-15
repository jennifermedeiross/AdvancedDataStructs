package br.com.project.structs.lsm.sstable;

import br.com.project.structs.lsm.io.ExtendedInputStream;
import br.com.project.structs.lsm.io.ExtendedOutputStream;
import br.com.project.structs.lsm.types.ByteArrayPair;
import br.com.project.structs.lsm.types.ByteArrayWrapper;
import br.com.project.structs.lsm.utils.IteratorMerger;
import br.com.project.structs.lsm.utils.UniqueSortedIterator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A classe SSTable representa uma tabela de busca otimizada para armazenar
 * pares chave-valor em disco. Ela inclui operações para leitura, escrita,
 * gerenciamento de índices e filtro de Bloom para otimização de buscas.
 */
public class SSTable implements Iterable<ByteArrayPair> {

    public static final String DATA_FILE_EXTENSION = ".data";
    public static final String BLOOM_FILE_EXTENSION = ".bloom";
    public static final String INDEX_FILE_EXTENSION = ".index";

    private static final int DEFAULT_SAMPLE_SIZE = 1000;
    static final AtomicLong SST_COUNTER = new AtomicLong();
    LongArrayList sparseOffsets;
    IntArrayList sparseSizeCount;
    ObjectArrayList<byte[]> sparseKeys;

    BloomFilter bloomFilter;

    public String filename;
    ExtendedInputStream is;
    public int size;

    ByteArrayWrapper minKey;
    ByteArrayWrapper maxKey;

    /**
     * Cria uma nova SSTable a partir de um iterável de itens.
     *
     * @param directory  O diretório onde a SSTable será salva.
     * @param items      Os itens a serem escritos na SSTable (deve estar ordenado).
     * @param sampleSize O número de itens a serem pulados entre entradas no índice esparso.
     */
    public SSTable(String directory, Iterator<ByteArrayPair> items, int sampleSize) {
        this(getNextSstFilename(directory), items, sampleSize, 1024 * 1024 * 256);
    }

    /**
     * Cria uma nova SSTable a partir de um iterável de itens com amostragem padrão.
     *
     * @param directory O diretório onde a SSTable será salva.
     * @param items     Os itens a serem escritos na SSTable (deve estar ordenado).
     */
    public SSTable(String directory, Iterator<ByteArrayPair> items) {
        this(getNextSstFilename(directory), items, DEFAULT_SAMPLE_SIZE, 1024 * 1024 * 256);
    }

    /**
     * Cria uma nova SSTable a partir de um iterável de itens com um tamanho máximo de arquivo especificado.
     *
     * @param directory   O diretório onde a SSTable será salva.
     * @param items       Os itens a serem escritos na SSTable (deve estar ordenado).
     * @param maxByteSize O tamanho máximo do arquivo SSTable.
     */
    public SSTable(String directory, Iterator<ByteArrayPair> items, long maxByteSize) {
        this(getNextSstFilename(directory), items, DEFAULT_SAMPLE_SIZE, maxByteSize);
    }

    /**
     * Cria uma nova SSTable a partir de um iterável de itens com parâmetros especificados.
     *
     * @param filename    O nome do arquivo SSTable.
     * @param items       Os itens a serem escritos na SSTable (deve estar ordenado).
     * @param sampleSize  O número de itens a serem pulados entre entradas no índice esparso.
     * @param maxByteSize O tamanho máximo do arquivo SSTable.
     */
    public SSTable(String filename, Iterator<ByteArrayPair> items, int sampleSize, long maxByteSize) {
        this.filename = filename;
        writeItems(filename, items, sampleSize, maxByteSize);
        is = new ExtendedInputStream(filename + DATA_FILE_EXTENSION);
    }

    /**
     * Inicializa uma SSTable a partir de um arquivo existente no disco.
     *
     * @param filename O nome do arquivo base da SSTable.
     */
    public SSTable(String filename) {
        this.filename = filename;
        initializeFromDisk(filename);
    }

    /**
     * Combina várias SSTables ordenadas numa nova lista de SSTables.
     *
     * @param dataDir    O diretório onde as novas SSTables serão armazenadas.
     * @param sstMaxSize O tamanho máximo de cada SSTable.
     * @param tables     As SSTables a serem combinadas.
     * @return Uma lista de SSTables ordenadas.
     */
    public static ObjectArrayList<SSTable> sortedRun(String dataDir, long sstMaxSize, SSTable... tables) {
        SSTableIterator[] itArray = Arrays.stream(tables).map(SSTable::iterator).toArray(SSTableIterator[]::new);

        IteratorMerger<ByteArrayPair> merger = new IteratorMerger<>(itArray);
        UniqueSortedIterator<ByteArrayPair> uniqueSortedIterator = new UniqueSortedIterator<>(merger);

        ObjectArrayList<SSTable> res = new ObjectArrayList<>();

        while (uniqueSortedIterator.hasNext()) {
            res.add(new SSTable(getNextSstFilename(dataDir), uniqueSortedIterator, DEFAULT_SAMPLE_SIZE, sstMaxSize));
        }

        return res;
    }

    /**
     * Lê um item da SSTable pelo valor da chave.
     *
     * @param key A chave do item a ser lido.
     * @return O valor associado à chave, ou null se a chave não for encontrada.
     */
    public byte[] get(byte[] key) {
        ByteArrayWrapper keyWrapper = new ByteArrayWrapper(key);
        if (minKey.compareTo(keyWrapper) < 0 ||
                maxKey.compareTo(keyWrapper) > 0 ||
                !bloomFilter.mightContain(key))
            return null;

        int offsetIndex = getCandidateOffsetIndex(key);
        long offset = sparseOffsets.getLong(offsetIndex);
        int remaining = size - sparseSizeCount.getInt(offsetIndex);
        is.seek(offset);

        int cmp = 1;
        int searchKeyLen = key.length, readKeyLen, readValueLen;

        byte[] readKey;

        while (cmp > 0 && remaining > 0) {

            remaining--;
            readKeyLen = is.readVByteInt();

            // passou demais
            if (readKeyLen > searchKeyLen) {
                return null;
            }

            // ficou muito curto
            if (readKeyLen < searchKeyLen) {
                readValueLen = is.readVByteInt();
                is.skip(readKeyLen + readValueLen);
                continue;
            }

            // lê a chave completa, compara, se for igual, lê o valor
            readValueLen = is.readVByteInt();
            readKey = is.readNBytes(readKeyLen);
            cmp = compare(key, readKey);

            if (cmp == 0) {
                return is.readNBytes(readValueLen);
            } else {
                is.skip(readValueLen);
            }
        }

        return null;
    }

    /**
     * Retorna um iterador sobre os itens da SSTable.
     *
     * @return Iterador da tabela.
     */
    public Iterator<ByteArrayPair> iterator() {
        is.seek(0);
        return new SSTableIterator(this);
    }

    /**
     * Fecha o fluxo de entrada da SSTable.
     */
    public void close() {
        is.close();
    }

    /**
     * Exclui os arquivos relacionados à SSTable do disco.
     */
    public void deleteFiles() {
        for (var extension : List.of(DATA_FILE_EXTENSION, INDEX_FILE_EXTENSION, BLOOM_FILE_EXTENSION))
            new File(filename + extension).delete();
    }

    /**
     * Fecha o fluxo e exclui os arquivos da SSTable.
     */
    public void closeAndDelete() {
        close();
        deleteFiles();
    }

    private static String getNextSstFilename(String directory) {
        return String.format("%s/sst_%d", directory, SST_COUNTER.incrementAndGet());
    }

    /**
     * Inicializa a SSTable carregando os seus metadados e estruturas auxiliares a partir do disco.
     * Abre os arquivos associados à SSTable (.data, .index e .bloom) e reconstrói:
     * - o fluxo de entrada para leitura dos dados
     * - os offsets esparsos e tamanhos acumulados a partir do índice
     * - as chaves associadas aos pontos de amostragem
     * - o filtro de Bloom utilizado para consultas rápidas de existência de chave
     * O arquivo .index armazena o número total de elementos, seguido pelas diferenças de offset e contagem
     * relativas aos pontos de amostragem. Esses dados são reconstruídos somando cumulativamente os valores.
     * Por fim, o filtro de Bloom é carregado do arquivo correspondente.
     *
     * @param filename caminho base dos arquivos da SSTable (sem extensão)
     */
    private void initializeFromDisk(String filename) {
        // arquivo de itens
        is = new ExtendedInputStream(filename + DATA_FILE_EXTENSION);

        // índice esparso
        sparseOffsets = new LongArrayList();
        sparseSizeCount = new IntArrayList();
        sparseKeys = new ObjectArrayList<>();

        ExtendedInputStream indexIs = new ExtendedInputStream(filename + INDEX_FILE_EXTENSION);
        size = indexIs.readVByteInt();

        int sparseSize = indexIs.readVByteInt();
        long offsetsCumulative = 0L;
        sparseOffsets.add(offsetsCumulative);
        for (int i = 0; i < sparseSize - 1; i++) {
            offsetsCumulative += indexIs.readVByteLong();
            sparseOffsets.add(offsetsCumulative);
        }

        int sizeCumulative = 0;
        sparseSizeCount.add(sizeCumulative);
        for (int i = 0; i < sparseSize - 1; i++) {
            sizeCumulative += indexIs.readVByteInt();
            sparseSizeCount.add(sizeCumulative);
        }

        for (int i = 0; i < sparseSize; i++)
            sparseKeys.add(indexIs.readNBytes(indexIs.readVByteInt()));

        is.close();

        // filtro de bloom
        bloomFilter = BloomFilter.readFromFile(filename + BLOOM_FILE_EXTENSION);
    }

    private int getCandidateOffsetIndex(byte[] key) {
        int low = 0;
        int high = sparseOffsets.size() - 1;

        while (low < (high - 1)) {
            int mid = (low + high) / 2;
            int cmp = compare(key, sparseKeys.get(mid));

            if (cmp < 0)
                high = mid - 1;
            else if (cmp > 0)
                low = mid;
            else
                return mid;
        }
        return low;
    }

    private int compare(byte[] b1, byte[] b2) {
        return new ByteArrayWrapper(b1).compareTo(new ByteArrayWrapper(b2));
    }

    /**
     * Escreve os pares chave-valor no disco, gerando os arquivos .data, .bloom e .index.
     * EPercorre os itens do iterador e grava os dados no arquivo .data enquanto mantém:
     * - um filtro de Bloom com todas as chaves
     * - amostras dos offsets e posições para formar um índice esparso
     * A cada 'sampleSize' elementos, registra a chave, o offset e a contagem atual, que
     * são utilizados na criação do arquivo .index.
     * Após o término, também grava o filtro de Bloom no arquivo correspondente.
     * Se o iterador estiver vazio, uma exceção é lançada para evitar a criação de uma SSTable inválida.
     *
     * @param filename    caminho base para os arquivos a serem criados (sem extensão)
     * @param items       iterador dos pares chave-valor a serem gravados
     * @param sampleSize  intervalo de amostragem para gerar o índice esparso
     * @param maxByteSize limite máximo de bytes que podem ser escritos no arquivo .data
     */
    private void writeItems(String filename, Iterator<ByteArrayPair> items, int sampleSize, long maxByteSize) {
        initializeIndexStructures();

        int numElements = writeDataFile(filename, items, sampleSize, maxByteSize);

        if (numElements == 0) {
            throw new IllegalArgumentException("Tentativa de criar uma SSTable a partir de um iterador vazio");
        }

        this.size = numElements;

        writeBloomFilter(filename);
        writeIndexFile(filename, numElements);
    }

    private void initializeIndexStructures() {
        sparseOffsets = new LongArrayList();
        sparseSizeCount = new IntArrayList();
        sparseKeys = new ObjectArrayList<>();
        bloomFilter = new BloomFilter();
    }

    private int writeDataFile(String filename, Iterator<ByteArrayPair> items, int sampleSize, long maxByteSize) {
        ExtendedOutputStream ios = new ExtendedOutputStream(filename + DATA_FILE_EXTENSION);

        int numElements = 0;
        long offset = 0L;
        long byteSize = 0L;

        while (items.hasNext() && byteSize < maxByteSize) {
            ByteArrayPair item = items.next();

            if (minKey == null)
                minKey = item.getKey();
            maxKey = item.getKey();

            if (numElements % sampleSize == 0) {
                sparseOffsets.add(offset);
                sparseSizeCount.add(numElements);
                sparseKeys.add(item.key());
            }

            bloomFilter.add(item.key());

            offset += ios.writeByteArrayPair(item);
            byteSize += item.size();
            numElements++;
        }

        ios.close();
        return numElements;
    }

    private void writeBloomFilter(String filename) {
        bloomFilter.writeToFile(filename + BLOOM_FILE_EXTENSION);
    }

    private void writeIndexFile(String filename, int numElements) {
        ExtendedOutputStream indexOs = new ExtendedOutputStream(filename + INDEX_FILE_EXTENSION);

        indexOs.writeVByteInt(numElements);
        int sparseSize = sparseOffsets.size();
        indexOs.writeVByteInt(sparseSize);

        long prevOffset = 0L;
        for (int i = 1; i < sparseSize; i++) {
            indexOs.writeVByteLong(sparseOffsets.getLong(i) - prevOffset);
            prevOffset = sparseOffsets.getLong(i);
        }

        int prevSize = 0;
        for (int i = 1; i < sparseSize; i++) {
            indexOs.writeVByteInt(sparseSizeCount.getInt(i) - prevSize);
            prevSize = sparseSizeCount.getInt(i);
        }

        for (byte[] key : sparseKeys) {
            indexOs.writeVByteInt(key.length);
            indexOs.write(key);
        }

        indexOs.close();
    }

    /**
     * Iterador para percorrer os pares chave-valor na SSTable.
     */
    private static class SSTableIterator implements Iterator<ByteArrayPair> {

        private final SSTable table;
        int remaining;

        public SSTableIterator(SSTable table) {
            this.table = table;
            remaining = table.size;
        }

        @Override
        public boolean hasNext() {
            return remaining > 0;
        }

        @Override
        public ByteArrayPair next() {
            remaining--;

            return table.is.readBytePair();
        }

    }

}