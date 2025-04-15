package br.com.project.structs.lsm.types;

import java.util.Arrays;

/**
 * Um wrapper para arrays de bytes que implementa a interface {@code Comparable}.
 * Essa classe permite o uso de arrays de bytes ({@code byte[]}) como chaves em estruturas
 * de dados ordenadas, como {@code AVLTree}, já que arrays por si só
 * não implementam {@code equals}, {@code hashCode} ou {@code Comparable} de forma adequada.
 * A comparação entre dois {@code ByteArrayWrapper} é feita de forma lexicográfica,
 * utilizando {@code Arrays.compare}. Servirá como chave.
 */
public class ByteArrayWrapper implements Comparable<ByteArrayWrapper> {
    private final byte[] data;

    /**
     * Constrói um {@code ByteArrayWrapper} com o array de bytes fornecido.
     *
     * @param data o array de bytes a ser encapsulado.
     */
    public ByteArrayWrapper(byte[] data) {
        this.data = data;
    }

    /**
     * Retorna o array de bytes encapsulado.
     *
     * @return o array de bytes original.
     */
    public byte[] getData() {
        return data;
    }

    @Override
    public int compareTo(ByteArrayWrapper other) {
        if (data == null)
            return other.data == null ? 0 : -1;

        int aLen = data.length;
        int bLen = other.data.length;

        if (aLen != bLen)
            return aLen < bLen ? -1 : 1;

        for (int i = 0; i < aLen; i++) {
            byte aByte = data[i];
            byte bByte = other.data[i];
            if (aByte != bByte)
                return aByte < bByte ? -1 : 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ByteArrayWrapper)) return false;
        return Arrays.equals(data, ((ByteArrayWrapper) obj).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}

