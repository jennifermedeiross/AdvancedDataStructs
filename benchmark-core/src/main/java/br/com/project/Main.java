package br.com.project;

import br.com.project.controllers.StructuresController;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        StructuresController sc = new StructuresController();
        sc.initAnalysis();
        System.out.println("Métricas geradas com sucesso!");
    }

}