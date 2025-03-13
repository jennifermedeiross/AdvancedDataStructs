package br.com.project.structs.lsm.io;

import br.com.project.structs.lsm.types.ByteArrayPair;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;

import java.io.FileOutputStream;

/**
 * Classe para escrita de inteiros e longs codificados em bytes variáveis utilizando FastBufferedOutputStream.
 */
public class ExtendedOutputStream {

    private static final byte[] VBYTE_BUFFER = new byte[10];
    private final FastBufferedOutputStream fos;

    /**
     * Inicializa um fluxo de saída para um arquivo.
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
     * Escreve um long codificado em bytes variáveis no fluxo.
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
     *
     * @param pair item a ser escrito.
     * @return número de bytes escritos.
     */
    public int writeByteArrayPair(ByteArrayPair pair) {
        return write(longToVByte(pair.key().length))
                + write(longToVByte(pair.value().length))
                + write(pair.key())
                + write(pair.value());
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

    /**
     * Converte um número long para bytes codificados com comprimento variável.
     */
    private byte[] longToVByte(long n) {
        if (++n <= 0) throw new IllegalArgumentException("n deve ser maior que 0");

        int i = 0;
        while (n > 0) {
            VBYTE_BUFFER[i++] = (byte) (n & 0x7F);
            n = n / 128;
        }
        VBYTE_BUFFER[i - 1] |= 0x80;

        byte[] res = new byte[i];
        System.arraycopy(VBYTE_BUFFER, 0, res, 0, i);
        return res;
    }

    /**
     * Converte um número long para 8 bytes.
     */
    private byte[] longToBytes(long n) {
        byte[] result = new byte[8];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (n & 0xFF);
            n = n / 256;
        }
        return result;
    }

    /**
     * Converte um número inteiro para bytes codificados com comprimento variável.
     */
    private byte[] intToVByte(int n) {
        if (++n <= 0) throw new IllegalArgumentException("n deve ser maior que 0");

        int i = 0;
        while (n > 0) {
            VBYTE_BUFFER[i++] = (byte) (n & 0x7F);
            n = n / 128;
        }
        VBYTE_BUFFER[i - 1] |= 0x80;

        byte[] res = new byte[i];
        System.arraycopy(VBYTE_BUFFER, 0, res, 0, i);
        return res;
    }
}
