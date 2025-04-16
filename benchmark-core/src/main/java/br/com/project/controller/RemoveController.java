package br.com.project.controller;

import br.com.project.entities.Pessoa;
import br.com.project.service.BenchmarkMetricsWriter;
import br.com.project.structs.treeMap.TreeMap;
import br.com.project.structs.btree.BTree;
import br.com.project.structs.lsm.tree.LSMTree;
import com.fasterxml.jackson.core.JsonProcessingException;

public class RemoveController {
    private BenchmarkMetricsWriter writer;
    private LSMTree<String, Pessoa> lsmTree;
    private TreeMap<String, Pessoa> treeMap;
    private BTree bTree;

    public RemoveController(){
        writer = new BenchmarkMetricsWriter("results");
        writer.write("remove.csv", "Estrutura", "Carga", "Operação", "Tempo (ms)", "Memória (KB)");
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

    private void benchmarkDelete(Pessoa[] pessoas, String estrutura) throws JsonProcessingException, InterruptedException {
        warmUp(pessoas, estrutura);

        Runtime runtime = Runtime.getRuntime();

        // Reinicializa as estruturas
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

        System.gc();
        Thread.sleep(100);
        int repeticoes = 100;

        for (int i = 0; i < pessoas.length; i++) {
            double somaTempo = 0;
            double somaMemoria = 0;

            for (int j = 0; j < repeticoes; j++) {
                // Reinsere a pessoa removida para repetir o teste
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

                long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                long startTime = System.nanoTime();

                switch (estrutura) {
                    case "LSMTree":
                        lsmTree.delete(pessoas[i].getCpf());
                        break;
                    case "BTree":
                        bTree.delete(pessoas[i].getCpf());
                        break;
                    case "TreeMap":
                        treeMap.delete(pessoas[i].getCpf());
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
                    "remove.csv",
                    estrutura,
                    i + 1,
                    "remocao",
                    String.format("%.3f", mediaTempo),
                    String.format("%.2f", mediaMemoria)
            );
        }
    }

    public void removeLsm(Pessoa[] pessoas) throws JsonProcessingException, InterruptedException {
        benchmarkDelete(pessoas, "LSMTree");
    }

    public void removeBTree(Pessoa[] pessoas) throws JsonProcessingException, InterruptedException {
        benchmarkDelete(pessoas, "BTree");
    }

    public void removeTreeMap(Pessoa[] pessoas) throws JsonProcessingException, InterruptedException {
        benchmarkDelete(pessoas, "TreeMap");
    }
}
