package br.com.project.structs.ChordDHT.node;

import br.com.project.entities.Pessoa;
import br.com.project.structs.ChordDHT.hashing.HashFunction;

import java.util.*;


import java.math.BigInteger;

/**
 * Representa um nó em uma DHT (Distributed Hash Table) baseada no protocolo Chord.
 * <p>
 * Cada instância de {@code ChordNode} mantém referências para o predecessor e
 * sucessor no anel lógico, além de uma finger table para roteamento eficiente.
 * Os dados são armazenados em {@code localStorage}, associando um {@link BigInteger}
 * (resultado de hash) a uma instância de {@link Pessoa}.
 * <p>
 * O nó pode ingressar em um anel já existente ({@link #join(NodeReference)}) ou criar
 * um anel próprio ({@code createRing}, não mostrado no snippet), e conta com métodos
 * auxiliares para encontrar sucessores ({@link #findSuccessor(BigInteger, NodeReference)},
 * {@link #findSuccessorLocal(BigInteger)}) e atualizar a finger table.
 */
public class ChordNode {

    private final NodeReference self;
    private NodeReference predecessor;
    private NodeReference successor;
    private int nextFinger = 0; 

    private final FingerTable fingerTable;
    private final HashFunction<String> hashFunction;

    private final Map<BigInteger, Pessoa> localStorage;

    /**
     * Constrói um novo nó Chord a partir de um IP e porta, gerando o ID por meio da função de hash.
     * <p>
     * O {@code fingerSize} indica o tamanho (m) da finger table, 
     * correspondendo a um espaço de endereçamento de 2^m.
     *
     * @param ip            o endereço IP deste nó
     * @param port          a porta em que o nó escuta conexões
     * @param hashFunction  a função de hash utilizada para gerar o ID a partir de {@code ip + ":" + port}
     * @param fingerSize    número de posições na finger table (m bits)
     */
    public ChordNode(String ip, int port, HashFunction<String> hashFunction, int fingerSize) {
        this.hashFunction = hashFunction;

        String nodeKey = ip + ":" + port;
        BigInteger nodeId = hashFunction.hash(nodeKey);
        this.self = new NodeReference(ip, port, nodeId);

        this.predecessor = null;
        this.successor = null;
        this.fingerTable = new FingerTable(fingerSize);
        this.localStorage = new HashMap<>();
    }
    
     /**
     * Faz este nó ingressar em um anel Chord já existente ou criar um anel novo.
     * <p>
     * Se {@code knownNode} for {@code null}, chama internamente {@code createRing()} (não exibido aqui)
     * para criar um anel de um único nó. Caso contrário, localiza o sucessor correto no anel do nó
     * conhecido e ajusta suas referências. Em seguida, inicializa a finger table.
     *
     * @param knownNode referência de um nó já presente no anel, ou {@code null} para criar um anel novo
     */
    public void join(NodeReference knownNode) {
        if (knownNode == null) {
            createRing();
        } else {
            NodeReference succ = findSuccessor(self.getId(), knownNode);

            this.successor = succ;
        }
        initFingerTable();
    }

    /**
     * Inicia a busca do sucessor para um ID específico, começando a partir de um nó conhecido.
     * <p>
     * Em um cenário distribuído real, {@code startNode} corresponderia a um nó remoto
     * e a busca seria feita via RPC. Aqui, para simplificar, assumimos que {@code startNode}
     * é o próprio {@code self}, chamando {@link #findSuccessorLocal(BigInteger)}.
     *
     * @param id         o ID cujo sucessor se deseja encontrar
     * @param startNode  nó inicial para a busca do sucessor
     * @return a {@link NodeReference} que representa o sucessor de {@code id}
     */
    private NodeReference findSuccessor(BigInteger id, NodeReference startNode) {
        if (startNode.equals(self)) {
            return findSuccessorLocal(id);
        }

        return findSuccessorLocal(id);
    }

    /**
     * Versão local de {@link #findSuccessor(BigInteger, NodeReference)}, 
     * operando apenas no contexto deste nó.
     * <p>
     * Se {@code id} estiver no intervalo (self.id, successor.id], retorna {@code successor}.
     * Caso contrário, obtém o nó que precede mais proximamente {@code id} via
     * {@link #closestPrecedingFinger(BigInteger)} e retorna {@code successor} (versão simplificada).
     *
     * @param id o ID cujo sucessor se deseja encontrar
     * @return a {@link NodeReference} do sucessor de {@code id}, segundo a visão local
     */
    private NodeReference findSuccessorLocal(BigInteger id) {
        if (self.equals(successor)) {
            return self;
        }

        if (inInterval(id, self.getId(), successor.getId(), true)) {
            return successor;
        } else {
            NodeReference next = closestPrecedingFinger(id);
            if (next.equals(self)) {
                return successor;
            }
            return successor;
        }
    }

