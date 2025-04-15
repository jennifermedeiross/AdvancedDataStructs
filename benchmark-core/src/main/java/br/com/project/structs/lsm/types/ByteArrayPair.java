package br.com.project.structs.lsm.types;

/**
 * Representa um par de chave-valor, onde tanto a chave quanto o valor são armazenados como arrays de bytes.
 * A chave é utilizada para ordenação e busca eficiente, enquanto o valor contém os dados associados à chave.
 * Implementa a interface {@code Comparable} para permitir comparações lexicográficas entre chaves,
 * facilitando a organização e a busca dentro de estruturas de dados ordenadas.
 * A chave e o valor podem ser usados em sistemas como LSM-Tree ou outras estruturas que exigem armazenamento
 * e acesso eficientes baseados em arrays de bytes.
 */
public record ByteArrayPair(byte[] key, byte[] value) implements Comparable<ByteArrayPair> {

    public int size() {
        return key.length + value.length;
    }

    /**
     * Retorna a chave encapsulada em um {@link ByteArrayWrapper}.
     * Isso permite comparações baseadas no conteúdo dos bytes da chave.
     *
     * @return a chave como um {@code ByteArrayWrapper}.
     */
    public ByteArrayWrapper getKey() {
        return new ByteArrayWrapper(key);
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public int compareTo(ByteArrayPair other) {
        return getKey().compareTo(other.getKey());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(Key: [");

        for (int i = 0; i < key.length; i++) {
            sb.append(key[i]);
            if (i < key.length - 1) sb.append(", ");
        }

        sb.append("] | Value: [");

        if (value.length == 0) {
            sb.append("TOMBSTONE");
        } else {
            for (int i = 0; i < value.length; i++) {
                sb.append(value[i]);
                if (i < value.length - 1) sb.append(", ");
            }
        }

        sb.append("])");
        return sb.toString();
    }
}
