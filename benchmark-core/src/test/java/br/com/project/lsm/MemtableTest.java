package br.com.project.lsm;

import br.com.project.entities.Pessoa;
import br.com.project.structs.lsm.memtable.Memtable;
import br.com.project.structs.lsm.serialization.ObjectSerializer;
import br.com.project.structs.lsm.types.ByteArrayPair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MemtableTest {
    @Test
    void testAddAndGet() throws IOException {
        Memtable memtable = new Memtable();

        byte[] key = "chave1".getBytes();
        byte[] value = "valor1".getBytes();
        memtable.add(new ByteArrayPair(key, value));

        byte[] result = memtable.get(key);
        assertEquals("valor1", ObjectSerializer.convertBytesToObject(result, String.class));
        assertNotNull(result);
        assertArrayEquals(value, result);
    }

    @Test
    void testGetNonExistingKey() {
        Memtable memtable = new Memtable();

        byte[] result = memtable.get("inexistente".getBytes());
        assertNull(result);
    }

    @Test
    void testRemoveInsertsTombstone() throws IOException {
        Memtable memtable = new Memtable();

        byte[] key = "teste".getBytes();
        byte[] value = "valor".getBytes();
        memtable.add(new ByteArrayPair(key, value));

        byte[] result = memtable.get(key);
        assertEquals("valor", ObjectSerializer.convertBytesToObject(result, String.class));

        memtable.remove(key);
        result = memtable.get(key);
        assertEquals("", ObjectSerializer.convertBytesToObject(result, String.class));

        assertNotNull(result);
        assertEquals(0, result.length); // tombstone
    }

    @Test
    void testByteSizeAfterInsertions() {
        Memtable memtable = new Memtable();

        ByteArrayPair p1 = new ByteArrayPair("k1".getBytes(), "v1".getBytes());
        ByteArrayPair p2 = new ByteArrayPair("k2".getBytes(), "v2".getBytes());
        memtable.add(p1);
        memtable.add(p2);

        long expected = p1.size() + p2.size();
        assertEquals(expected, memtable.byteSize());
    }

    @Test
    void testIteratorSkipsDuplicateKeys() {
        Memtable memtable = new Memtable();

        byte[] key = "duplicado".getBytes();
        memtable.add(new ByteArrayPair(key, "valor1".getBytes()));
        memtable.add(new ByteArrayPair("outro".getBytes(), "x".getBytes()));
        memtable.add(new ByteArrayPair(key, "valor2".getBytes())); // sobrescreve chave anterior

        List<String> keys = new ArrayList<>();
        for (ByteArrayPair pair : memtable) {
            keys.add(new String(pair.key()));
        }

        assertEquals(2, keys.size()); // não deve repetir "duplicado"
        assertTrue(keys.contains("duplicado"));
        assertTrue(keys.contains("outro"));
    }

    @Test
    void testAddAndGetPessoa() throws Exception {
        Memtable memtable = new Memtable();

        Pessoa pessoa = new Pessoa(
                "Pedro Henrique Fernandes",
                "250.341.876-71",
                71,
                "+55 04 9 5705-0142",
                "20/10/1952"
        );

        byte[] key = pessoa.getCpf().getBytes();
        byte[] value = ObjectSerializer.convertToBytes(pessoa);

        memtable.add(new ByteArrayPair(key, value));

        byte[] recovered = memtable.get(key);
        assertNotNull(recovered);

        Pessoa result = ObjectSerializer.convertBytesToObject(recovered, Pessoa.class);
        assertEquals(pessoa.getCpf(), result.getCpf());
        assertEquals(pessoa.getNome(), result.getNome());
    }

    @Test
    void testRemovePessoa() throws Exception {
        Memtable memtable = new Memtable();

        Pessoa pessoa = new Pessoa(
                "Maria Souza",
                "123.456.789-00",
                30,
                "+55 83 9 9999-9999",
                "10/10/1994"
        );

        byte[] key = pessoa.getCpf().getBytes();
        byte[] value = ObjectSerializer.convertToBytes(pessoa);

        memtable.add(new ByteArrayPair(key, value));
        memtable.remove(key);

        byte[] result = memtable.get(key);
        assertNotNull(result);
        assertEquals(0, result.length); // tombstone

    }
}
