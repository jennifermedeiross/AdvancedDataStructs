package br.com.project.structs.lsm.bloom;

import br.com.project.structs.lsm.io.ExtendedInputStream;
import br.com.project.structs.lsm.io.ExtendedOutputStream;
import it.unimi.dsi.fastutil.longs.LongLongMutablePair;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import org.apache.commons.codec.digest.MurmurHash3;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

/**
 * Implementação de um filtro de Bloom.
 * <p>
 * Dado o número de inserções esperadas e a taxa de falso positivo desejada,
 * o tamanho é calculado como -expectedInsertions * log(falsePositiveRate) / (log(2) * log(2))
 * e o número de funções de hash é calculado como ceil(-log(falsePositiveRate) / log(2)).
 * <p>
 * Dois hashes são calculados para cada chave usando uma única chamada para MurmurHash3 128 bits.
 * Em seguida, utilizamos a fórmula (h1 + i * h2) % size para obter o i-ésimo hash da chave.
 */
public class BloomFilter {

    static final int DEFAULT_SIZE = 1 << 20; // Tamanho padrão do filtro de Bloom

    final int size; // Tamanho do filtro em bits
    final int hashCount; // Número de funções de hash
    final long[] bits; // Array de bits para armazenar os valores

    /**
     * Cria um novo filtro de Bloom com o tamanho padrão e uma taxa de falso positivo de 0,1%.
     */
    public BloomFilter() {
        this(DEFAULT_SIZE, 0.001);
    }

    /**
     * Cria um novo filtro de Bloom com um número esperado de inserções e uma taxa de falso positivo de 0,1%.
     *
     * @param expectedInsertions Número esperado de inserções.
     */
    public BloomFilter(int expectedInsertions) {
        this(expectedInsertions, 0.001);
    }

    /**
     * Cria um novo filtro de Bloom com o número esperado de inserções e a taxa de falso positivo especificada.
     *
     * @param expectedInsertions Número esperado de inserções.
     * @param falsePositiveRate  Taxa de falso positivo desejada.
     */
    public BloomFilter(int expectedInsertions, double falsePositiveRate) {
        this.size = (int) (-expectedInsertions * log(falsePositiveRate) / (log(2) * log(2)));
        this.hashCount = (int) ceil(-log(falsePositiveRate) / log(2));
        this.bits = new long[(int) ceil(size / 64.0)];
    }

    /**
     * Cria um novo filtro de Bloom a partir dos parâmetros fornecidos.
     *
     * @param size      Tamanho do filtro de Bloom em bits.
     * @param hashCount Número de funções de hash.
     * @param bits      Array de bits armazenados.
     */
    public BloomFilter(int size, int hashCount, long[] bits) {
        this.size = size;
        this.hashCount = hashCount;
        this.bits = bits;
    }

    /**
     * Lê um filtro de Bloom a partir de um arquivo.
     *
     * @param filename O nome do arquivo a ser lido.
     * @return O filtro de Bloom lido do arquivo.
     */
    public static BloomFilter readFromFile(String filename) {
        ExtendedInputStream is = new ExtendedInputStream(filename);
        try {
            int size = is.readVByteInt();
            int hashCount = is.readVByteInt();
            int bitsLength = is.readVByteInt();
            long[] bits = new long[bitsLength];

            for (int i = 0; i < bitsLength; i++)
                bits[i] = is.readLong();

            is.close();
            return new BloomFilter(size, hashCount, bits);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adiciona uma chave ao filtro de Bloom.
     *
     * @param key A chave a ser adicionada.
     */
    public void add(byte[] key) {
        LongLongPair hash = getHash(key);
        long h1 = hash.leftLong(), h2 = hash.rightLong();

        for (int i = 0; i < hashCount; i++) {
            int bit = (int) Math.abs((h1 + i * h2) % size);
            bits[bit / 64] |= 1L << (bit % 64);
        }
    }

    /**
     * Verifica se o filtro de Bloom pode conter a chave fornecida.
     *
     * @param key A chave a ser verificada.
     * @return Verdadeiro se o filtro pode conter a chave, falso caso contrário.
     */
    public boolean mightContain(byte[] key) {
        LongLongPair hash = getHash(key);
        long h1 = hash.leftLong(), h2 = hash.rightLong();

        for (int i = 0; i < hashCount; i++) {
            int bit = (int) Math.abs((h1 + i * h2) % size);
            if ((bits[bit / 64] & (1L << (bit % 64))) == 0)
                return false;
        }

        return true;
    }

    /**
     * Calcula os hashes MurmurHash3 128-bit para a chave fornecida.
     *
     * @param key A chave a ser processada.
     * @return Um par contendo os dois valores de hash gerados.
     */
    private LongLongMutablePair getHash(byte[] key) {
        long[] hashes = MurmurHash3.hash128x64(key, 0, key.length, 0);
        return LongLongMutablePair.of(hashes[0], hashes[1]);
    }

    /**
     * Escreve o filtro de Bloom em um arquivo.
     *
     * @param filename O nome do arquivo para escrita.
     */
    public void writeToFile(String filename) {
        ExtendedOutputStream os = new ExtendedOutputStream(filename);

        os.writeVByteInt(size);
        os.writeVByteInt(hashCount);
        os.writeVByteInt(bits.length);

        for (var b : bits)
            os.writeLong(b);

        os.close();
    }
}
