package br.com.project.structs.lsm.tree;

import br.com.project.structs.lsm.memtable.Memtable;
import br.com.project.structs.lsm.sstable.SSTable;
import br.com.project.structs.lsm.types.ByteArrayPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Implementação de uma LSM Tree (Log-Structured Merge Tree).
 * <p>
 * As operações de escrita são adicionadas à Memtable, que é descarregada para o disco quando um tamanho máximo é atingido.
 * As SSTables são organizadas em níveis, com cada nível armazenando tabelas maiores.
 * <p>
 * Quando uma Memtable é descarregada, ela se torna uma SSTable no nível 1. Quando o número de tabelas de um nível
 * excede um limite, as tabelas desse nível são mescladas e movidas para o próximo nível.
 * <p>
 * Execuções em segundo plano são responsáveis pelo descarregamento da Memtable e pela compactação das SSTables.
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
    public final String dataDir;

    Memtable mutableMemtable;
    LinkedList<Memtable> immutableMemtables;
    ObjectArrayList<ObjectArrayList<SSTable>> levels;

    ScheduledExecutorService memtableFlusher;
    ScheduledExecutorService tableCompactor;

    /**
     * Cria uma nova LSMTree com o tamanho de Memtable padrão e diretório de dados.
     */
    public LSMTree() {
        this(DEFAULT_MEMTABLE_MAX_BYTE_SIZE, DEFAULT_LEVEL_ZERO_MAX_SIZE, DEFAULT_DATA_DIRECTORY);
    }

    /**
     * Cria uma nova LSMTree com o tamanho de Memtable e diretório de dados definidos pelo usuário.
     *
     * @param mutableMemtableMaxByteSize O tamanho máximo da Memtable antes de ser descarregada para o disco.
     * @param maxLevelZeroSstNumber      O número máximo de SSTables no nível zero.
     * @param dataDir                    O diretório onde os dados serão armazenados.
     */
    public LSMTree(long mutableMemtableMaxByteSize, int maxLevelZeroSstNumber, String dataDir) {
        this.mutableMemtableMaxSize = mutableMemtableMaxByteSize;
        this.maxLevelZeroSstNumber = maxLevelZeroSstNumber;
        this.maxLevelZeroSstByteSize = mutableMemtableMaxByteSize * 2;
        this.dataDir = Paths.get(System.getProperty("user.dir"),
                "benchmark-core", "src", "main", "java", "br", "com", "project", "structs", "lsm", "LSM-data"
        ).toString();
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
     * Adiciona um item à LSMTree. Se a Memtable estiver cheia, ela será descarregada no disco.
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
     * Remove um item da LSMTree. Isso é feito adicionando um "tombstone" (registro de exclusão) à Memtable.
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
     * @param key A chave do item a ser obtido.
     * @return O valor do item, ou null se o item não existir.
     */
    public byte[] get(byte[] key) {
        byte[] result;

        synchronized (mutableMemtableLock) {
            result = mutableMemtable.get(key);
            if (result != null) {
                return result.length == 0 ? null : result;
            }
        }

        synchronized (immutableMemtablesLock) {
            for (Memtable memtable : immutableMemtables) {
                result = memtable.get(key);
                if (result != null) {
                    return result.length == 0 ? null : result;
                }
            }
        }

        synchronized (tableLock) {
            for (ObjectArrayList<SSTable> level : levels) {
                for (SSTable table : level) {
                    result = table.get(key);
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
     * Realiza a compactação das SSTables nos diferentes níveis, mesclando as SSTables entre os níveis e substituindo as tabelas mais antigas.
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

                maxLevelSize = (int) (maxLevelSize * LEVEL_INCR_FACTOR);
                sstMaxSize = (int) (sstMaxSize * LEVEL_INCR_FACTOR);
            }
        }
    }

    /**
     * Cria o diretório onde os dados serão armazenados, caso não exista.
     */
    private void createDataDir() {
        try {
            Files.createDirectories(Paths.get(dataDir));
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao criar diretório: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retorna uma representação em string do estado atual da LSMTree.
     *
     * @return A string representando o estado da LSMTree.
     */
    @Override
    public String toString() {

        var s = new StringBuilder();
        s.append("LSM-Tree {\n");
        s.append("\tmemtable: ");
        s.append(mutableMemtable.byteSize() / 1024.0 / 1024.0);
        s.append(" mb\n");
        s.append("\timmutable memtables: ");
        s.append(immutableMemtables);
        s.append("\n\tsst levels:\n");

        int i = 0;
        for (var level : levels) {
            s.append(String.format("\t\t- %d: ", i));
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
