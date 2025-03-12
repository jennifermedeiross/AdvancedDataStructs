package br.com.project.structs.lsm.io;

import br.com.project.structs.lsm.types.ByteArrayPair;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;

import java.io.FileOutputStream;

/**
 * Esta classe utiliza um FastBufferedOutputStream como base e adiciona
 * métodos utilitários para gravação, principalmente para valores inteiros e longos
 * codificados em bytes variáveis.
 */
public class ExtendedOutputStream {

    private static final byte[] VBYTE_BUFFER = new byte[10];
    private final FastBufferedOutputStream fos;

    /**
     * Inicializa um fluxo de saída em um arquivo.
     *
     * @param filename o nome do arquivo.
     */
    public ExtendedOutputStream(String filename) {
        try {
            fos = new FastBufferedOutputStream(new FileOutputStream(filename));
            fos.position(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Escreve um array de bytes no fluxo.
     *
     * @param bytes array a ser escrito.
     * @return número de bytes escritos.
     */
    public int write(byte[] bytes) {
        try {
            fos.write(bytes);
            return bytes.length;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Escreve um inteiro codificado em bytes variáveis no fluxo.
     *
     * @param n inteiro a ser escrito.
     * @return número de bytes escritos.
     */
    public int writeVByteInt(int n) {
        return write(intToVByte(n));
    }

    /**
     * Escreve um longo codificado em bytes variáveis no fluxo.
     *
     * @param n long a ser escrito.
     * @return número de bytes escritos.
     */
    public int writeVByteLong(long n) {
        return write(longToVByte(n));
    }

    /**
     * Escreve 64 bits no fluxo.
     *
     * @param n long a ser escrito.
     * @return número de bytes escritos.
     */
    public int writeLong(long n) {
        return write(longToBytes(n));
    }

    /**
     * Escreve um ByteArrayPair no fluxo.
     * Cada array é codificado como comprimento e payload.
     *
     * @param pair item a ser escrito.
     * @return número de bytes escritos.
     */
    public int writeByteArrayPair(ByteArrayPair pair) {
        byte[] key = pair.key(), value = pair.value();
        byte[] keyBytes = intToVByte(key.length), valueBytes = intToVByte(value.length);

        byte[] result = new byte[keyBytes.length + valueBytes.length + key.length + value.length];

        System.arraycopy(keyBytes, 0, result, 0, keyBytes.length);
        System.arraycopy(valueBytes, 0, result, keyBytes.length, valueBytes.length);

        System.arraycopy(key, 0, result, keyBytes.length + valueBytes.length, key.length);
        System.arraycopy(value, 0, result, keyBytes.length + valueBytes.length + key.length, value.length);
        return write(result);
    }

    /**
     * Converte um inteiro para representação V-Byte.
     *
     * @param n inteiro a ser convertido.
     * @return array de bytes armazenando o resultado.
     */
    byte[] intToVByte(int n) {
        return longToVByte(n);
    }

    /**
     * Fecha os recursos do fluxo.
     */
    public void close() {
        try {
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] longToVByte(long n) {
        n++;

        if (n <= 0) {
            throw new IllegalArgumentException("n deve ser maior que 0");
        }

        int i = 0;
        while (n > 0) {
            VBYTE_BUFFER[i++] = (byte) (n & 0x7F);
            n >>>= 7;
        }

        VBYTE_BUFFER[i - 1] |= 0x80;
        byte[] res = new byte[i];
        System.arraycopy(VBYTE_BUFFER, 0, res, 0, i);
        return res;
    }

    private byte[] longToBytes(long n) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (n & 0xFF);
            n >>>= 8;
        }
        return result;
    }
}
