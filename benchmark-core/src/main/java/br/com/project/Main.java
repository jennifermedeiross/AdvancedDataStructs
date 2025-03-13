package br.com.project;

import br.com.project.entities.Pessoa;
import br.com.project.service.LeitorDeDados;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            Pessoa[] pessoas = LeitorDeDados.readJson(Pessoa[].class);
            System.out.println(Arrays.toString(pessoas));

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo JSON: " + e.getMessage());
        }

    }
}