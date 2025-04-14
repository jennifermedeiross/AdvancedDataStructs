package br.com.project.structs.lsm.io;

import br.com.project.structs.lsm.types.ByteArrayPair;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Esta classe utiliza um FastBufferedInputStream como base e adiciona
 * métodos utilitários a ele, principalmente para a leitura de inteiros e longs
 * codificados em bytes.
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
     * Lê um inteiro codificado em bytes do fluxo.
     *
     * @return o próximo inteiro V-Byte.
     */
    public int readVByteInt() {
        return (int) readVByteLong();
    }

    /**
     * Lê um long codificado em bytes do fluxo.
     * Um long codificado em bytes é escrito como:
     * |bit de continuação| 7 bits de carga útil|
     * Por exemplo, suponha que o CPF de uma instância de uma classe Pessoa seja "123.456.789-01".
     * Para armazená-lo como um número, pode-se convertê-lo para um valor longo, removendo os separadores,
     * resultando em 12345678901. Esse valor será representado no fluxo utilizando bytes da seguinte forma:
     * |1|0110110|1|1100101|0|0010110| ...
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
     * Lê 8 bytes que representam um long.
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
     * Lê N bytes.
     *
     * @param n a quantidade desejada de bytes.
     * @return um array contendo os próximos N bytes.
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
     * Cada array é codificado como tamanho, seguido do conteúdo.
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
     * @param offset o deslocamento para o qual o fluxo deve ser movido.
     */
    public void seek(long offset) {
        try {
            fis.position(offset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fecha os recursos utilizados.
     */
    public void close() {
        try {
            fis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}