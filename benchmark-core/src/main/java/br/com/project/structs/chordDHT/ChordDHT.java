package br.com.project.structs.chordDHT;

import br.com.project.entities.Pessoa;
import br.com.project.structs.chordDHT.hashing.HashFunction;
import br.com.project.structs.chordDHT.hashing.Sha1HashFunctionPessoa;
import br.com.project.structs.chordDHT.node.ChordNode;
import br.com.project.structs.chordDHT.node.NodeReference;

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
     * Construtor sem parâmetros que utiliza valores padrão:
     * - Função de hash: {@link Sha1HashFunctionPessoa}
     * - Tamanho da finger table: 4
     */
    public ChordDHT() {
        this.hashFunction = new Sha1HashFunctionPessoa();
        this.fingerSize = 4;
    }

    /**
     * Construtor que recebe a função de hash e o tamanho da finger table (m).
     * Exemplo: fingerSize=8 para um espaço pequeno, ou 160 para SHA-1 real.
     *
     * @param hashFunction Função de hash a ser usada.
     * @param fingerSize   Número de posições na finger table.
     */
    public ChordDHT(HashFunction<String> hashFunction, int fingerSize) {
        this.hashFunction = hashFunction;
        this.fingerSize = fingerSize;
    }

    /**
     * Cria um anel novo, com um único nó (ip:port), e inicia o timer de estabilização.
     *
     * @param ip   O endereço IP do nó.
     * @param port A porta em que o nó será iniciado.
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
     *
     * @param ip        O endereço IP do novo nó.
     * @param port      A porta do novo nó.
     * @param knownNode Referência a um nó já existente no anel.
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
     *
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
     *
     * @param ref Referência do nó onde a pessoa será inserida.
     * @param p   Pessoa a ser armazenada.
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
     *
     * @param ref O nó de referência para iniciar a busca.
     * @param cpf O CPF da pessoa a ser procurada.
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