## MemTable

### Definição

A **MemTable** é uma estrutura de dados temporária utilizada em sistemas de armazenamento para gerenciar operações de escrita de forma eficiente. Para ilustrar seu funcionamento, imagine o cenário de uma biblioteca onde várias pessoas desejam registrar empréstimos de livros. Em vez de atualizar diretamente o sistema principal (que pode ser lento devido a operações de I/O), os registros são inicialmente anotados em um bloco de notas temporário. Esse bloco de notas representa a **MemTable**, que atua como uma camada intermediária para armazenar dados de forma rápida antes de serem persistidos no armazenamento permanente.

### Funcionamento da MemTable

A MemTable é projetada para otimizar operações de inserção, busca e remoção de dados. Abaixo, detalhamos cada uma dessas operações:

#### 1. Inserção (`put`)

- **Processo**: Quando um novo dado é recebido (por exemplo, um registro de empréstimo de livro), ele é imediatamente anotado na MemTable.
- **Ordenação**: A MemTable utiliza uma estrutura de dados como o **TreeMap**, que garante que os dados sejam automaticamente ordenados por chave. Isso facilita operações de busca e manutenção da consistência dos dados.

#### 2. Busca (`get`)

- **Processo**: Para verificar se um determinado dado está presente (por exemplo, "Este livro já foi emprestado?"), a MemTable é consultada primeiro.
- **Fluxo**: Se o dado for encontrado na MemTable, a operação é concluída rapidamente. Caso contrário, a busca é estendida ao banco de dados principal, onde os dados são armazenados de forma permanente.

#### 3. Remoção (`delete`)

- **Processo**: Quando um dado precisa ser removido (por exemplo, um livro é devolvido), a MemTable não apaga o registro imediatamente. Em vez disso, o registro é marcado como **"removido"** por meio de um marcador especial chamado **tombstone** (semelhante a um rabisco no bloco de notas).
- **Persistência**: Quando a MemTable atinge sua capacidade máxima, seus dados (incluindo os registros marcados com tombstones) são transferidos para uma estrutura de armazenamento permanente, como o **SSTable** (Sorted String Table). Após essa transferência, a MemTable é esvaziada e está pronta para receber novos dados.
