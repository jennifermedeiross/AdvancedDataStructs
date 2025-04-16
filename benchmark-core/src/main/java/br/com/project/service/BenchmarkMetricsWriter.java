package br.com.project.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BenchmarkMetricsWriter {
    private boolean headerWritten = false;
    private final String directoryPath;

    public BenchmarkMetricsWriter(String directoryPath) {
        this.directoryPath = directoryPath;

        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

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


    public void append(String fileName, Object... values) {
        File file = new File(directoryPath, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (!headerWritten && file.length() == 0) {
                throw new IllegalStateException("Cabeçalho não escrito. Use write() antes de append().");
            }

            String line = join(values);
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever dados: " + e.getMessage(), e);
        }
    }

    private String join(Object... values) {
        return String.join(",",
                java.util.Arrays.stream(values)
                        .map(Object::toString)
                        .toArray(String[]::new)
        );
    }
}
