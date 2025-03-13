package br.com.project.structs;

import br.com.project.entities.Pessoa;
import br.com.project.structs.ChordDHT.hashing.HashFunction;
import br.com.project.structs.ChordDHT.node.ChordNode;
import br.com.project.structs.ChordDHT.node.NodeReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe orquestradora que gerencia múltiplos nós Chord em um mesmo processo.
 * Ela mantém um "localNetwork" que mapeia NodeReference -> ChordNode,
 * facilitando a criação de nós, entrada em anéis, remoção e operações de dados.
 */
public class ChordDHT {

    private final Map<NodeReference, ChordNode> localNetwork = new HashMap<>();

    private final HashFunction<String> hashFunction;
    private final int fingerSize;

    /**
     * Construtor que recebe a função de hash e o tamanho da finger table (m).
     * Exemplo: fingerSize=8 para um espaço pequeno, ou 160 para SHA-1 real.
     */
    public ChordDHT(HashFunction<String> hashFunction, int fingerSize) {
        this.hashFunction = hashFunction;
        this.fingerSize = fingerSize;
    }

    /**
     * Cria um anel novo, com um único nó (ip:port), e inicia o timer de estabilização.
     * @return O objeto ChordNode criado.
     */
    public ChordNode createRing(String ip, int port) {
        ChordNode node = new ChordNode(ip, port, hashFunction, fingerSize);
        localNetwork.put(node.getSelf(), node);
        node.join(null);
        node.startStabilizeTimer();
        return node;
    }

    /**
     * Cria um novo nó (ip:port) e faz join no anel do 'knownNode'.
     * Em seguida, inicia o timer de estabilização.
     * @return O objeto ChordNode criado.
     */
    public ChordNode joinNode(String ip, int port, NodeReference knownNode) {
        ChordNode node = new ChordNode(ip, port, hashFunction, fingerSize);
        localNetwork.put(node.getSelf(), node);
        node.join(knownNode);
        node.startStabilizeTimer();
        return node;
    }

    /**
     * Remove um nó do anel, chamando leave().
     * @param ref Referência do nó que desejamos remover.
     */
    public void removeNode(NodeReference ref) {
        ChordNode node = localNetwork.get(ref);
        if (node != null) {
            node.leave();  
        }
    }

    /**
     * Insere uma pessoa no nó correspondente a 'ref'.
     * Esse nó fará o roteamento (findSuccessor) e armazenará no dono correto.
     */
    public void putPessoa(NodeReference ref, Pessoa p) {
        ChordNode node = localNetwork.get(ref);
        if (node != null) {
            node.putPessoa(p);
        }
    }

    /**
     * Faz uma busca de pessoa (por CPF) a partir de um nó 'ref'.
     * Esse nó roteia a busca até o nó que possui a chave.
     * @return Pessoa encontrada ou null se não existir.
     */
    public Pessoa getPessoa(NodeReference ref, String cpf) {
        ChordNode node = localNetwork.get(ref);
        if (node != null) {
            return node.getPessoa(cpf);
        }
        return null;
    }

    /**
     * Lista todos os nós do anel (que temos registrados no localNetwork).
     * Pode ser útil para debug.
     */
    public void printAllNodes() {
        for (Map.Entry<NodeReference, ChordNode> entry : localNetwork.entrySet()) {
            NodeReference key = entry.getKey();
            ChordNode node = entry.getValue();
            System.out.println("NodeRef: " + key + " => " + node);
        }
    }


    /**
     * Retorna o mapa que associa cada {@link NodeReference} à respectiva instância de {@link ChordNode}
     * na simulação local.
     *
     * @return um {@code Map<NodeReference, ChordNode>} representando a rede local de nós
     */
    public Map<NodeReference, ChordNode> getLocalNetwork() {
        return localNetwork;
    }
}
