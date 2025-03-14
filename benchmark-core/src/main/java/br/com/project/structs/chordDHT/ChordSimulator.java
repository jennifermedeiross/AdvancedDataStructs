package br.com.project.structs.chordDHT;

import br.com.project.structs.chordDHT.node.NodeReference;
import br.com.project.structs.chordDHT.node.ChordNode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Simula um ambiente para execução da rede Chord, armazenando todos os nós.
 */
public class ChordSimulator {

    /**
     * Rede local simulada dos nós Chord.
     * A chave é a referência do nó, e o valor é o próprio objeto {@link ChordNode}.
     */
    public static final Map<NodeReference, ChordNode> localNetwork = new ConcurrentHashMap<>();

    /**
     * Adiciona um novo nó à simulação da rede Chord.
     *
     * @param node Nó a ser adicionado.
     */
    public static void addNode(ChordNode node) {
        localNetwork.put(node.getSelf(), node);
    }

    /**
     * Remove um nó da simulação da rede Chord.
     *
     * @param node Nó a ser removido.
     */
    public static void removeNode(ChordNode node) {
        localNetwork.remove(node.getSelf());
    }

    /**
     * Obtém um nó específico da rede simulada.
     *
     * @param ref Referência do nó desejado.
     * @return O objeto {@link ChordNode} correspondente, ou {@code null} se não encontrado.
     */
    public static ChordNode getNode(NodeReference ref) {
        return localNetwork.get(ref);
    }
}