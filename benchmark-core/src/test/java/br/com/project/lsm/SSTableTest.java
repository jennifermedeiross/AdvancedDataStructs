package br.com.project.lsm;

import br.com.project.entities.Pessoa;
import br.com.project.structs.lsm.serialization.ObjectSerializer;
import br.com.project.structs.lsm.sstable.SSTable;
import br.com.project.structs.lsm.types.ByteArrayPair;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SSTableTest {
    private static final Path dataDir = Paths.get(System.getProperty("user.dir"), "src", "test", "java", "br", "com", "project", "lsm", "data");
    private static SSTable sstable;

    @BeforeEach
    void setup() throws IOException {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        Pessoa[] pessoas = new Pessoa[] {
                new Pessoa("Maria Souza", "123.456.789-00", 30, "+55 83 9 9999-9999", "10/10/1994"),
                new Pessoa("João Silva", "111.111.111-11", 25, "+55 83 9 8888-8888", "15/05/1999"),
                new Pessoa("Ana Lima", "222.222.222-22", 40, "+55 83 9 7777-7777", "22/12/1984"),
                new Pessoa("Carlos Pinto", "333.333.333-33", 29, "+55 83 9 6666-6666", "03/03/1995"),
                new Pessoa("Fernanda Rocha", "444.444.444-44", 35, "+55 83 9 5555-5555", "28/07/1989")
        };

        List<ByteArrayPair> data = new ArrayList<>();
        for (Pessoa p : pessoas) {
            data.add(new ByteArrayPair(p.getCpf().getBytes(), p.toString().getBytes()));
        }

        sstable = new SSTable(dataDir.toString(), data.iterator());
    }

    @AfterAll
    static void cleanup() throws IOException {
        if (Files.exists(dataDir)) {
            Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path arquivo, BasicFileAttributes atributos) throws IOException {
                    Files.delete(arquivo);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path diretorio, IOException erro) throws IOException {
                    Files.delete(diretorio);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Test
    void testGet() {
        Pessoa pessoa = new Pessoa("Maria Souza", "123.456.789-00", 30, "+55 83 9 9999-9999", "10/10/1994");
        assertArrayEquals(pessoa.toString().getBytes(), sstable.get("123.456.789-00".getBytes()));

        assertNull(sstable.get("000.000.000-00".getBytes()));
    }

    @Test
    void testWriteAndRead() throws IOException {
        // Verifica se os arquivos foram escritos
        try (Stream<Path> stream = Files.list(dataDir)) {
            long count = stream.count();
            assertNotEquals(0, count);
        }

        // Testa leitura dos dados
        Pessoa pessoa = new Pessoa("Maria Souza", "123.456.789-00", 30, "+55 83 9 9999-9999", "10/10/1994");
        assertArrayEquals(pessoa.toString().getBytes(), sstable.get("123.456.789-00".getBytes()));

        // Testa a leitura de chave INEXISTENTE
        assertNull(sstable.get("000.000.000-00".getBytes()));

        // Testa iteração sobre os dados
        List<ByteArrayPair> readItems = new ArrayList<>();
        sstable.iterator().forEachRemaining(readItems::add);
        assertEquals(5, readItems.size(), "Deve ter 5 entradas na SSTable");

        // Testa o fechamento do SSTable
        sstable.closeAndDelete();
    }

    @Test
    void testEmptySSTable() {
        Iterator<ByteArrayPair> emptyIterator = Collections.emptyIterator();
        assertThrows(IllegalArgumentException.class, () -> new SSTable(dataDir.toString(), emptyIterator));
    }
}
