package br.com.project.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe responsável por escrever métricas de benchmark em arquivos CSV.
 */
public class BenchmarkMetricsWriter {
    private boolean headerWritten = false;
    private final String directoryPath;

    /**
     * Construtor que define o diretório de saída e cria-o caso não exista.
     *
     * @param directoryPath Caminho do diretório onde os arquivos serão escritos.
     */
    public BenchmarkMetricsWriter(String directoryPath) {
        this.directoryPath = directoryPath;

        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Escreve o cabeçalho no arquivo especificado.
     * Se o arquivo já existir, assume que o cabeçalho já foi escrito.
     *
     * @param fileName Nome do arquivo.
     * @param headers  Valores do cabeçalho a serem escritos.
     */
    public void write(String fileName, Object... headers) {
        File file = new File(directoryPath, fileName);

        if (file.exists()) {
            headerWritten = true;
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String line = join(headers);
            writer.write(line);
            writer.newLine();
            headerWritten = true;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever cabeçalho: " + e.getMessage(), e);
        }
    }

    /**
     * Adiciona uma linha de dados ao arquivo.
     * O cabeçalho deve ter sido escrito previamente.
     *
     * @param fileName Nome do arquivo.
     * @param values   Valores a serem adicionados como linha de dados.
     */
    public void append(String fileName, Object... values) {
        File file = new File(directoryPath, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (!headerWritten && file.length() == 0) {
                throw new IllegalStateException("Cabeçalho não escrito. Usar write() antes de append().");
            }

            String line = join(values);
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever dados: " + e.getMessage(), e);
        }
    }

    /**
     * Concatena os valores passados em uma única linha separada por vírgulas.
     *
     * @param values Valores a serem unidos.
     * @return String formatada com os valores separados por vírgula.
     */
    private String join(Object... values) {
        return String.join(",",
                java.util.Arrays.stream(values)
                        .map(Object::toString)
                        .toArray(String[]::new)
        );
    }
}
