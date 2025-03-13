package br.com.project.structs.lsm.tree;

import br.com.project.structs.lsm.memtable.Memtable;
import br.com.project.structs.lsm.sstable.SSTable;
import br.com.project.structs.lsm.types.ByteArrayPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Implementação de uma LSM Tree.
 * <p>
 * Escritas são adicionadas à Memtable, que é descarregada quando atinge um determinado tamanho.
 * SSTables são divididas em níveis, onde cada nível armazena tabelas maiores.
 * <p>
 * Quando descarregada, uma Memtable se torna uma SSTable no nível 1. Se o nível exceder um limite,
 * todas as suas tabelas são mescladas e adicionadas ao próximo nível.
 * <p>
 * Executores em segundo plano cuidam do descarregamento e da compactação.
 */
public class LSMTree {

    static final long DEFAULT_MEMTABLE_MAX_BYTE_SIZE = 1024 * 1024;
    static final int DEFAULT_LEVEL_ZERO_MAX_SIZE = 2;
    static final double LEVEL_INCR_FACTOR = 1.75;

    static final String DEFAULT_DATA_DIRECTORY = "LSM-data";

    final Object mutableMemtableLock = new Object();
    final Object immutableMemtablesLock = new Object();
    final Object tableLock = new Object();

    final long mutableMemtableMaxSize;
    final int maxLevelZeroSstNumber;
    final long maxLevelZeroSstByteSize;
    final String dataDir;

    Memtable mutableMemtable;
    LinkedList<Memtable> immutableMemtables;
    ObjectArrayList<ObjectArrayList<SSTable>> levels;

    ScheduledExecutorService memtableFlusher;
    ScheduledExecutorService tableCompactor;

    /**
     * Cria uma nova LSMTree com um tamanho padrão de memtable e diretório de dados.
     */
    public LSMTree() {
        this(DEFAULT_MEMTABLE_MAX_BYTE_SIZE, DEFAULT_LEVEL_ZERO_MAX_SIZE, DEFAULT_DATA_DIRECTORY);
    }

    /**
     * Cria uma nova LSMTree com um tamanho específico de memtable e diretório de dados.
     *
     * @param mutableMemtableMaxByteSize O tamanho máximo da memtable antes de ser descarregada para o disco.
     * @param dataDir                    O diretório onde os dados serão armazenados.
     */
    public LSMTree(long mutableMemtableMaxByteSize, int maxLevelZeroSstNumber, String dataDir) {
        this.mutableMemtableMaxSize = mutableMemtableMaxByteSize;
        this.maxLevelZeroSstNumber = maxLevelZeroSstNumber;
        this.maxLevelZeroSstByteSize = mutableMemtableMaxByteSize * 2;
        this.dataDir = dataDir;
        createDataDir();

        mutableMemtable = new Memtable();
        immutableMemtables = new LinkedList<>();
        levels = new ObjectArrayList<>();
        levels.add(new ObjectArrayList<>());

        memtableFlusher = newSingleThreadScheduledExecutor();
        memtableFlusher.scheduleAtFixedRate(this::flushMemtable, 50, 50, TimeUnit.MILLISECONDS);

        tableCompactor = newSingleThreadScheduledExecutor();
        tableCompactor.scheduleAtFixedRate(this::levelCompaction, 200, 200, TimeUnit.MILLISECONDS);
    }


    /**
     * Adiciona um item à LSMTree.
     * Se a memtable estiver cheia, ela é descarregada para o disco.
     *
     * @param item O item a ser adicionado.
     */
    public void add(ByteArrayPair item) {
        synchronized (mutableMemtableLock) {
            mutableMemtable.add(item);
            checkMemtableSize();
        }
    }

    /**
     * Remove um item da LSMTree.
     * Isso é feito adicionando uma marcação de remoção (tombstone) à memtable.
     *
     * @param key A chave do item a ser removido.
     */
    public void delete(byte[] key) {
        synchronized (mutableMemtableLock) {
            mutableMemtable.remove(key);
            checkMemtableSize();
        }
    }

    /**
     * Obtém um item da LSMTree.
     *
     * @param key A chave do item a ser buscado.
     * @return O valor do item, ou null se ele não existir.
     */
    public byte[] get(byte[] key) {
        byte[] result;

        synchronized (mutableMemtableLock) {
            if ((result = mutableMemtable.get(key)) != null)
                return result;
        }

        synchronized (immutableMemtablesLock) {
            for (Memtable memtable : immutableMemtables)
                if ((result = memtable.get(key)) != null)
                    return result;
        }

        synchronized (tableLock) {
            for (ObjectArrayList<SSTable> level : levels)
                for (SSTable table : level)
                    if ((result = table.get(key)) != null)
                        return result;
        }

        return null;
    }

    /**
     * Interrompe as threads em segundo plano.
     */
    public void stop() {
        memtableFlusher.shutdownNow();
        tableCompactor.shutdownNow();
    }

    private void checkMemtableSize() {
        if (mutableMemtable.byteSize() <= mutableMemtableMaxSize)
            return;

        synchronized (immutableMemtablesLock) {
            immutableMemtables.addFirst(mutableMemtable);
            mutableMemtable = new Memtable();
        }
    }

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

    private void levelCompaction() {
        synchronized (tableLock) {
            int n = levels.size();

            int maxLevelSize = maxLevelZeroSstNumber;
            long sstMaxSize = maxLevelZeroSstByteSize;

            for (int i = 0; i < n; i++) {
                ObjectArrayList<SSTable> level = levels.get(i);

                if (level.size() > maxLevelSize) {
                    // adiciona um novo nível, se necessário
                    if (i == n - 1)
                        levels.add(new ObjectArrayList<>());

                    // pega todas as tabelas do nível atual e do próximo nível
                    ObjectArrayList<SSTable> nextLevel = levels.get(i + 1);
                    ObjectArrayList<SSTable> merge = new ObjectArrayList<>();
                    merge.addAll(level);
                    merge.addAll(nextLevel);

                    // executa uma mesclagem ordenada e substitui o próximo nível
                    var sortedRun = SSTable.sortedRun(dataDir, sstMaxSize, merge.toArray(SSTable[]::new));

                    // exclui as tabelas anteriores
                    level.forEach(SSTable::closeAndDelete);
                    level.clear();
                    nextLevel.forEach(SSTable::closeAndDelete);
                    nextLevel.clear();

                    nextLevel.addAll(sortedRun);
                }

                maxLevelSize = (int) (maxLevelSize * LEVEL_INCR_FACTOR);
                sstMaxSize = (int) (sstMaxSize * LEVEL_INCR_FACTOR);
            }
        }
    }

    private void createDataDir() {
        try {
            Path path = Path.of(dataDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao garantir a criação do diretório de dados: " + dataDir, e);
        }
    }


    @Override
    public String toString() {

        var s = new StringBuilder();
        s.append("Árvore LSM {\n");
        s.append("\tmemtable: ");
        s.append(mutableMemtable.byteSize() / 1024.0 / 1024.0);
        s.append(" MB\n");
        s.append("\tmemtables imutáveis: ");
        s.append(immutableMemtables);
        s.append("\n\tníveis de SST:\n");

        int i = 0;
        for (var level : levels) {
            s.append(String.format("\t\t- %d: ", i));
            level.stream()
                    .map(st -> String.format("[ %s, tamanho: %d ] ", st.filename, st.size))
                    .forEach(s::append);
            s.append("\n");
            i += 1;
        }

        s.append("}");
        return s.toString();
    }
}
