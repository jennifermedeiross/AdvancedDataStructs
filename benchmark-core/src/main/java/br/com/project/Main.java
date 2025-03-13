package br.com.project;

import br.com.project.entities.Pessoa;
import br.com.project.service.LeitorDeDados;
import br.com.project.structs.lsm.tree.LSMTree;
import br.com.project.structs.lsm.types.ByteArrayPair;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// ajustar delete

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            // 1. Carregar os dados do arquivo JSON usando LeitorDeDados
            List<Pessoa> pessoas = List.of(LeitorDeDados.readJson(Pessoa[].class));

            LSMTree lsmTree = new LSMTree(1024 * 1024, 10, "data_dir");

            for (Pessoa pessoa : pessoas) {
                ByteArrayPair item = new ByteArrayPair(
                        pessoa.getCpf().getBytes(StandardCharsets.UTF_8),
                        pessoa.toBytes()
                );
                lsmTree.add(item);
            }

            // 3. Testar busca de um CPF específico
            String cpfTeste = pessoas.get(0).getCpf();
            byte[] resultadoBytes = lsmTree.get(cpfTeste.getBytes(StandardCharsets.UTF_8));
            Pessoa resultado = Pessoa.fromBytes(resultadoBytes);

            if (resultado != null) {
                System.out.println("Pessoa encontrada: " + resultado.getNome());
            } else {
                System.out.println("Pessoa não encontrada!");
            }

            lsmTree.delete(cpfTeste.getBytes(StandardCharsets.UTF_8));

            byte[] resultadoAposRemocao = lsmTree.get(cpfTeste.getBytes(StandardCharsets.UTF_8));
            if (resultadoAposRemocao == null) {
                System.out.println("Remoção confirmada, CPF não encontrado!");
            } else {
                System.out.println("Falha na remoção.");
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo JSON: " + e.getMessage());
        }

    }
}
