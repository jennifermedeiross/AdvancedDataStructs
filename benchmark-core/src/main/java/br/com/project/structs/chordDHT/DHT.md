# Chord DHT - Documentação

Esta documentação descreve uma *DHT (Distributed Hash Table)* baseada no protocolo *Chord. O objetivo é armazenar e recuperar dados de forma distribuída (aqui, **simulada localmente*) com roteamento em O(log N) e manutenção dinâmica de nós.

---

## Sumário

- *Visão Geral do Protocolo Chord*
- *Estrutura Geral e Classes*
- *Fluxo de Operações*
  - Criação de Anel (createRing())
  - Entrada no Anel (join(...))
  - Inserção de Dados (putPessoa(...))
  - Busca de Dados (getPessoa(...))
  - Roteamento de Chaves (findSuccessor, closestPrecedingFinger)
  - Estabilização (stabilize(), fixFingers())
  - Saída de Nó (leave())
- *Timer de Estabilização*
- *Simulação Local vs. Distribuída*
- *Exemplo de Uso*
- *Pontos de Extensão*
- *Conclusão*

---

## Visão Geral do Protocolo Chord

O *Chord* organiza os nós em um *anel lógico*, onde cada nó possui:

- Um *ID* (gerado por hash).
- *Sucessor* e *Predecessor* para navegação no anel.
- Uma *Finger Table* que acelera o roteamento para O(log N).

Ao inserir ou buscar uma chave (por exemplo, o hash de um CPF), localizamos o *nó responsável* usando findSuccessor(...), que roteia a requisição até o nó correto, o qual então armazena ou devolve os dados.

---

## Estrutura Geral e Classes

- *ChordNode*: Implementa a lógica de um nó Chord (criar anel, entrar, roteamento, armazenamento local, estabilização etc.).
- *NodeReference*: Estrutura leve com ip, port e id (BigInteger) para identificar nós.
- *FingerTable*: Mantém referências para saltos em O(log N).
- *HashFunction*: Converte uma string (por exemplo, ip:port ou cpf) em BigInteger.
- *ChordDHT* (opcional): Orquestrador que gerencia vários nós em um único processo.

---

## Fluxo de Operações

### Criação de Anel (createRing())
- Se o nó não conhece ninguém, define predecessor = self e successor = self, formando um anel de *1 nó*.

### Entrada no Anel (join(...))
- Se knownNode é null, chama createRing().
- Caso contrário, faz findSuccessor(self.id, knownNode) para descobrir o sucessor e ajusta this.successor.
- O sucessor chamará notify(this) para atualizar seu predecessor.

### Inserção de Dados (putPessoa(...))
1. Gera hash do CPF (keyHash = hashFunction.hash(p.getCpf())).
2. Localiza nó dono (owner = findSuccessor(keyHash, self)).
3. Se owner for este nó, armazena localmente; senão, delega ao nó responsável.

### Busca de Dados (getPessoa(...))
1. Gera hash do CPF (keyHash = hashFunction.hash(cpf)).
2. Acha nó dono (owner = findSuccessor(keyHash, self)).
3. Se owner for este nó, retorna de localStorage; senão, faz chamada simulada a owner.

### Roteamento de Chaves (findSuccessor, closestPrecedingFinger)
- *findSuccessor(id, startNode)*: em Chord real, seria remoto. Aqui, chamamos findSuccessorLocal(id).
- *findSuccessorLocal(id)*: verifica se id está em (self.id, successor.id]; se sim, retorna successor; caso contrário, chama closestPrecedingFinger(id).

### Estabilização (stabilize(), fixFingers())
- *stabilize()*:
  1. Lê x = successor.predecessor.
  2. Se x está entre (self, successor), redefine successor = x.
  3. Chama successor.notify(self).
- *fixFingers()*:
  - Atualiza periodicamente uma entrada da finger table:

    finger[i] = findSuccessor((self.id + 2^i) mod 2^m)


### Saída de Nó (leave())
1. Se só há 1 nó, chama shutdownNode().
2. Senão, transfere chaves para o sucessor.
3. Conecta predecessor e successor diretamente.
4. Chama shutdownNode() para remover este nó.

---

## Timer de Estabilização

Chamando startStabilizeTimer(), inicia-se um Timer que, a cada X segundos, chama:

- stabilize()
- fixFingers()

Isso corrige inconsistências e atualiza gradualmente a finger table.

---

## Simulação Local vs. Distribuída

- *Simulação Local*: As “chamadas remotas” (findSuccessor, notify, etc.) são simuladas com um mapa Map<NodeReference, ChordNode> (por ex., ChordSimulator.localNetwork).
- *Distribuído Real*: Cada nó rodaria em um processo/máquina diferente e as chamadas seriam via RPC, gRPC ou outro protocolo.

---

## Exemplo de Uso

java
HashFunction<String> hashFunc = new Sha1HashFunction();

// Nó A
ChordNode nodeA = new ChordNode("127.0.0.1", 5000, hashFunc, 8);
nodeA.join(null); // cria anel
nodeA.startStabilizeTimer();

// Nó B
ChordNode nodeB = new ChordNode("127.0.0.1", 5001, hashFunc, 8);
nodeB.join(nodeA.getSelf());
nodeB.startStabilizeTimer();

// Insere pessoa
Pessoa p = new Pessoa("Fulano", 30, "12345678900", "99-9999-9999", LocalDate.of(1993, 5, 10));
nodeA.putPessoa(p);

// Busca a partir de B
Pessoa found = nodeB.getPessoa("12345678900");
System.out.println("Encontrada: " + found);

// Nó B sai
nodeB.leave();


---

## Pontos de Extensão

1. Replicação: Armazenar cópias das chaves no sucessor para lidar com falhas não-graciosas.


2. Transferência de chaves ao entrar: Após join(), chamar requestKeysFromSuccessor() para realocar imediatamente as chaves.


3. Falhas não-graciosas: Implementar checkPredecessor() ou checagens no stabilize() para remover nós inativos.


4. Roteamento Multi-hop Completo: Em Chord “puro”, findSuccessorLocal chamaria recursivamente outros nós até chegar ao destino.




---

## Conclusão

Esta implementação Chord cobre:

Criação/entrada de anel (createRing, join).

Roteamento de chaves (findSuccessor, closestPrecedingFinger).

Inserção/busca (putPessoa, getPessoa).

Manutenção do anel (stabilize, fixFingers).

Saída graciosa (leave).


Embora local, ela reflete os princípios centrais do protocolo Chord, permitindo comparar desempenho com outras estruturas (por exemplo, TreeMap, B-Tree, LSM) e estudar comportamento de sistemas distribuídos em cenários controlados.