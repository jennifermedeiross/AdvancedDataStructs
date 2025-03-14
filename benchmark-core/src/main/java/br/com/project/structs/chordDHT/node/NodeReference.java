package br.com.project.structs.chordDHT.node;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Representa uma referência a um nó Chord, contendo IP, porta e ID (hash).
 * É usada para identificar e contatar outros nós na rede.
 */
public class NodeReference implements Comparable<NodeReference> {

    private final String ip;
    private final int port;
    private final BigInteger id;

    /**
     * Construtor para criar uma referência a um nó.
     * 
     * @param ip   Endereço IP do nó
     * @param port Porta do nó
     * @param id   Hash/ID do nó no anel (gerado via HashFunction)
     */
    public NodeReference(String ip, int port, BigInteger id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public BigInteger getId() {
        return id;
    }

    /**
     * Compara NodeReferences pelo valor do ID (BigInteger).
     * Permite ordenação entre nós, se necessário.
     */
    @Override
    public int compareTo(NodeReference other) {
        if (other == null || other.getId() == null) {
            return 1; 
        }
        return this.id.compareTo(other.getId());
    }

    /**
     * Dois NodeReferences são considerados iguais se tiverem o mesmo ID.
     * Você pode adicionar lógica adicional (por ex. comparar IP/porta),
     * mas normalmente o ID é o identificador principal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeReference)) return false;
        NodeReference that = (NodeReference) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NodeReference{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", id=" + id +
                '}';
    }
}

