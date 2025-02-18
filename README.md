# AdvancedDataStructs
Repositório do projeto da disciplina de Laboratório de Estrutura de Dados com foco em analisar estruturas avançadas de dados.

## Após clonar a primeira vez rodar esse comando abaixo.

```sh
git config core.hooksPath githooks/
```

## Comandos do Gradle
Este projeto utiliza **Gradle** como ferramenta de automação de build. Abaixo estão alguns comandos essenciais para gerenciar o projeto:

### **Compilar o projeto**
Compila o código-fonte e gera os arquivos necessários:
```sh
./gradlew build
```

### **Executar a aplicação**
Se o projeto tiver uma classe principal, você pode executá-lo com:
```sh
./gradlew run
```

### **Rodar os testes**
Executa os testes automatizados para validar o funcionamento do código:
```sh
./gradlew test
```

### **Limpar arquivos gerados**
Remove os arquivos de build e recompila do zero:
```sh
./gradlew clean
```

### **Compilar e reconstruir do zero**
Executa um `clean` seguido de um `build` para garantir que tudo seja gerado novamente:
```sh
./gradlew clean build
```

### **Ver dependências do projeto**
Caso queira visualizar as dependências utilizadas no projeto:
```sh
./gradlew dependencies
```

### **Atualizar dependências**
Baixa e aplica as versões mais recentes das dependências especificadas:
```sh
./gradlew build --refresh-dependencies
```

## Geração de Dados
A geração de dados usa a biblioteca [Faker](https://faker.readthedocs.io/en/master/) do Python.

### Como gerar
No diretório `/data-generator`, execute:
```shell
chmod +x generate.sh
./generate.sh
```
O script instalará as dependências e gerará os dados automaticamente.