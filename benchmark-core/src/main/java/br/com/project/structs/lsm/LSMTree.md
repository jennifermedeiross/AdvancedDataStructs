### **Introdução**

Imagina que estamos lidando com um banco de dados onde as inserções acontecem o tempo todo, em grande quantidade e velocidade. O problema é que gravar direto no disco seria muito lento, então precisamos de uma solução esperta pra isso. É aí que entra a **LSM-Tree** (Log-Structured Merge Tree), que foi feita pra otimizar gravações. Ela é usada em sistemas como Cassandra, RocksDB, LevelDB e DynamoDB.

A estrutura da LSM-Tree basicamente tem duas partes principais:
- **Memtable** (em memória): onde os dados ficam temporariamente antes de irem pro disco.
- **SSTables** (em disco): onde os dados são armazenados de forma permanente e ordenada.

A sacada da LSM-Tree é separar bem as operações de escrita e leitura pra manter o desempenho alto. Em vez de gravar direto no disco, jogamos tudo na memória primeiro e depois, de tempos em tempos, despejamos os dados no disco de forma organizada.

---
### **MemTable**

---

A **Memtable** é como uma estante organizada onde colocamos novos itens sempre no lugar certo. Isso facilita muito na hora de encontrar qualquer coisa rapidamente.

Ela usa uma estrutura chamada **Skip List**, que funciona como uma estante com vários andares. Em vez de procurar livro por livro na prateleira de baixo, subimos um pouco pra ter uma visão melhor e descemos no lugar certo. Assim, evitamos percorrer tudo e encontramos o que queremos de forma mais eficiente.

Características principais:
- **Ordenação**: Tudo dentro da Memtable já fica organizado.
- **Múltiplos níveis**: Quanto mais elementos, mais níveis podemos ter.
- **Ponteiros inteligentes**: Se um item aparece num nível superior, ele também existe nos níveis mais baixos.

Como funciona a busca na Skip List?
- Começamos pelo nível mais alto e descemos até encontrar o que queremos.
- Se o item não estiver onde esperávamos, descemos mais um nível até chegar na base.

E a inserção?
- Procuramos o lugar certo pra colocar o novo dado.
- Um processo aleatório define em quantos níveis ele vai aparecer.

Agora, a Memtable não dura pra sempre. Quando ela enche, criamos uma versão **imutável** dela e começamos outra do zero. Essa Memtable imutável fica esperando ser escrita no disco, enquanto novas inserções vão pra uma Memtable nova e vazia.

---
### **SSTable (Sorted String Table)**

---

Depois que os dados saem da Memtable, eles viram uma **SSTable**, que é um arquivo no disco onde as informações ficam organizadas e imutáveis. Isso significa que, uma vez gravados, não podemos mais modificar os registros – só podemos adicionar novos arquivos.

Pra acelerar as buscas, usamos um índice esparso, que funciona como aqueles guias de dicionário onde você só vê algumas palavras-chave pra pular direto pro trecho certo.

Além disso, temos os **Filtros Bloom**, que são como um “oráculo” que nos diz se um item definitivamente **não está** lá. Se ele diz que o dado pode estar, então fazemos a busca de verdade.

---
### **Bloom Filter**

---

O **Bloom Filter** é tipo um detector rápido que nos ajuda a evitar buscas desnecessárias. Ele não garante que um item existe, mas pode garantir que **não existe**, economizando tempo.

Funciona assim:
- Quando adicionamos um item, aplicamos várias funções de hash pra marcar algumas posições num vetor de bits.
- Quando queremos verificar se um item está presente, olhamos essas posições.
- Se todas as posições estão preenchidas, pode ser que o item esteja lá. Se alguma estiver vazia, certeza que não está.

O legal é que isso ocupa pouquíssimo espaço, o que torna o filtro super eficiente pra sistemas grandes.

---
### **Compactação**

---

Com o tempo, várias SSTables pequenas vão se acumulando, tornando a busca mais lenta. A solução? Compactação!

Esse processo junta várias SSTables pequenas em uma maior, eliminando versões antigas e deixando tudo mais organizado. Assim, evitamos procurar em arquivos desnecessários e garantimos que só os dados mais novos fiquem disponíveis.

A compactação funciona por níveis: os arquivos mais recentes ficam em níveis baixos, enquanto os mais antigos e já organizados sobem pra níveis superiores.

---
### **Fluxo de Operações da LSM-Tree**

---

Agora, vamos ver como tudo acontece na prática:

1. **Inserção:**
   - Os dados entram na **Memtable** (estrutura em memória, rápida e ordenada).
   - Quando a Memtable enche, ela vira uma versão **imutável** e uma nova começa a ser preenchida.
   - As MemTables imutáveis são despejadas no disco como **SSTables**.

2. **Busca:**
   - Primeiro olhamos na Memtable.
   - Se não estiver lá, verificamos as MemTables imutáveis.
   - Se ainda não encontramos, buscamos nas SSTables mais recentes primeiro.
   - O **Bloom Filter** pode nos poupar tempo descartando buscas desnecessárias.

3. **Despejo do Memtable:**
   - Quando a Memtable atinge o limite, criamos uma imutável e começamos outra.
   - A Memtable imutável é escrita no disco como uma nova SSTable.
   
4. **Compactação de Tabelas:**
   - Como várias SSTables pequenas vão sendo criadas, precisamos uni-las de tempos em tempos.
   - A compactação junta arquivos, remove duplicatas e organiza os dados.

---
### **Benefícios da LSM-Tree**

---

- **Alta taxa de gravação**: Inserções são rápidas porque tudo vai primeiro pra memória.
- **Leitura eficiente**: Bloom Filters, índices e compactação ajudam a acelerar a busca.
- **Escalabilidade**: Funciona bem em sistemas distribuídos e grandes volumes de dados.

---
### **Referências**

1. **GitHub**. Tomfran. *LSM Tree*. Disponível em: [https://github.com/tomfran/LSM-Tree](https://github.com/tomfran/LSM-Tree).

2. **CS.UMB.EDU**. O'Neil, P. *LSM-Tree: Log-Structured Merge Tree*. Disponível em: [https://www.cs.umb.edu/~poneil/lsmtree.pdf](https://www.cs.umb.edu/~poneil/lsmtree.pdf).

3. **YouTube**. *LSM Tree - Introduction and Background*. Disponível em: [https://www.youtube.com/watch?v=lbEl8nXM7pE](https://www.youtube.com/watch?v=lbEl8nXM7pE).

4. **YouTube**. *LSM Tree - Overview and Design*. Disponível em: [https://www.youtube.com/watch?v=I6jB0nM9SKU](https://www.youtube.com/watch?v=I6jB0nM9SKU).

5. **Stack Overflow**. *What is the difference between the term SSTable and LSM-Tree?* Disponível em: [https://stackoverflow.com/questions/58168809/what-is-the-differences-between-the-term-sstable-and-lsm-tree](https://stackoverflow.com/questions/58168809/what-is-the-differences-between-the-term-sstable-and-lsm-tree).

