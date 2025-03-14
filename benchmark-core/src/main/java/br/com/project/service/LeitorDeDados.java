package br.com.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/*
 * Classe que lê o arquivo de dados.json e mapeia o objeto
 */
public class LeitorDeDados {

    /**
     * Lê um arquivo JSON e converte o conteúdo em um objeto da classe T.
     *
     * @param classReturn Classe do objeto a ser retornado.
     * @param <T> Tipo genérico do objeto.
     * @return Objeto do tipo T com os dados do JSON.
     * @throws IOException Se houver erro na leitura do arquivo.
     */
    public static <T> T readJson(Class<T> classReturn) throws IOException {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        System.err.println(currentDir);
        File filePath = new File(currentDir + "/benchmark-core/src/main/java/br/com/project/data/dados.json");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(filePath, classReturn);
    }
}
