package br.com.project.controller;

import br.com.project.entities.Pessoa;
import br.com.project.structs.chordDHT.ChordDHT;
import br.com.project.structs.lsm.tree.LSMTree;
import br.com.project.structs.lsm.types.ByteArrayPair;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class StructuresController {
    private LSMTree lsmTree;

    public StructuresController() {
        this.lsmTree = new LSMTree();
    }

    private String randomCpf(Pessoa[] pessoas){
        Random rand = new Random();
        return pessoas[rand.nextInt(pessoas.length)].getCpf();
    }

    public void insereLSM(Pessoa[] pessoas) {
        long startTime, endTime, duration;
        for (Pessoa pessoa : pessoas) {
            byte[] cpfBytes = pessoa.getCpf().getBytes(StandardCharsets.UTF_8);
            byte[] pessoaBytes = pessoa.toJson().getBytes(StandardCharsets.UTF_8);

            startTime = System.nanoTime();

            lsmTree.add(new ByteArrayPair(cpfBytes, pessoaBytes));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("LSM " + (duration) + " " + pessoas.length);
        }
    }

    public void removeLSM(Pessoa[] pessoas) {
        long startTime, endTime, duration;
        for (int i = 0; i < pessoas.length; i++) {
            startTime = System.nanoTime();

            lsmTree.delete(randomCpf(pessoas).getBytes(StandardCharsets.UTF_8));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("LSM " + (duration) + " " + pessoas.length);
        }
    }

    public void buscaLSM(Pessoa[] pessoas) {
        long startTime, endTime, duration;

        for (Pessoa pessoa : pessoas) {
            startTime = System.nanoTime();

            lsmTree.get(pessoa.getCpf().getBytes(StandardCharsets.UTF_8));

            endTime = System.nanoTime();
            duration = endTime - startTime;

            System.out.println("LSM " + (duration) + " " + pessoas.length);
        }
    }
}
