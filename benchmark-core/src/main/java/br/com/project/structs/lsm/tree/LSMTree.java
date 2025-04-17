package br.com.project.structs.lsm.tree;

import br.com.project.structs.lsm.serialization.ObjectSerializer;
import br.com.project.structs.lsm.memtable.Memtable;
import br.com.project.structs.lsm.sstable.SSTable;
import br.com.project.structs.lsm.types.ByteArrayPair;
import com.fasterxml.jackson.core.JsonProcessingException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Implementação de uma LSM Tree (Log-Structured Merge Tree).
 * As operações de escrita são adicionadas à Memtable, descarregada para o disco quando um tamanho máximo é atingido.
 * As SSTables são organizadas em níveis, com cada nível armazenando tabelas maiores.
 * Quando uma Memtable é descarregada, ela torna-se uma SSTable no nível 1. Quando o número de tabelas de um nível
 * excede um limite, as tabelas desse nível são mescladas e movidas para o próximo nível.
 * Execuções em segundo plano são responsáveis pelo descarregamento da Memtable e pela compactação das SSTables.
 */
public class LSMTree<K, V> {

    static final long DEFAULT_MEMTABLE_MAX_BYTE_SIZE = 1024 * 1024 * 32;
    static final int DEFAULT_LEVEL_ZERO_MAX_SIZE = 2;
    static final double LEVEL_INCR_FACTOR = 1.75;

    final Object mutableMemtableLock = new Object();
    final Object immutableMemtablesLock = new Object();
    final Object tableLock = new Object();

    final long mutableMemtableMaxSize;
    final int maxLevelZeroSstNumber;
    final long maxLevelZeroSstByteSize;
    final double levelIncrFactor;
    public final String dataDir;

    Memtable mutableMemtable;
    LinkedList<Memtable> immutableMemtables;
    ObjectArrayList<ObjectArrayList<SSTable>> levels;

    ScheduledExecutorService memtableFlusher;
    ScheduledExecutorService tableCompactor;

    /**
     * Cria uma LSMTree com o tamanho de Memtable padrão e diretório de dados.
     */
    public LSMTree() {
        this(DEFAULT_MEMTABLE_MAX_BYTE_SIZE, DEFAULT_LEVEL_ZERO_MAX_SIZE, LEVEL_INCR_FACTOR);
    }

    /**
     * Construtor auxiliar que define um diretório padrão para armazenar os dados da SSTable.
     *
     * @param mutableMemtableMaxByteSize O tamanho máximo da Memtable antes de ser descarregada para o disco.
     * @param maxLevelZeroSstNumber      O número máximo de SSTables no nível zero.
     * @param levelGrowthFactor          Fator de crescimento para o tamanho dos níveis superiores.
     */
    public LSMTree(long mutableMemtableMaxByteSize, int maxLevelZeroSstNumber, double levelGrowthFactor) {
        this(Paths.get(System.getProperty("user.dir"),"benchmark-core", "src", "main", "java", "br", "com", "project", "structs", "lsm", "sstable", "data").toString(),
                mutableMemtableMaxByteSize,
                maxLevelZeroSstNumber,
                levelGrowthFactor);
    }

    /**
     * Construtor principal da LSMTree, que permite configurar o diretório de dados, o tamanho máximo da Memtable
     * e os parâmetros de controle das SSTables.
     *
     * @param dataDir                    Caminho onde os arquivos da SSTable serão salvos.
     * @param mutableMemtableMaxByteSize Tamanho máximo da Memtable (em bytes) antes de ser "flushada" para disco.
     * @param maxLevelZeroSstNumber     Número máximo de SSTables permitidas no nível zero antes de disparar uma compactação.
     * @param levelGrowthFactor         Fator de crescimento para calcular o tamanho dos níveis seguintes da LSMTree.
     */
    public LSMTree(String dataDir, long mutableMemtableMaxByteSize, int maxLevelZeroSstNumber, double levelGrowthFactor) {
        this(dataDir, mutableMemtableMaxByteSize, maxLevelZeroSstNumber, levelGrowthFactor, 50, 200);
    }


