package br.com.project.structs.lsm.io;

import br.com.project.structs.lsm.types.ByteArrayPair;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Esta classe utiliza um FastBufferedInputStream como base e adiciona
 * métodos utilitários para leitura, principalmente para valores inteiros e longos
 * codificados em bytes variáveis.
 */
public class ExtendedInputStream {

    private final FastBufferedInputStream fis;

    /**
     * Inicializa um fluxo de entrada em um arquivo.
     *
     * @param filename o nome do arquivo.
     */
    public ExtendedInputStream(String filename) {
        try {
            fis = new FastBufferedInputStream(new FileInputStream(filename));
            fis.position(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lê um inteiro codificado em bytes variáveis do fluxo.
     *
     * @return o próximo inteiro V-Byte.
     */
    public int readVByteInt() {
        return (int) readVByteLong();
    }

    /**
     * Lê um longo codificado em bytes variáveis do fluxo.
     *
     * @return o próximo long V-Byte.
     */
    public long readVByteLong() {
        long result = 0;
        int b;
        int shift = 0;
        while (true) {
            b = readByteInt();
            result |= (((long) b & 0x7F) << shift);

            if ((b & 0x80) == 0x80)
                break;

            shift += 7;
        }
        return result - 1;
    }

    /**
     * Lê 8 bytes representando um long.
     *
     * @return o próximo long no fluxo.
     */
    public long readLong() {
        try {
            long result = 0;
            for (byte b : fis.readNBytes(8)) {
                result <<= 8;
                result |= (b & 0xFF);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lê um único byte como um inteiro.
     *
     * @return o próximo inteiro de 8 bits no fluxo.
     */
    public int readByteInt() {
        try {
            return fis.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lê N bytes do fluxo.
     *
     * @param n o número de bytes desejado.
     * @return um array com os próximos N bytes.
     */
    public byte[] readNBytes(int n) {
        try {
            return fis.readNBytes(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lê um ByteArrayPair do fluxo.
     * Cada array é codificado como comprimento e payload.
     *
     * @return o próximo item no fluxo.
     */
    public ByteArrayPair readBytePair() {
        try {
            int keyLength = readVByteInt();
            int valueLength = readVByteInt();

            return new ByteArrayPair(
                    readNBytes(keyLength),
                    readNBytes(valueLength)
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Pula N bytes no fluxo.
     *
     * @param n o número de bytes a serem ignorados.
     * @return o número de bytes efetivamente ignorados.
     */
    public long skip(int n) {
        try {
            return fis.skip(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Posiciona o fluxo no deslocamento desejado.
     *
     * @param offset o deslocamento para onde mover o fluxo.
     */
    public void seek(long offset) {
        try {
            fis.position(offset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fecha os recursos do fluxo.
     */
    public void close() {
        try {
            fis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
