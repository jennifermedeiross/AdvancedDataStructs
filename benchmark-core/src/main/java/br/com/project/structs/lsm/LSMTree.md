### **Introdução**

Imaginemos que estamos gerenciando um sistema de banco de dados onde as inserções de dados acontecem em grande quantidade, de forma rápida e constante. O que queremos é uma maneira eficiente de lidar com isso, certo? A **LSM-Tree** (Log-Structured Merge Tree) surgiu para isso. Ela foi desenvolvida para otimizar justamente as operações de gravação, e é usada em sistemas como o Cassandra, RocksDB, LevelDB, DynamoDB, entre outros.

**A estrutura LSM-Tree** é composta basicamente por duas partes:
- **Memtable** (em memória): onde armazenamos temporariamente os dados enquanto eles ainda estão "quentes" e precisam ser processados rapidamente.
- **SSTables** (em disco): onde armazenamos os dados de forma permanente e ordenada.

A ideia central da LSM-Tree é separar as operações de gravação e leitura para garantir uma boa performance. Ou seja, em vez de gravar os dados diretamente no disco (o que seria mais demorado), fazemos as inserções na memória e, de tempos em tempos, "despejamos" essas informações no disco.

---
### **MemTable**

---
Imaginemos que a **Memtable** é como uma estante de livros organizados de forma bem ordenada, onde a cada novo livro que compramos, colocamos ele diretamente na estante. A vantagem é que, com essa estante organizada, podemos achar qualquer livro rapidamente, pois a ordem dos livros facilita a busca.

Dentro da **Memtable**, usamos uma estrutura chamada **Skip List**. Vamos pensar nela como uma estante que tem vários andares. Cada andar de livros é uma versão mais "rápida" para achar um livro, já que subimos para um andar superior e vamos descendo até encontrar o livro que buscamos. A ideia é que, em vez de ter que percorrer todos os livros (o que levaria muito tempo), conseguimos pular para o próximo "andar" mais rápido.

Algumas propriedades importantes dessa estrutura:
- **Ordenação**: Os livros no nível zero estão sempre ordenados.
- **Estrutura de múltiplos níveis**: O número de andares na estante é proporcional ao logaritmo do número de livros (elementos).
- **Ponteiros**: Se um livro está em um andar mais alto, ele também estará em andares inferiores.

**Busca na Skip List** funciona da seguinte forma:
- Começamos no andar mais alto da estante (Memtable) e procuramos o livro até que a chave desejada seja maior que o livro que estamos olhando.
- Se isso acontecer, descemos para um andar inferior e continuamos até encontrar o livro ou chegar ao andar mais baixo (onde estão todos os livros ordenados).

**Inserção na Skip List** é bem parecida. Quando queremos inserir um novo livro na estante, procuramos o lugar correto (onde ele deve ir). Para decidir em que andar colocá-lo, usamos um processo probabilístico (como jogar uma moeda) para decidir o nível em que ele vai aparecer.

---
### **SSTable (Sorted String Table)**

---
Agora, quando o livro já está "pronto" e é necessário guardá-lo de forma permanente, ele vai para o **disco**. Imaginemos o **SSTable** como um arquivo imutável onde não podemos mais alterar os livros uma vez que os colocamos lá. Uma vez armazenados, esses livros (ou dados) não mudam.

Para facilitar a busca, usamos uma **indexação**. Isso funciona mais ou menos como um índice de livros, onde você não precisa olhar todos os livros um por um. Usamos um índice esparso, com uma amostra de livros de cada vez, e fazemos uma **busca binária** nesse índice para achar onde o livro está guardado.

Além disso, temos os **Filtros Bloom**, que são como uma "lista de livros que provavelmente não existem". Se o filtro disser que o livro não existe, a busca é interrompida. Mas se o filtro disser que ele pode estar lá, então fazemos a busca de fato. A vantagem dos Filtros Bloom é que eles são compactos, mas podem nos dizer "errado" que um livro não está lá (falso negativo), mas nunca nos dirão que o livro está lá quando ele não estiver.

Perfeito! Vamos continuar com a explicação de **Bloom Filter** e **compactação**, mas de uma forma mais fluida, sem listar os benefícios.

---
### **Bloom Filter**

---
Vamos imaginar que estamos procurando um livro em uma biblioteca imensa, mas sem saber se o livro realmente está lá. Para não perdermos tempo procurando em todos os cantos, utilizamos uma ferramenta chamada **Bloom Filter**.

O Bloom Filter é uma estrutura probabilística que nos diz se um item **provavelmente** está na coleção ou **definitivamente não está**. Ele é como um **sistema de triagem** que faz uma verificação rápida e nos ajuda a evitar buscas longas quando o item definitivamente não está presente.

Por exemplo, se queremos saber se o livro "O Senhor dos Anéis" está na biblioteca, o Bloom Filter pode rapidamente nos dizer que **provavelmente não está**. Se ele disser que **talvez esteja**, então sim, precisamos buscar mais a fundo. A ideia é que o Bloom Filter nunca vai afirmar que o livro não está quando ele realmente está, mas pode nos dar um falso positivo, dizendo que o livro está, mesmo que não esteja.

**Como ele funciona?**
O Bloom Filter utiliza funções de **hash** para mapear a chave de um item a uma série de posições em uma estrutura de bits. Quando inserimos um dado, essas posições são marcadas. Quando queremos verificar se um dado está presente, o filtro verifica se as posições indicadas pelas funções de hash estão marcadas. Se alguma posição não estiver marcada, sabemos que o item não está presente.