    /**
     * Construtor da LSMTree que permite configurar os delays do flush e da compactação.
     *
     * @param dataDir                    Caminho onde os arquivos da SSTable serão salvos.
     * @param mutableMemtableMaxByteSize Tamanho máximo da Memtable (em bytes) antes de ser "flushada" para disco.
     * @param maxLevelZeroSstNumber     Número máximo de SSTables permitidas no nível zero antes de disparar uma compactação.
     * @param levelGrowthFactor         Fator de crescimento para calcular o tamanho dos níveis seguintes da LSMTree.
     * @param flushDelayMillis          Intervalo (em milissegundos) entre execuções do flush da Memtable.
     * @param compactionDelayMillis     Intervalo (em milissegundos) entre execuções da compactação de níveis.
     */
    public LSMTree(String dataDir,
                   long mutableMemtableMaxByteSize,
                   int maxLevelZeroSstNumber,
                   double levelGrowthFactor,
                   long flushDelayMillis,
                   long compactionDelayMillis) {

        this.mutableMemtableMaxSize = mutableMemtableMaxByteSize;
        this.maxLevelZeroSstNumber = maxLevelZeroSstNumber;
        this.maxLevelZeroSstByteSize = mutableMemtableMaxByteSize * 2;
        this.levelIncrFactor = levelGrowthFactor;
        this.dataDir = dataDir;

        createDataDir();
        initMemtables();
        initLevels();

        memtableFlusher = newSingleThreadScheduledExecutor();
        memtableFlusher.scheduleAtFixedRate(this::flushMemtable, flushDelayMillis, flushDelayMillis, TimeUnit.MILLISECONDS);

        tableCompactor = newSingleThreadScheduledExecutor();
        tableCompactor.scheduleAtFixedRate(this::levelCompaction, compactionDelayMillis, compactionDelayMillis, TimeUnit.MILLISECONDS);
    }

    private void initMemtables() {
        this.mutableMemtable = new Memtable();
        this.immutableMemtables = new LinkedList<>();
    }

    private void initLevels() {
        this.levels = new ObjectArrayList<>();
        this.levels.add(new ObjectArrayList<>()); // Nível 0
    }

    private byte[] conversorToByte(Object o) throws JsonProcessingException {
        return ObjectSerializer.convertToBytes(o);
    }

    /**
     * Adiciona um ‘item’ à LSMTree. Se a Memtable estiver cheia, ela será descarregada no disco.
     *
     * @param key   O a chave a ser adicionada.
     * @param value o valor a ser adicionado.
     */
    public void add(K key, V value) throws JsonProcessingException {
        synchronized (mutableMemtableLock) {
            mutableMemtable.add(new ByteArrayPair(conversorToByte(key), conversorToByte(value)));
            checkMemtableSize();
        }
    }

    /**
     * Remove um ‘item’ da LSMTree. Isso é feito adicionando um "tombstone" (registro de exclusão) à Memtable.
     *
     * @param key A chave do ‘item’ a ser removido.
     */
    public void delete(K key) throws JsonProcessingException {
        synchronized (mutableMemtableLock) {
            mutableMemtable.remove(conversorToByte(key));
            checkMemtableSize();
        }
    }

