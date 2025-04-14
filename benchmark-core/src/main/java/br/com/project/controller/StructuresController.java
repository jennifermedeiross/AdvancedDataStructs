package br.com.project.controller;

import br.com.project.entities.Pessoa;
import br.com.project.structs.lsm.tree.LSMTree;
import br.com.project.structs.btree.*;
import br.com.project.structs.lsm.types.ByteArrayPair;
import br.com.project.structs.TreeMap;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class StructuresController {
    private LSMTree lsmTree;
    private BTree bTree;
    private TreeMap treeMap;

    public StructuresController() {
        this.lsmTree = new LSMTree();
        this.bTree = new BTree(1000);
        this.treeMap = new TreeMap<String, Pessoa>();
    }

    private String randomCpf(Pessoa[] pessoas) {
        Random rand = new Random();
        return pessoas[rand.nextInt(pessoas.length)].getCpf();
    }

    public void insereBtree(Pessoa[] pessoas) {
        long startTime, endTime, duration;
        for (Pessoa pessoa : pessoas) {

            startTime = System.nanoTime();

            bTree.add(pessoa);

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("Btree " + (duration) + " " + pessoas.length);
        }
    }

    public void buscaBtree(Pessoa[] pessoas) {
        long startTime, endTime, duration;
        for (Pessoa pessoa : pessoas) {

            startTime = System.nanoTime();

            bTree.search(randomCpf(pessoas));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("Btree " + (duration) + " " + pessoas.length);
        }
    }

    public void removeBtree(Pessoa[] pessoas) {
        long startTime, endTime, duration;
        for (Pessoa pessoa : pessoas) {

            startTime = System.nanoTime();

            bTree.delete(randomCpf(pessoas));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("Btree " + (duration) + " " + pessoas.length);
        }
    }

    public void insereLSM(Pessoa[] pessoas) {
        long startTime, endTime, duration;
        for (Pessoa pessoa : pessoas) {
            byte[] cpfBytes = pessoa.getCpf().getBytes(StandardCharsets.UTF_8);
            byte[] pessoaBytes = pessoa.toJson().getBytes(StandardCharsets.UTF_8);

            startTime = System.nanoTime();

            // lsmTree.add(new ByteArrayPair(cpfBytes, pessoaBytes));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("LSM " + (duration) + " " + pessoas.length);
        }
    }

    public void removeLSM(Pessoa[] pessoas) {
        long startTime, endTime, duration;
        for (int i = 0; i < pessoas.length; i++) {
            startTime = System.nanoTime();

            // lsmTree.delete(randomCpf(pessoas).getBytes(StandardCharsets.UTF_8));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("LSM " + (duration) + " " + pessoas.length);
        }
    }

    public void buscaLSM(Pessoa[] pessoas) {
        long startTime, endTime, duration;

        for (Pessoa pessoa : pessoas) {
            startTime = System.nanoTime();

            // lsmTree.get(pessoa.getCpf().getBytes(StandardCharsets.UTF_8));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("LSM " + (duration) + " " + pessoas.length);
        }
    }

    public void insereTreeMap(Pessoa[] pessoas){
        long startTime, endTime, duration;
        for (Pessoa pessoa : pessoas) {

            startTime = System.nanoTime();

            treeMap.put(pessoa.getCpf(),pessoa);
            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("TreeMap " + (duration) + " " + pessoas.length);

        }

    }

    public void buscaTreeMap(Pessoa[] pessoas){
        long startTime, endTime, duration;
        for (Pessoa pessoa : pessoas) {

            startTime = System.nanoTime();
            treeMap.get(randomCpf(pessoas));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("TreeMap " + (duration) + " " + pessoas.length);

         }
    }

    public void removeTreeMap(Pessoa[] pessoas) {
        long startTime, endTime, duration;
        for (Pessoa pessoa : pessoas) {

            startTime = System.nanoTime();
            treeMap.delete(randomCpf(pessoas));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("TreeMap " + (duration) + " " + pessoas.length);
        }
    }
}