O grande truque aqui é que ele **ocupa muito pouco espaço**. Em vez de manter todos os dados ou uma lista enorme, ele apenas guarda algumas marcas em um espaço compacto. Isso economiza muito tempo e memória, especialmente em sistemas grandes.

---
### **Compactação**

---
Agora, vamos falar sobre a **compactação** na LSM-Tree. Imaginemos que, à medida que estamos fazendo anotações em vários cadernos (representando as SSTables), vamos acumulando várias páginas com as mesmas informações. No começo, pode ser fácil de organizar, mas com o tempo fica complicado e ineficiente consultar e armazenar esses dados.

A compactação é o processo de pegar várias dessas "anotações" (SSTables) e **organizar tudo em um só lugar**. Esse processo de **organização e reescrita** visa eliminar dados duplicados e manter as informações de maneira mais compacta, facilitando as buscas.

Quando uma SSTable atinge um certo tamanho, ela precisa ser "despejada" para o disco. Mas com o passar do tempo, várias SSTables pequenas vão se acumulando, o que acaba tornando as consultas mais lentas, porque precisamos percorrer muitas delas. Aqui entra a compactação: ela pega várias SSTables, compara as chaves de cada uma e as reorganiza em uma nova tabela, já eliminando as chaves antigas ou duplicadas.

É como se tivéssemos várias pilhas de papéis (SSTables) com anotações repetidas e, ao organizá-las, fizéssemos uma nova pilha com as informações mais recentes e sem repetições.

A compactação também ajuda a **organizar os dados em níveis**. No início, as SSTables são pequenas e ficam em um nível mais baixo. Com o tempo, conforme as tabelas vão se compactando, elas vão sendo movidas para níveis mais altos, tornando a leitura mais eficiente, pois não precisamos consultar todas as SSTables do disco, mas sim as mais recentes.

---

Com esses dois componentes — `Bloom Filter` e `Compactação` — a LSM-Tree consegue ser uma estrutura muito eficiente, principalmente em sistemas que lidam com um grande volume de gravações e leituras. Eles ajudam a manter o sistema rápido e organizado, economizando tempo e espaço de armazenamento.

---
### **Fluxo de Operações da LSM-Tree**

---

Agora, imaginemos o seguinte fluxo de operações para um sistema que utiliza **LSM-Tree**:

1. **Inserção**:
    - Quando queremos inserir um dado, colocamos ele diretamente no **Memtable** (a estante em memória).
    - Se o **Memtable** atingir um limite (como uma estante que já está cheia), ele é "despejado" para o **disco** na forma de uma **SSTable**.

2. **Busca**:
    - Quando queremos buscar um dado, começamos pela **Memtable**. Se o dado não estiver lá, procuramos nas **Memtables imutáveis** que foram preparadas para ir para o disco.
    - Se não encontrarmos o dado em nenhuma dessas etapas, buscamos nas **SSTables** do disco, começando pela tabela mais recente.

3. **Despejo do Memtable**:
    - Quando o **Memtable** atinge seu limite, ele é despejado para o disco. Isso é feito de forma assíncrona, ou seja, enquanto o sistema continua funcionando, ele vai "empurrando" esses dados para o disco sem bloquear o sistema.

4. **Compactação de Tabelas**:
    - Ao longo do tempo, muitas **SSTables** podem ser criadas. Isso pode fazer com que a leitura fique mais lenta, pois precisamos verificar várias tabelas.
    - Para resolver isso, as **SSTables** são periodicamente "compactadas", ou seja, combinamos várias tabelas em uma nova tabela maior, descartando dados duplicados e mantendo apenas os dados mais recentes.
---

### **Benefícios da LSM-Tree**

---

- **Alta taxa de gravação**: Como as inserções são feitas em memória (Memtable), as operações de gravação são muito rápidas.
- **Leitura eficiente**: A leitura pode envolver vários níveis, mas com o uso de Filtros Bloom e indexação, conseguimos acelerar o processo.
- **Escalabilidade**: A estrutura é muito bem adaptada para ser distribuída em sistemas grandes e escaláveis.

---

### **Referências**

---
1. **GitHub**. Tomfran. *LSM Tree*. Disponível em: [https://github.com/tomfran/LSM-Tree](https://github.com/tomfran/LSM-Tree).

2. **CS.UMB.EDU**. O'Neil, P. *LSM-Tree: Log-Structured Merge Tree*. Disponível em: [https://www.cs.umb.edu/~poneil/lsmtree.pdf](https://www.cs.umb.edu/~poneil/lsmtree.pdf).

3. **YouTube**. *LSM Tree - Introduction and Background*. Disponível em: [https://www.youtube.com/watch?v=lbEl8nXM7pE](https://www.youtube.com/watch?v=lbEl8nXM7pE).

4. **YouTube**. *LSM Tree - Overview and Design*. Disponível em: [https://www.youtube.com/watch?v=I6jB0nM9SKU](https://www.youtube.com/watch?v=I6jB0nM9SKU).

5. **Stack Overflow**. *What is the difference between the term SSTable and LSM-Tree?* Disponível em: [https://stackoverflow.com/questions/58168809/what-is-the-differences-between-the-term-sstable-and-lsm-tree](https://stackoverflow.com/questions/58168809/what-is-the-differences-between-the-term-sstable-and-lsm-tree).
