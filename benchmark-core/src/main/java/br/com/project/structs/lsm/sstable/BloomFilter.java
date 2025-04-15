package br.com.project.structs.lsm.sstable;

import br.com.project.structs.lsm.io.ExtendedInputStream;
import br.com.project.structs.lsm.io.ExtendedOutputStream;
import it.unimi.dsi.fastutil.longs.LongLongMutablePair;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import lombok.Getter;
import org.apache.commons.codec.digest.MurmurHash3;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

/**
 * Implementação simples de um filtro de Bloom.
 * Dado o número esperado de inserções e a taxa de falsos positivos desejada,
 * o tamanho do filtro é calculado e o número de funções de hash é calculado com base na taxa de falsos positivos.
 * O filtro usa duas funções de hash simples para cada chave.
 */
public class BloomFilter {

    static final int DEFAULT_SIZE = 1 << 20;

    final int size;
    @Getter
    final int hashCount;
    final long[] bits;

    /**
     * Cria um novo filtro de Bloom com o tamanho padrão e uma taxa de falsos positivos de 0,1%.
     */
    public BloomFilter() {
        this(DEFAULT_SIZE, 0.001);
    }

    /**
     * Cria um novo filtro de Bloom com o número esperado de inserções e uma taxa de falsos positivos de 0,1%.
     *
     * @param expectedInsertions O número esperado de inserções.
     */
    public BloomFilter(int expectedInsertions) {
        this(expectedInsertions, 0.001);
    }

    /**
     * Cria um novo filtro de Bloom com o número esperado de inserções e a taxa de falsos positivos.
     *
     * @param expectedInsertions O número esperado de inserções.
     * @param falsePositiveRate  A taxa de falsos positivos desejada.
     */
    public BloomFilter(int expectedInsertions, double falsePositiveRate) {
        this.size = (int) (-expectedInsertions * log(falsePositiveRate) / (log(2) * log(2)));
        this.hashCount = (int) ceil(-log(falsePositiveRate) / log(2));
        this.bits = new long[(int) ceil(size / 64.0)];
    }

    /**
     * Cria um novo filtro de Bloom com o tamanho, número de funções de hash e bits fornecidos.
     *
     * @param size      Tamanho do filtro Bloom em bits.
     * @param hashCount Número de funções de hash a serem usadas.
     * @param bits      Array de longos representando os bits do filtro.
     */
    public BloomFilter(int size, int hashCount, long[] bits) {
        this.size = size;
        this.hashCount = hashCount;
        this.bits = bits;
    }

    /**
     * Cria um filtro de Bloom a partir de um arquivo.
     *
     * @param filename O nome do arquivo.
     * @return O filtro de Bloom.
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
     * Verifica se o filtro de Bloom pode conter a chave dada.
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

    private LongLongMutablePair getHash(byte[] key) {
        long[] hashes = MurmurHash3.hash128x64(key, 0, key.length, 0);
        return LongLongMutablePair.of(hashes[0], hashes[1]);
    }

    /**
     * Escreve o filtro de Bloom em um arquivo.
     *
     * @param filename O nome do arquivo.
     */
    public void writeToFile(String filename) {
        ExtendedOutputStream os = new ExtendedOutputStream(filename);

        os.writeVByteInt(size);
        os.writeVByteInt(hashCount);
        os.writeVByteInt(bits.length);

        for (long b : bits)
            os.writeLong(b);

        os.close();
    }

}