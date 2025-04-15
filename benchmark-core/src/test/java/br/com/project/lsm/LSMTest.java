package br.com.project.lsm;

import br.com.project.entities.Pessoa;
import br.com.project.structs.lsm.serialization.ObjectSerializer;
import br.com.project.structs.lsm.tree.LSMTree;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LSMTest {
    private static LSMTree<String, Pessoa> lsm;
    private static final Path dataDir = Paths.get(System.getProperty("user.dir"), "src", "test", "java", "br", "com", "project", "lsm", "data");

    @BeforeEach
    void setup(){
        lsm = new LSMTree<>(dataDir.toString(), 300, 2, 1.75, 10, 50);
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
    void testAdd() throws IOException {
        Pessoa[] pessoas = new Pessoa[] {
                new Pessoa("Maria Souza", "123.456.789-00", 30, "+55 83 9 9999-9999", "10/10/1994"),
                new Pessoa("João Silva", "111.111.111-11", 25, "+55 83 9 8888-8888", "15/05/1999"),
                new Pessoa("Ana Lima", "222.222.222-22", 40, "+55 83 9 7777-7777", "22/12/1984"),
                new Pessoa("Carlos Pinto", "333.333.333-33", 29, "+55 83 9 6666-6666", "03/03/1995"),
                new Pessoa("Fernanda Rocha", "444.444.444-44", 35, "+55 83 9 5555-5555", "28/07/1989"),
                new Pessoa("Lucas Andrade", "555.555.555-55", 22, "+55 83 9 4444-4444", "01/01/2003"),
                new Pessoa("Juliana Alves", "666.666.666-66", 27, "+55 83 9 3333-3333", "19/09/1997"),
                new Pessoa("Pedro Melo", "777.777.777-77", 32, "+55 83 9 2222-2222", "11/11/1992"),
                new Pessoa("Camila Teixeira", "888.888.888-88", 38, "+55 83 9 1111-1111", "07/04/1986"),
                new Pessoa("Rafael Costa", "999.999.999-99", 45, "+55 83 9 0000-0000", "30/06/1979")
        };

        for(Pessoa p: pessoas){
            lsm.add(p.getCpf(), p);
        }

        // Deve ter criado a pasta
        assertTrue(Files.exists(dataDir));

        // e ela ainda deve estar vazia, pois o flush é de tempos em tempos
        try (Stream<Path> stream = Files.list(dataDir)) {
            long count = stream.count();
            assertEquals(0, count);
        }

        // esperando para que o temporizador chame o flush e o levelCompaction
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // agora deve ter acontecido a persistência das sstables no disco
        try (Stream<Path> stream = Files.list(dataDir)) {
            long count = stream.count();
            assertNotEquals(0, count);
        }
    }

    @Test
    void testSearch() throws IOException {
        Pessoa[] pessoasPresentes = new Pessoa[] {
                new Pessoa("Maria Souza", "123.456.789-00", 30, "+55 83 9 9999-9999", "10/10/1994"),
                new Pessoa("João Silva", "111.111.111-11", 25, "+55 83 9 8888-8888", "15/05/1999"),
                new Pessoa("Ana Lima", "222.222.222-22", 40, "+55 83 9 7777-7777", "22/12/1984"),
                new Pessoa("Carlos Pinto", "333.333.333-33", 29, "+55 83 9 6666-6666", "03/03/1995"),
                new Pessoa("Fernanda Rocha", "444.444.444-44", 35, "+55 83 9 5555-5555", "28/07/1989")
        };

        for(Pessoa p: pessoasPresentes){
            lsm.add(p.getCpf(), p);
        }

        // esperando para que o temporizador chame o flush e o levelCompaction
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Pessoa[] pessoasAusentes = new Pessoa[] {
                new Pessoa("Sofia Almeida", "555.555.555-99", 28, "+55 83 9 1111-2222", "12/02/1997"),
                new Pessoa("Ricardo Gomes", "666.666.666-77", 33, "+55 83 9 3333-4444", "09/04/1990"),
                new Pessoa("Patrícia Costa", "777.777.777-88", 36, "+55 83 9 5555-6666", "25/06/1987"),
                new Pessoa("Eduardo Silva", "888.888.888-99", 41, "+55 83 9 7777-8888", "01/08/1982"),
                new Pessoa("Fernanda Pereira", "999.999.999-00", 26, "+55 83 9 9999-0000", "16/11/1998")
        };

        for(Pessoa pp: pessoasPresentes){
            assertNotNull(lsm.get(pp.getCpf()));

            String pessoa = ObjectSerializer.convertBytesToObject(lsm.get(pp.getCpf()), Pessoa.class).toString();
            assertEquals(pp.toString(), pessoa);
        }

        for(Pessoa pa: pessoasAusentes){
            assertNull(lsm.get(pa.getCpf()));
        }

    }

    @Test
    void testDelete() throws JsonProcessingException {
        Pessoa[] pessoas = new Pessoa[] {
                new Pessoa("Maria Souza", "123.456.789-00", 30, "+55 83 9 9999-9999", "10/10/1994"),
                new Pessoa("João Silva", "111.111.111-11", 25, "+55 83 9 8888-8888", "15/05/1999"),
                new Pessoa("Ana Lima", "222.222.222-22", 40, "+55 83 9 7777-7777", "22/12/1984"),
                new Pessoa("Carlos Pinto", "333.333.333-33", 29, "+55 83 9 6666-6666", "03/03/1995"),
                new Pessoa("Fernanda Rocha", "444.444.444-44", 35, "+55 83 9 5555-5555", "28/07/1989"),
                new Pessoa("Lucas Andrade", "555.555.555-55", 22, "+55 83 9 4444-4444", "01/01/2003"),
                new Pessoa("Juliana Alves", "666.666.666-66", 27, "+55 83 9 3333-3333", "19/09/1997"),
                new Pessoa("Pedro Melo", "777.777.777-77", 32, "+55 83 9 2222-2222", "11/11/1992"),
                new Pessoa("Camila Teixeira", "888.888.888-88", 38, "+55 83 9 1111-1111", "07/04/1986"),
                new Pessoa("Rafael Costa", "999.999.999-99", 45, "+55 83 9 0000-0000", "30/06/1979")
        };

        // esperando para que o temporizador chame o flush e o levelCompaction
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(Pessoa p: pessoas){
            lsm.add(p.getCpf(), p);
        }

        for(Pessoa pp: pessoas){
            assertNotNull(lsm.get(pp.getCpf()));
            lsm.delete(pp.getCpf());
        }

        // removeu com sucesso
        for(Pessoa pp: pessoas){
            assertNull(lsm.get(pp.getCpf()));
        }

    }
}
