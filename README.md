# AdvancedDataStructs

**Estruturas**

As estruturas foram implementadas com base em referências que encontramos em livros, artigos, fóruns e github. Além disso, elaboramos documentos detalhados sobre cada uma delas com explicações de códigos e exemplos concretos para tornar o entendimento claro.

* [BTree \- Documento explicativo.](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/documentation/structures/btree/Btree.md)  
* [TreeMap \- Documento explicativo.](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/documentation/structures/treeMap/TreeMap.md)  
* [LSM-Tree \- Documento explicativo.](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/documentation/structures/lsm/LSM.md)

Códigos de implementação:

* [BTree \- Implementação.](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/benchmark-core/src/main/java/br/com/project/structs/btree/BTree.java)  
* [LSM-Tree \- Implementação.](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/benchmark-core/src/main/java/br/com/project/structs/lsm/tree/LSMTree.java) (Envolveu alguns [arquivos](https://github.com/jennifermedeiross/AdvancedDataStructs/tree/main/benchmark-core/src/main/java/br/com/project/structs/lsm))  
* [TreeMap \- Implementação.](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/benchmark-core/src/main/java/br/com/project/structs/treeMap/TreeMap.java)

**Testes**

Para garantir corretude, elaboramos testes de unidade para cada estrutura.

* [BTree \- Testes](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/benchmark-core/src/test/java/br/com/project/btree/BtreeTest.java)  
* [TreeMap \- Testes](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/benchmark-core/src/test/java/br/com/project/treeMap/TreeMapTest.java)  
* [LSM-Tree \- Testes](https://github.com/jennifermedeiross/AdvancedDataStructs/tree/main/benchmark-core/src/test/java/br/com/project/lsm)

**Dados**

Para a geração dos dados fictícios, utilizamos a biblioteca **Faker** do Python. ([Script de geração](https://github.com/jennifermedeiross/AdvancedDataStructs/blob/main/data-factory/data_generator.py))  
 	Nosso objetivo era gerar dados básicos para simular uma representação de pessoas, o que poderia ser comparado a operações envolvendo clientes, por exemplo.

Esse script gerou arquivos **.json** contendo as cargas variáveis mencionadas anteriormente, seguindo o seguinte formato:

```
[{

       "nome": "Sr. Benicio Pacheco",

       "cpf": "042.186.395-15",

       "idade": 41,

       "telefone": "+55 27 95944-6715",

       "dataNascimento": "28/12/1983"

   	}
]
```

[Nossos controladores para geração de resultados.](https://github.com/jennifermedeiross/AdvancedDataStructs/tree/main/benchmark-core/src/main/java/br/com/project/controller)

---

## Análise:

---
### Inserção

![Inserção gráfico](https://github.com/user-attachments/assets/5fb05ebc-abc0-47f9-ab78-d2ed02f62513)

**Btree**

Vê-se que as três estruturas tendem a se assemelhar em desempenho à medida que o número de operações aumenta. As diferenças entre elas se devem, principalmente, às **constantes ocultas** que são ignoradas na notação Big-O, mas que impactam diretamente o tempo de execução na prática. A estrutura B-tree, por exemplo, apresenta um pequeno aumento no tempo de remoção a partir das 10.000 adições. Isso ocorre porque, ao atingir sua capacidade máxima de chaves, um nó da B-tree precisa se dividir, promovendo sua mediana para o nó pai. Esse processo pode se repetir de forma recursiva até a raiz, o que gera um custo adicional. Considerando uma B-tree de grau mínimo 1000, como no exemplo, é possível perceber que a cada 10.000 inserções, a quantidade de elementos exige um novo rebalanceamento, o que torna o gráfico gradualmente mais inclinado. Esse comportamento ilustra bem como os detalhes de implementação e os fatores constantes influenciam o desempenho real, mesmo entre estruturas que possuem complexidades assintóticas semelhantes.

**LSM-Tree**

O comportamento da LSM-Tree durante as inserções mostra um tempo inicialmente alto — entre 0 e 5000 inserções — que depois se estabiliza, aproximando-se do desempenho do TreeMap. Isso ocorre porque, no início, cada inserção é feita na **mutableMemtable**, uma estrutura em memória rápida. No entanto, ao atingir seu limite, essa estrutura é transformada em uma Memtable imutável e uma nova é criada. Em paralelo, uma thread realiza o **flush** dessas **Memtables** imutáveis para o disco como **SSTables**, processo mais lento devido ao acesso a disco.

Nas primeiras milhares de inserções, esse flush ocorre com frequência, gerando muitas **SSTables** e sobrecarregando as threads de **I/O e compactação**, o que aumenta o tempo médio por inserção. A partir da 5000ª inserção, a estrutura começa a se estabilizar: a compactação organiza os dados, reduz arquivos redundantes e alinha o ritmo entre inserções e gravações. Com isso, a **mutableMemtable** permanece ativa por mais tempo, e o impacto do disco se dilui.

Após essa fase, a maioria das inserções ocorre em memória, enquanto as operações mais pesadas seguem em segundo plano, permitindo que a **LSM-Tree atinja desempenho semelhante ao TreeMap**. Por isso, apesar do custo inicial mais alto, a **LSM-Tree** oferece ótima performance e escalabilidade para grandes volumes de dados.

**TreeMap**

O comportamento da TreeMap se destaca pela sua estabilidade e eficiência. Representado pela linha azul, o treeMap mantém um tempo de inserção praticamente constante e muito próximo de zero, mesmo com o aumento significativo da carga de dados, que varia de 1000 até 100.000 elementos. Esse comportamento é explicado pela estrutura interna do treeMap, que é baseada em uma árvore vermelho-preto, garantindo inserções com complexidade logarítimica O(log n).   
Na prática, isso significa que, mesmo à medida que a quantidade de dados cresce, o tempo necessário para inserir novos elementos não aumenta de forma significativa. Isso demonstra uma excelente escalabilidade e torna o treeMap uma opção muito eficiente quando se deseja manter os dados ordenados e realizar inserções com bom desempenho.   
Em conclusão o gráfico evidencia que o treeMap oferece inserções rápidas e estáveis, sendo uma excelente escolha para aplicações que exigem inserções frequentes e ordenação automática dos elementos.  
Sua inserção funciona das seguintes maneiras, quando um novo par chave-valor é inserido, estrutura percorre a árvore a partir da raiz, comparando a chave a ser inserida com as chaves já existentes, utilizando a ordem natural dos elementos (ou um comparador, se fornecido). Se a chave já existir, o valor é atualizado, caso contrário, o novo nó é inserido na posição correta, respeitando a ordenação.   
Após a inserção, a árvore pode executar uma série de rotações e recoloração para manter as propriedades da árvore vermelho-preto, garantindo que ela permaneça balanceadas. Esse balanceamento automático é essencial para que todas as operações, tenham complexidade O(log n).

---
**Remoção**

![image](https://github.com/user-attachments/assets/16c4baab-db20-4dc9-a89b-763d20ce42b5)

**Btree**

O comportamento da B-tree durante a remoção se deve, principalmente, à forma como ela executa o rebalanceamento. A remoção pode se encaixar em três casos distintos. No primeiro caso, quando o valor a ser removido se encontra em uma folha e essa folha ainda possui o número mínimo de chaves exigido, a remoção é direta: o valor é simplesmente eliminado, sem necessidade de rebalanceamento ou qualquer outra operação estrutural, o que torna esse caso muito eficiente em termos de desempenho. No segundo caso, se o valor a ser removido está em um nó interno, ele deve ser substituído por seu antecessor (maior valor da subárvore esquerda) ou por seu sucessor (menor valor da subárvore direita). Após essa substituição, a remoção continua de forma recursiva na subárvore correspondente. Esse processo envolve mais etapas que o primeiro caso, mas ainda assim mantém um bom desempenho, já que a altura da B-tree tende a ser baixa devido ao seu alto fator de ramificação. Por fim, o terceiro caso ocorre quando o valor a ser removido está em uma subárvore que já contém o número mínimo de chaves permitido. Nesse cenário, a B-tree precisa executar operações adicionais para manter suas propriedades estruturais. Isso pode envolver a redistribuição de chaves com nós irmãos ou a fusão de nós, o que pode propagar alterações para os níveis superiores da árvore. Essas operações tornam esse caso o mais custoso dos três, impactando mais diretamente o desempenho da remoção, especialmente à medida que o número de elementos cresce. Ainda assim, no geral, a B-tree apresenta um crescimento suave no tempo de remoção, como visto no gráfico, graças ao seu design equilibrado e adaptado para minimizar a profundidade da árvore.

**LSM-Tree**

A LSM se comporta de forma diferente na operação de remoção porque, ao invés de deletar diretamente a chave, ela apenas insere um marcador especial que é o *tombstone* na Memtable. Este marcador indica que a chave foi removida, mesmo que versões anteriores ainda estejam presentes em níveis mais baixos da árvore, como nas SSTables, que são imutáveis. Por isso, a remoção na LSM Tree é, na verdade, uma inserção – rápida, leve e feita inteiramente em memória. Não há reestruturação da árvore nem leitura ou escrita direta no disco nesse momento. Isso faz com que o tempo de remoção se mantenha praticamente constante, independentemente da quantidade total de dados armazenados. Em muitos casos, com o uso de técnicas como buffering e gravação sequencial, o desempenho pode até melhorar com volumes maiores. A complexidade do processo é adiada para a compactação, quando os tombstones são usados para efetivamente remover os dados obsoletos das SSTables. Até lá, a remoção continua sendo tratada como uma simples adição de informação à estrutura, o que explica sua eficiência e estabilidade mesmo em escalas muito grandes.

**TreeMap**

Podemos reparar que para a treeMap o tempo de remoção cresce linearmente à medida  que a quantidade de elementos aumenta. Isso ocorre porque, embora a Red-Black Tree seja uma árvore balanceada e garanta complexidade O(log n) para remoções, o custo envolvido no rebalanceamento  da árvore (como rotações e recolorações) após a exclusão de um nó,para manter as propriedades fundamentais da estrutura, pode se acumular em grandes volumes de dados. Essa reorganização é essencial para manter o balanceamento e, portanto, a eficiência das operações subsequentes, mas ela traz um custo perceptível em termos de desempenho conforme a estrutura cresce. O rebalanceamento é acionado nos seguintes casos:

* Um nó preto é removido: A eliminação de um nó preto pode violar a propriedade de "caminhos com igual número de nós pretos" ;  
* O nó removido tem um filho vermelho: Se o nó substituto (que ocupa o lugar do removido) for vermelho, ele deve ser repintado de preto para evitar violações (dois nós vermelhos consecutivos).  
* Casos específicos de desbalanceamento: Após a remoção, a árvore pode entrar em um dos 6 casos de rebalanceamento (definidos no algoritmo de Cormen et al.), que envolvem rotações e ajustes de cores.

---
**Busca**  

![image](https://github.com/user-attachments/assets/ceadd268-a625-4863-a406-bc19860428c1)


**LSM-Tree**

Esse comportamento está diretamente relacionado ao funcionamento interno da operação de busca na LSM-tree. Quando uma chave é procurada, o algoritmo segue uma ordem específica: primeiro busca na **Memtable mutável** (em memória), depois nas **Memtables imutáveis** (buffers em memória aguardando serem gravados no disco) e, por fim, nas **SSTables**, que são arquivos organizados por níveis e armazenados em disco. A busca na Memtable mutável costuma ser rápida, pois é realizada em uma árvore AVL em memória. No entanto, se a chave não estiver presente nela, a busca precisa continuar nas **Memtables** imutáveis e depois nas **SSTables**, o que aumenta consideravelmente o tempo de acesso.

As **Memtables** imutáveis também usam árvores em memória, mas como são múltiplas, é necessário percorrer todas até encontrar a chave ou garantir que ela não existe. Já as **SSTables** no disco apresentam o maior custo de busca. Apesar de utilizarem ***Bloom filters*** e índices esparsos para reduzir o escopo da leitura, a operação ainda envolve acessos ao disco e ***comparações*** byte a byte para encontrar a chave exata, o que torna o processo mais lento.

Com o crescimento do número de elementos, mais **SSTables** são geradas e, consequentemente, mais leituras precisam ser feitas durante a busca. Isso explica o crescimento acentuado da curva da **LSM-tree** no gráfico.

Portanto, embora a **LSM-tree** seja muito eficiente para operações de escrita, ela apresenta um desempenho inferior em buscas quando comparada com outras estruturas tradicionais, principalmente em cenários com grande volume de dados.

**Btree**

A Btree se destaca no cenário de busca, principalmente porque foi projetada para tornar esse processo mais eficiente, especialmente em sistemas que lidam com grandes volumes de dados. Sua principal vantagem está no fato de manter a altura da árvore controlada, sempre **O(log n)**, independentemente do número de elementos. Isso significa que, conforme a quantidade de dados cresce, o tempo necessário para realizar uma busca não aumenta de forma exponencial, mas sim de maneira logarítmica, tornando as operações muito mais rápidas, mesmo com grandes volumes de dados.

Além disso, os nós da B-Tree são sempre ordenados, o que permite que uma **busca binária** seja realizada de maneira eficiente dentro de cada nó. Isso reduz significativamente o número de comparações necessárias para localizar um elemento, tornando a operação de busca mais ágil. A combinação desses fatores, a altura controlada e a busca binária interna, contribui para uma redução considerável na quantidade de **acessos à memória**, especialmente em sistemas que utilizam memória secundária, como discos rígidos. A B-Tree minimiza a necessidade de acessar o disco, o que é crucial para sistemas de banco de dados e arquivos, onde o custo de cada leitura de disco é muito mais alto do que o acesso à memória RAM.

**TreeMap**

O TreeMap em Java oferece garantias de desempenho de O(log n) para operações de busca, já que a Red-Black Tree mantém o balanceamento da árvore após cada inserção ou remoção. Essa árvore assegura que o caminho mais longo da raiz a qualquer folha seja no máximo o dobro do caminho mais curto, evitando o crescimento descontrolado da altura.

Apesar disso, cada operação de busca ainda percorre um caminho da raiz até uma folha, realizando comparações de chaves em cada nó. Em aplicações puramente em memória, essas comparações são rápidas, mas se tornam perceptíveis conforme a profundidade da árvore aumenta, especialmente com grandes volumes de dados, o que explica a curva crescente do tempo de busca no gráfico. Portanto,a **TreeMap é eficiente e previsível em termos de tempo de busca**, mantendo bom desempenho mesmo com o aumento dos dados, graças ao balanceamento automático da Red-Black Tree. Ainda assim, sua estrutura binária impõe limites à escalabilidade da busca quando comparada a árvores como a B-tree, que são projetadas para minimizar profundidade e custo de acesso — principalmente em contextos com grandes volumes de dados ou armazenamento secundário (como bancos de dados).

O TreeMap, no entanto, **oferece vantagens práticas em muitos cenários típicos de aplicações Java**, especialmente quando os dados residem inteiramente em memória e a ordenação natural das chaves é necessária. Seu tempo de busca é estável, mesmo com crescimento exponencial no número de elementos, como evidenciado pela curva suave e contínua no gráfico.

---
### Conclusão da análise**

* **Então, qual é a melhor?**

	Simples: depende\!

Quando a gente compara **LSM-Tree, B-Tree e TreeMap**, a escolha certa vai depender muito do tipo de sistema que está sendo construído e do que ele mais precisa: desempenho em escrita, leitura rápida, ordenação em tempo real, ou uma combinação disso tudo. 

A **LSM-Tree**, por exemplo, é super usada em bancos de dados modernos que precisam lidar com muita escrita o tempo todo, como o Cassandra, RocksDB e LevelDB. Esses sistemas aparecem em cenários como o Apache Kafka (armazenando offsets e metadados), os sistemas internos do Facebook (Meta) (como feeds e mensagens), e até no Google Bigtable, que gerencia quantidades absurdas de dados. O que faz a LSM-Tree funcionar bem nesses casos é justamente sua forma eficiente de gravar dados no disco e organizar tudo depois com compactações periódicas — isso permite manter um desempenho estável mesmo com uma enxurrada de inserções.

Já a **B-Tree** continua firme e forte nos sistemas mais tradicionais, principalmente quando leitura e escrita acontecem com frequência e precisam de respostas rápidas e ordenadas. Ela é a base de muitos bancos relacionais como PostgreSQL, MySQL e Oracle, onde serve para criar índices que aceleram buscas. Um bom exemplo disso são os sistemas de e-commerce, onde a gente precisa filtrar produtos por preço, nome, data, etc. — tudo isso funciona bem com B-Trees, que conseguem manter o desempenho mesmo com muito dado e atualizações constantes.

Por fim, o **TreeMap** é uma estrutura pensada para trabalhar em memória, principalmente em aplicações Java que precisam manter os dados sempre ordenados e acessíveis de forma eficiente. Ele é ótimo quando a gente precisa navegar pelos elementos em tempo real ou acessar as menores ou maiores chaves com facilidade. É comum encontrar TreeMap em situações como caches ordenados, rankings dinâmicos, e até sistemas de agendamento de tarefas. Um exemplo prático: num sistema de leilão em tempo real, o TreeMap pode organizar os lances e permitir que o sistema saiba imediatamente quem fez o maior lance ou quem está logo abaixo — tudo isso sem precisar percorrer a estrutura inteira.

Resumindo: **LSM-Tree** é ideal quando o foco é escrita intensa e grande volume de dados, especialmente em aplicações distribuídas e orientadas a logs. **B-Tree** é a melhor opção para quando precisamos de consultas rápidas e estruturadas, comuns em bancos relacionais. E o **TreeMap** é perfeito para quando os dados estão em memória e a ordenação precisa ser mantida o tempo todo, como em lógica de negócio mais dinâmica. A decisão final depende de entender bem o que o sistema precisa fazer e escolher a estrutura que mais combina com esse cenário.
