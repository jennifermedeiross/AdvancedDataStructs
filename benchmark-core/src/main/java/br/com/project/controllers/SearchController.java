package br.com.project.controllers;

import br.com.project.entities.Pessoa;
import br.com.project.service.BenchmarkMetricsWriter;
import br.com.project.structs.treeMap.TreeMap;
import br.com.project.structs.btree.BTree;
import br.com.project.structs.lsm.tree.LSMTree;

import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SearchController {
    private BenchmarkMetricsWriter writer;
    private LSMTree<String, Pessoa> lsmTree;
    private TreeMap<String, Pessoa> treeMap;
    private BTree bTree;

    public SearchController(){
        writer = new BenchmarkMetricsWriter("results");
        writer.write("search.csv", "Estrutura", "Carga", "Operação", "Tempo (ms)", "Memória (KB)");
    }

    private void warmUp(Pessoa[] pessoas, String estrutura) throws JsonProcessingException {
        switch (estrutura) {
            case "LSMTree":
                this.lsmTree = new LSMTree<>();
                for (Pessoa pessoa : pessoas) {
                    lsmTree.add(pessoa.getCpf(), pessoa);
                }
                break;
            case "BTree":
                this.bTree = new BTree(1000);
                for (Pessoa pessoa : pessoas) {
                    bTree.add(pessoa);
                }
                break;
            case "TreeMap":
                this.treeMap = new TreeMap<>();
                for (Pessoa pessoa : pessoas) {
                    treeMap.put(pessoa.getCpf(), pessoa);
                }
                break;
        }

        for (Pessoa pessoa : pessoas) {
            switch (estrutura) {
                case "LSMTree":
                    lsmTree.get(pessoa.getCpf());
                    break;
                case "BTree":
                    bTree.search(pessoa.getCpf());
                    break;
                case "TreeMap":
                    treeMap.get(pessoa.getCpf());
                    break;
            }
        }
    }

    private void benchmarkSearch(Pessoa[] pessoas, String estrutura) throws JsonProcessingException {
        warmUp(pessoas, estrutura);

        Runtime runtime = Runtime.getRuntime();

        switch (estrutura) {
            case "LSMTree":
                this.lsmTree = new LSMTree<>();
                break;
            case "BTree":
                this.bTree = new BTree(1000);
                break;
            case "TreeMap":
                this.treeMap = new TreeMap<>();
                break;
        }

        for (Pessoa pessoa : pessoas) {
            switch (estrutura) {
                case "LSMTree":
                    lsmTree.add(pessoa.getCpf(), pessoa);
                    break;
                case "BTree":
                    bTree.add(pessoa);
                    break;
                case "TreeMap":
                    treeMap.put(pessoa.getCpf(), pessoa);
                    break;
            }
        }

        int repeticoes = 10;

        for (int i = 0; i < pessoas.length; i++) {
            double somaTempo = 0;
            double somaMemoria = 0;

            for (int j = 0; j < repeticoes; j++) {
                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                long startTime = System.nanoTime();

                switch (estrutura) {
                    case "LSMTree":
                        lsmTree.get(pessoas[i].getCpf());
                        break;
                    case "BTree":
                        bTree.search(pessoas[i].getCpf());
                        break;
                    case "TreeMap":
                        treeMap.get(pessoas[i].getCpf());
                        break;
                }

                long endTime = System.nanoTime();
                long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();

                double durationMs = (endTime - startTime) / 1_000_000.0;
                double memoryKB = (usedMemoryAfter - usedMemoryBefore) / 1024.0;

                somaTempo += durationMs;
                somaMemoria += memoryKB;
            }

            double mediaTempo = somaTempo / repeticoes;
            double mediaMemoria = somaMemoria / repeticoes;

            writer.append(
                    "search.csv",
                    estrutura,
                    pessoas.length,
                    "busca",
                    String.format(Locale.US,"%.3f", mediaTempo),
                    String.format(Locale.US,"%.2f", mediaMemoria)
            );
        }
    }

    public void searchLsm(Pessoa[] pessoas) throws JsonProcessingException {
        benchmarkSearch(pessoas, "LSMTree");
    }

    public void searchBTree(Pessoa[] pessoas) throws JsonProcessingException {
        benchmarkSearch(pessoas, "BTree");
    }

    public void searchTreeMap(Pessoa[] pessoas) throws JsonProcessingException {
        benchmarkSearch(pessoas, "TreeMap");
    }
}