    /**
     * Retorna o nó que precede mais proximamente {@code id}, de acordo com a finger table.
     * <p>
     * Percorre a finger table de trás para frente, buscando a maior entrada cujo ID
     * esteja no intervalo (self.id, id).
     *
     * @param id o ID para o qual se deseja encontrar um predecessor próximo
     * @return a {@link NodeReference} do nó que precede {@code id} de forma mais aproximada,
     *         ou {@code self} se não houver um nó melhor
     */
    private NodeReference closestPrecedingFinger(BigInteger id) {
        for (int i = fingerTable.getSize() - 1; i >= 0; i--) {
            NodeReference finger = fingerTable.getFinger(i);
            if (finger != null && inInterval(finger.getId(), self.getId(), id, false)) {
                return finger;
            }
        }
        return self;
    }

    /**
     * Retorna a referência completa (IP, porta e ID) que representa este nó.
     *
     * @return um {@link NodeReference} descrevendo este nó
     */
    public NodeReference getSelf() {
        return self;
    }

    /**
     * Obtém a referência ao predecessor deste nó.
     *
     * @return um {@link NodeReference} para o predecessor, ou {@code null} se ainda não definido
     */
    public NodeReference getPredecessor() {
        return predecessor;
    }

    /**
     * Obtém a referência ao sucessor deste nó.
     *
     * @return um {@link NodeReference} para o sucessor
     */
    public NodeReference getSuccessor() {
        return successor;
    }

    /**
     * Retorna a finger table utilizada por este nó para roteamento em O(log N).
     *
     * @return a instância de {@link FingerTable} associada a este nó
     */
    public FingerTable getFingerTable() {
        return fingerTable;
    }

