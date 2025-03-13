### DHT.md ###

Este documento descreve a implementação de uma DHT (Distributed Hash Table) baseada no protocolo Chord, utilizada para armazenar e recuperar dados de forma distribuída. No nosso caso, simulamos o comportamento distribuído localmente em um único processo, mas seguimos os princípios de roteamento e manutenção do anel definidos pelo protocolo Chord.


---

1. Visão Geral

O Chord é um protocolo que organiza os nós em um anel lógico:

1. Cada nó possui um ID (gerado por uma função de hash).


2. Sucessor e Predecessor: cada nó mantém ponteiros para o próximo e o anterior no anel.


3. Finger Table: uma tabela de tamanho  (onde o espaço de IDs é ) que acelera a busca de chaves em O() saltos.



Quando precisamos inserir ou buscar uma chave (por exemplo, o hash de um CPF), localizamos o nó responsável via findSuccessor(...). Esse nó armazena localmente os dados.


---

2. Estrutura de Pastas e Classes

2. Estrutura de Pastas e Classes

benchmark-core
└── src
    └── main
        └── java
            └── br
                └── com
                    └── project
                        └── structs
                            └── ChordDHT
                                ├── hashing
                                │   ├── HashFunction.java
                                │   └── Sha1HashFunctionPessoa.java
                                └── node
                                    ├── ChordNode.java
                                    ├── FingerTable.java
                                    ├── NodeReference.java
                                ├── ChordDHT.java (opcional - orquestrador)
                                └── DHT.md (este arquivo de documentação)

1. ChordNode

Implementa a lógica de um nó Chord (criação/entrada de anel, estabilização, roteamento de chaves, armazenamento local).

Possui métodos como join(...), createRing(), stabilize(), fixFingers(), putPessoa(), getPessoa(), leave().

Armazena dados em Map<BigInteger, Pessoa>.



2. NodeReference

Estrutura leve para representar um nó (IP, porta, ID).

Usada para identificar nós na finger table, no predecessor/sucessor, etc.



3. FingerTable

Guarda as referências que aceleram o roteamento (para cada , aponta para o nó responsável por ).

Métodos como calculateStart(...) ajudam a encontrar esses intervalos.



4. HashFunction (e implementações)

Define como transformar uma string (por ex. cpf ou ip:port) em um BigInteger.

Exemplo: Sha1HashFunctionPessoa, que hasheia o cpf via SHA-1.



5. ChordDHT (Orquestrador, opcional)

Pode gerenciar múltiplos nós em um só processo, criando e conectando nós.

Mantém um Map<NodeReference, ChordNode> para simular “chamadas remotas” localmente.





---

3. Fluxo de Operações

3.1. Criação de Anel: createRing()

Se o nó não conhece ninguém, chama createRing(), definindo:

predecessor = self;
successor = self;

Assim, temos um anel de um nó.


3.2. Entrada no Anel: join(NodeReference knownNode)

Se knownNode é null, chamamos createRing().

Se não, chamamos findSuccessor(self.id, knownNode) para descobrir quem é nosso sucessor.

Ajustamos this.successor e inicializamos a finger table (initFingerTable()).

Em seguida, o sucessor chamará notify(this) para atualizar seu predecessor.


3.3. Inserção de Dados: putPessoa(Pessoa p)

1. Calcula o hash do CPF:

BigInteger keyHash = hashFunction.hash(p.getCpf());


2. Descobre o nó dono da chave (owner = findSuccessor(keyHash, self)).


3. Se owner.equals(self), armazena em localStorage. Senão, faz uma chamada simulada ao owner.



3.4. Busca de Dados: getPessoa(String cpf)

1. Calcula keyHash = hashFunction.hash(cpf).


2. owner = findSuccessor(keyHash, self).


3. Se for self, pega de localStorage. Senão, chama owner.getPessoaLocal(keyHash) simuladamente.



3.5. Roteamento: findSuccessor(...), closestPrecedingFinger(...)

findSuccessor(id, startNode) inicia a busca a partir de startNode.

Em ambiente real, seria remoto; aqui, chamamos findSuccessorLocal(id).

