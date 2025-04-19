package br.com.project.controller;

import br.com.project.entities.Pessoa;
import br.com.project.service.BenchmarkMetricsWriter;
import br.com.project.structs.treeMap.TreeMap;
import br.com.project.structs.btree.BTree;
import br.com.project.structs.lsm.tree.LSMTree;

import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;

public class InsertController {
    private BenchmarkMetricsWriter writer;
    private LSMTree<String, Pessoa> lsmTree;
    private TreeMap<String, Pessoa> treeMap;
    private BTree bTree;
    
    public InsertController(){
        writer = new BenchmarkMetricsWriter("results");
        writer.write("insert.csv","Estrutura", "Carga", "Operação", "Tempo (ms)", "Memória (KB)");
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

    private void benchmarkInsert(Pessoa[] pessoas, String estrutura) throws JsonProcessingException {
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

        for (int i = 0; i < pessoas.length; i++) {
            long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
            long startTime = System.nanoTime();

            switch (estrutura) {
                case "LSMTree":
                    lsmTree.add(pessoas[i].getCpf(), pessoas[i]);
                    break;
                case "BTree":
                    bTree.add(pessoas[i]);
                    break;
                case "TreeMap":
                    treeMap.put(pessoas[i].getCpf(), pessoas[i]);
                    break;
            }

            long endTime = System.nanoTime();
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();

            double durationMs = (endTime - startTime) / 1_000_000.0;
            double memoryKB = (usedMemoryAfter - usedMemoryBefore) / 1024.0;

            writer.append(
                    "insert.csv",
                    estrutura,
                    pessoas.length,
                    "inserção",
                    String.format(Locale.US,"%.3f", durationMs),
                    String.format(Locale.US,"%.2f", memoryKB)
            );
        }
    }

    public void insertLsm(Pessoa[] pessoas) throws JsonProcessingException {
        benchmarkInsert(pessoas, "LSMTree");
    }

    public void insertBTree(Pessoa[] pessoas) throws JsonProcessingException {
        benchmarkInsert(pessoas, "BTree");
    }

    public void insertTreeMap(Pessoa[] pessoas) throws JsonProcessingException {
        benchmarkInsert(pessoas, "TreeMap");
    }
}
