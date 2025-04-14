## LSM Tree

A LSM Tree, ou Árvore de Mesclagem Baseada em Log, é uma estrutura de dados bastante utilizada em sistemas de arquivos e bancos de dados. Sua principal vantagem está no desempenho eficiente para operações de escrita. Devido a essa característica, é comumente adotada por bancos de dados NoSQL, como o Cassandra, SyllaDB e RocksDB.

### Por que LSM Tree?
O PostgreSQL, por exemplo, utiliza B-Trees como estrutura padrão para armazenamento e busca de dados, com operações em tempo O(log n). Apesar de eficientes para leitura e escrita, as B-Trees enfrentam um problema chamado amplificação de escrita: uma única inserção pode exigir a atualização de várias páginas no disco, resultando em muitas operações de E/S aleatórias. Isso impacta negativamente o desempenho em cargas de escrita intensas.

Nesse cenário, surgem alternativas como a LSM-Tree, que minimizam esse custo ao otimizar o fluxo de inserção e reduzir o número de acessos diretos ao disco.

---

Tudo bem, agora discutiremos como essa estrutura funciona e veremos a implementação em java baseada e inspirada na implementação e arquitetura de [Francesco Tomaselli](https://github.com/tomfran).
Inicialmente, é importante entender que uma árvore LSM é composta de dois componentes principais:

![Fluxo MemTable - SSTable](assets/fluxo-mem-ss-diag.png)

#### Memtable

A MemTable é uma estrutura de dados utilizada como área temporária na memória para armazenar informações antes que sejam persistidas no disco. Ela atua como um intermediário entre o sistema e o armazenamento permanente, permitindo que operações de leitura e escrita sejam realizadas de forma rápida, já que os dados permanecem em memória volátil.

Geralmente, a MemTable é implementada como uma árvore binária balanceada, o que assegura eficiência nas operações de inserção, remoção e busca. Esse balanceamento é fundamental para que a altura da árvore se mantenha proporcional ao logaritmo do número de elementos armazenados, ou seja, **O(log N)**, garantindo um bom desempenho mesmo com grandes volumes de dados.

Quando a MemTable atinge sua capacidade máxima, os dados nela contidos são **descarregados (ou "flushed") para o disco**, no formato de uma **SSTable (Sorted String Table)** — um tipo de armazenamento otimizado para leitura e escrita sequenciais.

Usamos árvore binária de busca balanceadas(AVL) na implementação. Ela têm essa cara:

| ![Exemplo AVL Perfeitamente balanceada](./assets/avl-exemplo1.png)  | ![Exemplo AVL Balanceada](./assets/avl-exemplo2.png) |
|:----------------------------------------------------------:|:---------------------------------------------------:|
| Exemplo de AVL Perfeitamente Balanceada                   | Exemplo de AVL Balanceada                            |

Para nosso exemplo, teremos uma entidade Pessoa que representa de forma básica uma pessoa com nome, CPF, idade, telefone e data de nascimento. A AVLTree recebe um par <String, Pessoa>, em que a key é o CPF e o value é a instância do objeto Pessoa. Nesse caso, o Node armazena apenas o Value, pois a Function extrai a chave (CPF) a partir do valor armazenado (a instância de Pessoa).

Ao inserirmos uma instância de Pessoa na árvore AVL, o CPF é extraído automaticamente por meio da keyExtractor, permitindo que a árvore posicione o nó corretamente sem que o Node precise armazenar a chave separadamente, garantindo que a árvore permaneça balanceada. Além disso, caso uma Pessoa com o mesmo CPF já exista, a árvore irá atualizar os dados dessa Pessoa, mantendo a integridade da estrutura de dados e a ordem de inserção. A busca e remoção também funcionam de maneira similar, utilizando o CPF como chave para localizar a instância da Pessoa correspondente.

**Obs.:** A decisão pelo uso de keyExtractor se deu pela simplicidade que ele traz ao processo de inserção e pela capacidade de tornar a árvore mais genérica. Como a extração da chave (como o CPF) é uma operação direta e de baixo custo, o impacto no desempenho é praticamente nulo. Assim, a estrutura permanece eficiente e reutilizável para diversos tipos de dados, sem comprometer a performance.

Exemplo:
```java
AVLTree<String, Pessoa> avl = new AVLTree<>(Pessoa::getCpf);
avl.add(new Pessoa(...));
```

Na prática, quando a LSMTree adicionar os elementos, eles serão adicionados como um par chave-valor em que ambos são uma `byte[]`:
```java
[LSMTree]

public void add(K key, V value) throws JsonProcessingException {
    synchronized (mutableMemtableLock) {
        mutableMemtable.add(new ByteArrayPair(conversorToByte(key), conversorToByte(value)));
        checkMemtableSize();
    }
}
```
Memtable recebe esse `ByteArrayPair` e adiciona à AVLTree:
```java
[Memtable]

private AVLTree<ByteArrayWrapper, ByteArrayPair> tree;

public Memtable() {
    tree = new AVLTree<>(ByteArrayPair::getKey); // indica como pegar a chave
}

public void add(ByteArrayPair item) {
    tree.add(item);
    byteSize += item.size();
}
```

Vale destacar que, em LSM-Trees, a remoção é tratada como uma nova inserção: adiciona-se um valor especial conhecido como TOMBSTONE (um marcador de exclusão), associado à chave correspondente. Isso indica que o valor foi deletado, mesmo antes da persistência no disco.
```java
[Memtable]

public void remove(byte[] key) {
    tree.add(new ByteArrayPair(key, new byte[]{}));
}
```

---

### SSTable

As SSTables (Sorted String Tables) são uma estrutura central no nosso modelo de armazenamento de dados. Elas representam arquivos imutáveis gravados em disco, contendo pares <chave, valor> ordenados pela chave. Por serem imutáveis, novas alterações de dados (como inserções, deleções ou atualizações) não modificam as SSTables existentes, mas geram novas versões, permitindo uma escrita eficiente em disco.

A SSTable é composta, na nossa implementação, por três arquivos principais:

`.data`: armazena os dados reais (<chave, valor>) em sequência ordenada.

`.index`: contém um índice esparso, que facilita buscas rápidas sem percorrer todo o arquivo.

`.bloom`: representa um filtro de Bloom, utilizado para descartar rapidamente chaves que com certeza não existem na tabela.

#### Criação
Basicamente, a criação de uma SSTable envolve a gravação dos pares no disco, gerando esses três arquivos citados. Durante esse processo, os elementos são lidos a partir de um iterador e armazenados sequencialmente no arquivo `.data`. Ao mesmo tempo, é construído um filtro de Bloom contendo todas as chaves presentes, o que permite otimizar futuras buscas, descartando rapidamente elementos inexistentes. Além disso, é formado um índice esparso para acelerar a localização de dados. Esse índice é baseado em amostras coletadas a cada `sampleSize` elementos. Para cada amostra, são registradas a chave, o deslocamento no arquivo (offset) e a posição no conjunto de dados, que posteriormente são gravados no arquivo `.index`. Ao final da escrita, o filtro de Bloom é persistido no arquivo `.bloom`. Caso o iterador fornecido esteja vazio, o processo é interrompido com uma exceção para evitar a criação de uma SSTable inválida e vazia.

#### Nível 0 e as Memtables Imutáveis
A criação de uma SSTable é diretamente desencadeada quando a Memtable atinge um limite de tamanho e se torna imutável. Essa Memtable imutável é então descarregada no disco como uma nova SSTable. Todas as SSTables recém-criadas são inicialmente armazenadas no `nível 0`, que é reservado para os dados mais recentes e ainda não compactados.

No **nível 0**, várias SSTables podem existir ao mesmo tempo, e é normal que elas tenham chaves repetidas entre si. Isso acontece porque cada SSTable é criada a partir de uma Memtable diferente, em momentos diferentes, e por isso não seguem uma ordem entre si.

No começo, isso não é um problema. Mas com o tempo, à medida que mais SSTables são criadas, essa sobreposição de chaves pode atrapalhar as buscas. Para resolver isso, o sistema faz a **compactação**: junta várias SSTables antigas, organiza os dados e move para níveis mais altos, onde as SSTables não têm chaves repetidas entre si. Assim, o sistema continua rápido e eficiente.

A principal vantagem das SSTables está na eficiência da leitura e escrita sequencial, ideal para discos magnéticos ou SSDs. Como os dados são ordenados e imutáveis, eles podem ser escritos uma única vez e lidos de forma otimizada, especialmente com o suporte de estruturas auxiliares como o índice esparso e o filtro de Bloom.

Abaixo segue uma representação simplória de como os dados chegam à SSTable.

![Fluxo incial que mostra como os dados chegam à SSTable](./assets/sstable-inicial.png)

Mais a diante, discutiremos o fluxo completo. A priori, o objetivo é entendermos o que é cada um desses componentes da **LSMTree**.

---

### Flush e Compactação

#### Estratégia Baseada em Inspeção Temporizada para Flush e Compactação
Na arquitetura da LSM-Tree, tanto o flush da Memtable quanto a compactação entre níveis são operações essenciais para manter a estrutura eficiente, organizada e rápida em operações de leitura e escrita.

Embora seja possível realizar essas operações com base em limites de tamanho ou quantidade de dados (por exemplo, ao ultrapassar o número máximo de SSTables no nível 0 ou o tamanho total em bytes), optamos por uma estratégia baseada em inspeções periódicas(mantendo a estratégia de Francesco). Isso significa que o sistema verifica de tempos em tempos — em intervalos definidos — se é necessário realizar o flush ou iniciar uma nova compaction.

Isso porque, ao realizar testes com cargas razoavelmente grandes (a partir de 10.000 registros), observamos que a estratégia baseada em verificação por tamanho apresentava um leve impacto negativo no tempo de execução. Embora essa diferença de desempenho não tenha sido drástica, a abordagem com inspeção temporizada se mostrou mais eficiente e estável para o nosso cenário. Ela permite que o sistema mantenha sua performance mesmo sob variações de carga, evitando pausas ou gargalos causados por verificações frequentes baseadas em tamanho ou quantidade de SSTables.