findSuccessorLocal(id) verifica se id está no intervalo (self.id, successor.id]; se sim, retorna successor. Caso contrário, obtém closestPrecedingFinger(id) e pula para esse nó (na versão real, seria recursivo ou multi-hop).


3.6. Estabilização: stabilize(), fixFingers()

stabilize()

1. Acessa successor.predecessor.


2. Se esse predecessor (x) estiver entre (self, successor), redefine successor = x.


3. Chama successor.notify(self).



fixFingers()

Atualiza periodicamente uma entrada da finger table:



\text{finger}[i] = \text{findSuccessor}\bigl((\text{self.id} + 2^i) \mod 2^m\bigr).

3.7. Saída do Nó: leave()

1. Se há só um nó (self == successor == predecessor), chamamos shutdownNode().


2. Senão, transferimos chaves para o sucessor (transferKeysToSuccessor()).


3. Ajustamos predecessor e successor para se apontarem mutuamente.


4. Chamamos shutdownNode() para encerrar timers e remover o nó do localNetwork.




---

4. Timer de Estabilização

Chamando startStabilizeTimer(), iniciamos um Timer que a cada 2 segundos executa:

stabilize(): Corrige sucessor/predecessor.

fixFingers(): Atualiza finger table gradualmente.



---

5. Simulação Local vs. Distribuído

Nesta implementação, os nós estão todos em um único processo. As “chamadas remotas” são simuladas através de um mapa Map<NodeReference, ChordNode> (por exemplo, em uma classe ChordSimulator ou ChordDHT). Em um sistema distribuído real:

Cada ChordNode estaria em um processo (ou máquina) diferente.

As chamadas (findSuccessor, notify, etc.) seriam via RPC/gRPC/RMI.



---

6. Exemplo de Uso

public static void main(String[] args) {
    // Exemplo de hash para strings
    HashFunction<String> hashFunc = new Sha1HashFunction();

    // Cria um nó (A) e inicia anel
    ChordNode nodeA = new ChordNode("127.0.0.1", 5000, hashFunc, 8);
    nodeA.join(null);
    nodeA.startStabilizeTimer();

    // Cria outro nó (B) e entra no anel de A
    ChordNode nodeB = new ChordNode("127.0.0.1", 5001, hashFunc, 8);
    nodeB.join(nodeA.getSelf());
    nodeB.startStabilizeTimer();

    // Insere pessoa
    Pessoa p = new Pessoa("Fulano", 30, "12345678900", "99-9999-9999", LocalDate.of(1993, 5, 10));
    nodeA.putPessoa(p);

    // Busca pessoa via nó B
    Pessoa found = nodeB.getPessoa("12345678900");
    System.out.println("Encontrada: " + found);

    // Remove nó B do anel
    nodeB.leave();
}


---

7. Pontos de Extensão

1. Replicação: Armazenar cópias das chaves no sucessor para tolerar falhas não-graciosas.


2. Transferência de Chaves ao Entrar: O novo nó pode chamar requestKeysFromSuccessor() para receber imediatamente as chaves que agora lhe pertencem.


3. Falhas Não-Graciosas: Implementar checkPredecessor() e checagens de sucessor no stabilize() para remover nós inativos que não chamaram leave().


4. Roteamento Multi-hop: Na versão simplificada, findSuccessorLocal retorna successor ao invés de fazer saltos sucessivos. Para Chord “puro”, faríamos recursão ou iteração de nó em nó.




---

8. Conclusão

Este projeto demonstra como Chord pode ser implementado de maneira simplificada em Java, usando:

Uma função de hash para gerar IDs (por ex. Sha1HashFunction).

Estruturas para manter predecessor, sucessor e finger table (ChordNode, FingerTable).

Métodos de inserção e busca (putPessoa, getPessoa).

Mecanismos de manutenção do anel (stabilize, fixFingers, leave).


Mesmo rodando tudo localmente, os conceitos centrais do protocolo Chord — anel lógico, roteamento em O(), manutenção dinâmica de nós — são mantidos. Isso possibilita comparações de desempenho com outras estruturas avançadas (LSM-Tree, B-Tree, TreeMap) e serve como base para estudos de sistemas distribuídos.