    /**
     * Define o predecessor deste nó no anel.
     *
     * @param predecessor referência de nó que será o predecessor
     */
    public void setPredecessor(NodeReference predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * Define o sucessor deste nó no anel.
     *
     * @param successor a referência de nó que passará a ser o sucessor deste nó
     */
    public void setSuccessor(NodeReference successor) {
        this.successor = successor;
    }

    /**
     * Obtém a função de hash que este nó utiliza para gerar IDs.
     *
     * @return a instância de {@link HashFunction} associada a este nó
     */
    public HashFunction<String> getHashFunction() {
        return hashFunction;
    }

    /**
     * Retorna uma representação textual do nó, incluindo 
     * suas referências (self, predecessor, successor).
     *
     * @return uma string que descreve este {@code ChordNode}
     */
    @Override
    public String toString() {
        return "ChordNode{" +
                "self=" + self +
                ", predecessor=" + predecessor +
                ", successor=" + successor +
                '}';
    }

    /**
     * Cria um anel Chord contendo apenas este nó. 
     * <p>
     * Define o predecessor e o sucessor como {@code self}, 
     * e inicializa a finger table para apontar para si mesmo. 
     * É chamado normalmente quando este nó não conhece nenhum outro nó no anel.
     */
    public void createRing() {
        this.predecessor = self;
        this.successor = self;

        int size = fingerTable.getSize();
        for (int i = 0; i < size; i++) {
            fingerTable.setFinger(i, self);
        }
    }

    /**
     * Notifica este nó de que o nó {@code candidate} deseja ser seu predecessor.
     * <p>
     * Se {@code candidate} estiver no intervalo (predecessor, self), 
     * então atualizamos o {@code predecessor} para {@code candidate}.
     *
     * @param candidate nó que se propõe como predecessor
     */
    public void notify(NodeReference candidate) {
        if (predecessor == null || inInterval(candidate.getId(), predecessor.getId(), self.getId(), false)) {

            this.predecessor = candidate;
        }
    }

    /**
     * Rotina de estabilização, chamada periodicamente para manter o anel Chord consistente.
     * <ol>
     *   <li>Obtém o predecessor do sucessor atual.</li>
     *   <li>Se esse predecessor estiver entre este nó e o sucessor, 
     *       redefine o sucessor para ele.</li>
     *   <li>Chama {@code notify(this)} no sucessor, para que ele possa atualizar 
     *       seu predecessor se necessário.</li>
     * </ol>
     */
//    public void stabilize() {
//        ChordNode successorNode = findNodeObject(successor);
//        if (successorNode == null) {
//            return;
//        }
//
//        NodeReference x = successorNode.getPredecessor();
//        if (x != null && x != self && inInterval(x.getId(), self.getId(), successor.getId(), false)) {
//            this.successor = x;
//        }
//
//        successorNode.notify(self);
//    }

    /**
     * Verifica se o valor {@code check} está no intervalo (start, end) 
     * dentro do anel Chord, considerando a aritmética circular.
     * <p>
     * Se {@code includeEnd} for {@code true}, então {@code end} faz parte do intervalo.
     * Caso {@code start} seja maior que {@code end}, o intervalo "cruza" o zero do anel.
     *
     * @param check      valor a ser verificado
     * @param start      início do intervalo
     * @param end        fim do intervalo
     * @param includeEnd indica se o valor {@code end} deve ser incluído no intervalo
     * @return {@code true} se {@code check} estiver dentro do intervalo definido, 
     *         caso contrário {@code false}
     */
    private boolean inInterval(BigInteger check, BigInteger start, BigInteger end, boolean includeEnd) {
        int cmpStart = check.compareTo(start);
        int cmpEnd = check.compareTo(end);

        if (start.compareTo(end) < 0) {
            if (cmpStart > 0 && (cmpEnd < 0 || (includeEnd && cmpEnd == 0))) {
                return true;
            }
            return false;
        } else if (start.compareTo(end) == 0) {
            return true; 
        } else {
            if (cmpStart > 0 || (cmpEnd < 0 || (includeEnd && cmpEnd == 0))) {
                return true;
            }
            return false;
        }
    }

    /**
     * Localiza o objeto {@link ChordNode} correspondente a uma dada {@link NodeReference},
     * caso você esteja simulando vários nós no mesmo processo.
     * <p>
     * Em um sistema distribuído real, essa operação seria uma chamada remota 
     * (por exemplo, via RPC/gRPC) ao nó representado por {@code ref}.
     *
     * @param ref a referência do nó que se deseja obter
     * @return o objeto {@link ChordNode} associado a {@code ref}, ou {@code null} se não existir
     */
//    private ChordNode findNodeObject(NodeReference ref) {
//        return ChordSimulator.localNetwork.get(ref);
//    }

    /**
     * Atualiza periodicamente uma das entradas da finger table (controlada por {@code nextFinger}).
     * <p>
     * Após {@code fingerTable.getSize()} chamadas, todas as posições serão atualizadas,
     * garantindo que a finger table reflita melhor a topologia atual do anel.
     */
    public void fixFingers() {
        nextFinger = (nextFinger + 1) % fingerTable.getSize();
        BigInteger start = fingerTable.calculateStart(self.getId(), nextFinger);
        
        NodeReference succ = findSuccessor(start, self);
        fingerTable.setFinger(nextFinger, succ);
    }

   /**
     * Insere uma pessoa no anel Chord.
     * <p>
     * 1) Calcula o hash do CPF (chave).<br>
     * 2) Encontra o nó responsável por armazenar esse hash (via {@link #findSuccessor}).<br>
     * 3) Armazena a pessoa localmente, caso o nó responsável seja este mesmo nó; 
     *    caso contrário, delega a chamada ao nó responsável (simulado localmente).
     *
     * @param p a instância de {@link Pessoa} que será inserida no anel
     */
//    public void putPessoa(Pessoa p) {
//        BigInteger keyHash = hashFunction.hash(p.getCpf());
//        NodeReference owner = findSuccessor(keyHash, self);
//        if (owner.equals(self)) {
//            localStorage.put(keyHash, p);
//        } else {
//            ChordNode ownerNode = findNodeObject(owner);
//            if (ownerNode != null) {
//                ownerNode.storePessoaLocal(keyHash, p);
//            }
//        }
//    }

    /**
     * Recupera uma pessoa do anel Chord, dado um CPF.
     * <p>
     * Localiza o nó responsável pelo hash do CPF e, se for este nó, 
     * retorna a pessoa do armazenamento local. Caso contrário, 
     * faz uma chamada simulada ao nó responsável para obter a pessoa.
     *
     * @param cpf o CPF da pessoa a ser buscada
     * @return a instância de {@link Pessoa} correspondente ao CPF, 
     *         ou {@code null} se não for encontrada
     */
//    public Pessoa getPessoa(String cpf) {
//        BigInteger keyHash = hashFunction.hash(cpf);
//        NodeReference owner = findSuccessor(keyHash, self);
//        if (owner.equals(self)) {
//            return localStorage.get(keyHash);
//        } else {
//            ChordNode ownerNode = findNodeObject(owner);
//            if (ownerNode != null) {
//                return ownerNode.getPessoaLocal(keyHash);
//            }
//        }
//        return null;
//    }

    /**
     * Armazena uma pessoa localmente, sem roteamento.
     * <p>
     * Este método é utilizado internamente quando o nó responsável 
     * já foi identificado, evitando a busca de sucessor.
     *
     * @param keyHash o hash (ID) calculado a partir do CPF
     * @param p       a instância de {@link Pessoa} a ser armazenada
     */
    private void storePessoaLocal(BigInteger keyHash, Pessoa p) {
        localStorage.put(keyHash, p);
    }

    /**
     * Recupera uma pessoa localmente, sem roteamento.
     * <p>
     * Utilizado internamente quando já se sabe que este nó 
     * é o responsável pela chave.
     *
     * @param keyHash o hash (ID) calculado a partir do CPF
     * @return a instância de {@link Pessoa} armazenada, ou {@code null} se não existir
     */
    private Pessoa getPessoaLocal(BigInteger keyHash) {
        return localStorage.get(keyHash);
    }

    /**
     * Remove este nó do anel Chord de forma graciosa.
     * <p>
     * 1) Se só existe um nó no anel, encerra imediatamente ({@link #shutdownNode()}).<br>
     * 2) Caso contrário, transfere as chaves locais para o sucessor 
     *    ({@link #transferKeysToSuccessor()}).<br>
     * 3) Ajusta o {@code predecessor} e {@code successor} para que se apontem mutuamente.<br>
     * 4) Finaliza recursos e remove este nó do {@code localNetwork}.
     */
//    public void leave() {
//        if (self.equals(successor) && self.equals(predecessor)) {
//            shutdownNode();
//            return;
//        }
//
//        transferKeysToSuccessor();
//
//        ChordNode predNode = findNodeObject(predecessor);
//        ChordNode succNode = findNodeObject(successor);
//
//        if (predNode != null) {
//            predNode.setSuccessor(successor);
//        }
//        if (succNode != null) {
//            succNode.setPredecessor(predecessor);
//        }
//        shutdownNode();
//    }

    /**
     * Transfere todas as chaves armazenadas localmente para o sucessor.
     * <p>
     * Após a transferência, o armazenamento local deste nó fica vazio.
     * Geralmente chamado dentro de {@link #leave()}.
     */
//    private void transferKeysToSuccessor() {
//        if (!successor.equals(self)) {
//            ChordNode succNode = findNodeObject(successor);
//            if (succNode != null) {
//                for (Map.Entry<BigInteger, Pessoa> e : localStorage.entrySet()) {
//                    succNode.storePessoaLocal(e.getKey(), e.getValue());
//                }
//                localStorage.clear();
//            }
//        }
//    }
    
    /**
     * Encerra este nó localmente, removendo-o do {@code localNetwork} 
     * e cancelando timers se necessário.
     * <p>
     * Geralmente chamado após {@link #leave()}, quando o nó sai do anel 
     * e não deve mais participar do roteamento ou armazenar dados.
     */
//    private void shutdownNode() {
//        ChordSimulator.localNetwork.remove(self);
//    }
    
    
    /**
     * Inicializa a finger table deste nó, definindo cada entrada para o {@code successor}.
     * <p>
     * Em uma implementação mais completa, seria feita uma chamada 
     * a {@link #findSuccessor} para cada intervalo 
     * {@code (self.id + 2^i) mod 2^m}, atualizando cada posição da finger table.
     */
    private void initFingerTable() {
        for (int i = 0; i < fingerTable.getSize(); i++) {
            fingerTable.setFinger(i, successor);
        }
    }
    
//    /**
//     * Inicia um {@link java.util.Timer} que chama periodicamente
//     * {@link #stabilize()} e {@link #fixFingers()} para manter o anel coerente.
//     * <p>
//     * A cada 2 segundos, as rotinas de estabilização e atualização da finger table
//     * são executadas, corrigindo eventuais inconsistências e melhorando o roteamento.
//     */
//    public void startStabilizeTimer() {
//    Timer timer = new Timer("chord-stabilize");
//    timer.scheduleAtFixedRate(new TimerTask() {
//        @Override
//        public void run() {
//            stabilize();
//            fixFingers();
//
//            }
//        }, 2000, 2000); // começa depois de 2s e repete a cada 2s
//    }

}