    /**
     * Obtém um item da LSMTree.
     *
     * @param key A chave do item a ser obtido.
     * @return O valor do item, ou null se o item não existir.
     */
    public byte[] get(K key) throws JsonProcessingException {
        byte[] result;
        byte[] keyBytes = conversorToByte(key);

        synchronized (mutableMemtableLock) {
            result = mutableMemtable.get(keyBytes);
            if (result != null) {
                return result.length == 0 ? null : result;
            }
        }

        synchronized (immutableMemtablesLock) {
            for (Memtable memtable : immutableMemtables) {
                result = memtable.get(keyBytes);
                if (result != null) {
                    return result.length == 0 ? null : result;
                }
            }
        }

        synchronized (tableLock) {
            for (ObjectArrayList<SSTable> level : levels) {
                for (SSTable table : level) {
                    result = table.get(keyBytes);
                    if (result != null) {
                        return result.length == 0 ? null : result;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Interrompe os executores em segundo plano responsáveis pelo descarregamento da Memtable e compactação das SSTables.
     */
    public void stop() {
        memtableFlusher.shutdownNow();
        tableCompactor.shutdownNow();
    }

    /**
     * Verifica se a Memtable atingiu o tamanho máximo. Caso tenha atingido, ela é transferida para a lista de Memtables imutáveis
     * e uma nova Memtable mutável é criada.
     */
    private void checkMemtableSize() {
        if (mutableMemtable.byteSize() <= mutableMemtableMaxSize)
            return;

        synchronized (immutableMemtablesLock) {
            immutableMemtables.addFirst(mutableMemtable);
            mutableMemtable = new Memtable();
        }
    }

    /**
     * Descarrega a última Memtable imutável para o disco como uma nova SSTable.
     */
    private void flushMemtable() {
        Memtable memtableToFlush;
        synchronized (immutableMemtablesLock) {
            if (immutableMemtables.isEmpty())
                return;

            memtableToFlush = immutableMemtables.getLast();
        }

        SSTable table = new SSTable(dataDir, memtableToFlush.iterator(), mutableMemtableMaxSize * 2);

        synchronized (tableLock) {
            levels.get(0).add(0, table);
        }

        synchronized (immutableMemtablesLock) {
            immutableMemtables.removeLast();
        }
    }

    /**
     * Realiza a compactação das SSTables nos diferentes níveis, mesclando as SSTables entre os níveis
     * e substituindo as tabelas mais antigas.
     */
    private void levelCompaction() {
        synchronized (tableLock) {
            int n = levels.size();

            int maxLevelSize = maxLevelZeroSstNumber;
            long sstMaxSize = maxLevelZeroSstByteSize;

            for (int i = 0; i < n; i++) {
                ObjectArrayList<SSTable> level = levels.get(i);

                if (level.size() > maxLevelSize) {
                    // Adiciona um novo nível se necessário
                    if (i == n - 1)
                        levels.add(new ObjectArrayList<>());

                    // Pega todas as tabelas do nível atual e do próximo
                    ObjectArrayList<SSTable> nextLevel = levels.get(i + 1);
                    ObjectArrayList<SSTable> merge = new ObjectArrayList<>();
                    merge.addAll(level);
                    merge.addAll(nextLevel);

                    // Realiza uma execução ordenada e substitui o próximo nível
                    var sortedRun = SSTable.sortedRun(dataDir, sstMaxSize, merge.toArray(SSTable[]::new));

                    // Exclui as tabelas anteriores
                    level.forEach(SSTable::closeAndDelete);
                    level.clear();
                    nextLevel.forEach(SSTable::closeAndDelete);
                    nextLevel.clear();

                    nextLevel.addAll(sortedRun);
                }

                maxLevelSize = (int) (maxLevelSize * levelIncrFactor);
                sstMaxSize = (int) (sstMaxSize * levelIncrFactor);
            }
        }
    }


    /**
     * Cria o diretório onde os dados serão armazenados, caso não exista.
     * Se existir, exclui.
     */
    private void createDataDir() {
        try {
            Path path = Paths.get(dataDir);
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted((p1, p2) -> p2.compareTo(p1))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna uma representação em ‘string’ do estado atual da LSMTree.
     *
     * @return A ‘string’ representando o estado da LSMTree.
     */
    @Override
    public String toString() {

        var s = new StringBuilder();
        s.append("LSM-Tree {\n");
        s.append("\tmemtable: ");
        s.append(mutableMemtable.byteSize() / 1024.0 / 1024.0);
        s.append(" mb\n");
        s.append("\tquantity immutable memtables: ");
        s.append(immutableMemtables.size());
        s.append("\n\tsst levels:\n");

        int i = 0;
        for (var level : levels) {
            s.append(String.format("\t\t-> %d: ", i));
            level.stream()
                    .map(st -> String.format("[ %s, size: %d ] ", st.filename, st.size))
                    .forEach(s::append);
            s.append("\n");
            i += 1;
        }

        s.append("}");
        return s.toString();
    }
}