package br.com.project.structs.lsm;

import br.com.project.entities.Pessoa;
import br.com.project.service.LeitorDeDados;
import br.com.project.structs.lsm.tree.LSMTree;
import br.com.project.structs.lsm.types.ByteArrayPair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class Main {
    static final String DIRECTORY = "LSM-data";

    public static void main(String[] args) {
        try {
            // 1. Ler os dados do arquivo JSON
            List<Pessoa> pessoas = List.of(LeitorDeDados.readJson(Pessoa[].class));
            System.out.println("Total de registros carregados: " + pessoas.size());

            // 2. Criar a LSM-Tree
            LSMTree lsmTree = new LSMTree();
            System.out.println("LSM-Tree inicializada.");

            // 3. Inserir dados na LSM-Tree
            for (Pessoa pessoa : pessoas) {
                byte[] cpfBytes = pessoa.getCpf().getBytes(StandardCharsets.UTF_8);
                byte[] pessoaBytes = pessoa.toJson().getBytes(StandardCharsets.UTF_8);
                lsmTree.add(new ByteArrayPair(cpfBytes, pessoaBytes));
            }
            System.out.println("Dados inseridos na LSM-Tree.");

            // 4. Verificar se o flush aconteceu
            File pastaSST = new File(lsmTree.dataDir);
            File[] arquivos = pastaSST.listFiles();
            if (arquivos != null && arquivos.length > 0) {
                System.out.println("[OK] Memtable foi flushada para SSTable.");
            } else {
                System.out.println("[ERRO] Nenhum arquivo SSTable foi gerado!");
            }

            // 5. Teste de busca imediata após inserção
            Pessoa pessoaTeste = pessoas.get(0);
            byte[] cpfTesteBytes = pessoaTeste.getCpf().getBytes(StandardCharsets.UTF_8);
            byte[] resultadoTeste = lsmTree.get(cpfTesteBytes);

            if (resultadoTeste != null) {
                System.out.println("[OK] Busca imediata funcionou para CPF: " + pessoaTeste.getCpf());
            } else {
                System.out.println("[ERRO] Busca falhou para CPF: " + pessoaTeste.getCpf());
            }

            // 6. Teste de busca aleatória
            Random rand = new Random();
            for (int i = 0; i < 5; i++) {
                Pessoa pessoa = pessoas.get(rand.nextInt(pessoas.size()));
                byte[] cpfBytes = pessoa.getCpf().getBytes(StandardCharsets.UTF_8);

                byte[] resultadoBytes = lsmTree.get(cpfBytes);
                if (resultadoBytes != null) {
                    Pessoa resultado = Pessoa.fromJson(new String(resultadoBytes, StandardCharsets.UTF_8));
                    System.out.println("[OK] Encontrado: " + resultado.getNome() + " (CPF: " + pessoa.getCpf() + ")");
                    System.out.println("[OBJETO]" + resultado);
                } else {
                    System.out.println("[ERRO] CPF não encontrado: " + pessoa.getCpf());
                }
            }

            // 7. Teste de remoção
            Pessoa pessoaRemover = pessoas.get(0);
            byte[] cpfRemoverBytes = pessoaRemover.getCpf().getBytes(StandardCharsets.UTF_8);
            lsmTree.delete(cpfRemoverBytes);
            byte[] resultadoAposRemocao = lsmTree.get(cpfRemoverBytes);

            if (resultadoAposRemocao == null) {
                System.out.println("[OK] Remoção confirmada para CPF: " + pessoaRemover.getCpf());
                System.out.println("[OBJETO]" + pessoaRemover);
            } else {
                System.out.println("[ERRO] Falha na remoção do CPF: " + pessoaRemover.getCpf());
            }

            System.out.println("Teste concluído.");

        } catch (IOException e) {
            System.err.println("Erro ao ler o JSON: " + e.getMessage());
        }
    }
}
