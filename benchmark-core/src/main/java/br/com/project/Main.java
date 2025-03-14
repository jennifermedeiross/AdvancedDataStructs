package br.com.project;

import br.com.project.controller.StructuresController;
import br.com.project.entities.Pessoa;
import br.com.project.service.LeitorDeDados;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            int[] quantidades = new int[]{1000, 5000, 10000};
            StructuresController sc = new StructuresController();
            Pessoa[] pessoas;

            for (int qnt : quantidades) {
                pessoas = LeitorDeDados.readJson(Pessoa[].class, "dados-" + qnt + ".json");
                sc.removeTreeMap(pessoas);
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo JSON: " + e.getMessage());
        }

    }
}