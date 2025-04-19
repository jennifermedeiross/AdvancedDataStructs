package br.com.project.treeMap;

import br.com.project.structs.treeMap.TreeMap;
import br.com.project.entities.Pessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.util.List;
import java.util.NoSuchElementException;

class TreeMapTest {
    private TreeMap<String, Pessoa> treeMap;
    private Pessoa pessoa1, pessoa2, pessoa3;

    @BeforeEach
    void setUp() {
        treeMap = new TreeMap<>();

        pessoa1 = new Pessoa("João Silva", "111.111.111-11", 30, "11 99999-9999", "01/01/1990");
        pessoa2 = new Pessoa("Maria Souza", "222.222.222-22", 25, "22 88888-8888", "02/02/1995");
        pessoa3 = new Pessoa("Carlos Oliveira", "333.333.333-33", 40, "33 77777-7777", "03/03/1980");
    }

    @Test
    void testPutAndGet() {
        treeMap.put(pessoa1.getCpf(), pessoa1);
        treeMap.put(pessoa2.getCpf(), pessoa2);

        assertEquals(pessoa1, treeMap.get(pessoa1.getCpf()));
        assertEquals(pessoa2, treeMap.get(pessoa2.getCpf()));
        assertNull(treeMap.get(pessoa3.getCpf()));
    }

    @Test
    void testContainsKey() {
        treeMap.put(pessoa1.getCpf(), pessoa1);

        assertTrue(treeMap.containsKey(pessoa1.getCpf()));
        assertFalse(treeMap.containsKey(pessoa2.getCpf()));
    }

    @Test
    void testSize() {
        assertEquals(0, treeMap.size());

        treeMap.put(pessoa1.getCpf(), pessoa1);
        assertEquals(1, treeMap.size());

        treeMap.put(pessoa2.getCpf(), pessoa2);
        assertEquals(2, treeMap.size());

        treeMap.delete(pessoa1.getCpf());
        assertEquals(1, treeMap.size());
    }

    @Test
    void testClear() {
        treeMap.put(pessoa1.getCpf(), pessoa1);
        treeMap.put(pessoa2.getCpf(), pessoa2);

        treeMap.clear();

        assertEquals(0, treeMap.size());
        assertNull(treeMap.get(pessoa1.getCpf()));
    }

    @Test
    void testFirstKeyAndLastKey() {
        treeMap.put(pessoa2.getCpf(), pessoa2);
        treeMap.put(pessoa1.getCpf(), pessoa1);
        treeMap.put(pessoa3.getCpf(), pessoa3);

        assertEquals(pessoa1.getCpf(), treeMap.firstKey());
        assertEquals(pessoa3.getCpf(), treeMap.lastKey());
    }

    @Test
    void testDelete() {
        treeMap.put(pessoa1.getCpf(), pessoa1);
        treeMap.put(pessoa2.getCpf(), pessoa2);

        treeMap.delete(pessoa1.getCpf());

        assertNull(treeMap.get(pessoa1.getCpf()));
        assertEquals(pessoa2, treeMap.get(pessoa2.getCpf()));
        assertEquals(1, treeMap.size());
    }

    @Test
    void testDeleteMin() {
        treeMap.put(pessoa2.getCpf(), pessoa2);
        treeMap.put(pessoa1.getCpf(), pessoa1);
        treeMap.put(pessoa3.getCpf(), pessoa3);

        treeMap.deleteMin();

        assertNull(treeMap.get(pessoa1.getCpf()));
        assertEquals(2, treeMap.size());
    }

    @Test
    void testIsBalanced() {
        // Inserção que deve manter a árvore balanceada
        treeMap.put(pessoa2.getCpf(), pessoa2);
        assertTrue(treeMap.isBalanced());

        treeMap.put(pessoa1.getCpf(), pessoa1);
        assertTrue(treeMap.isBalanced());

        treeMap.put(pessoa3.getCpf(), pessoa3);
        assertTrue(treeMap.isBalanced());
    }

    @Test
    void testKeys() {
        treeMap.put(pessoa2.getCpf(), pessoa2);
        treeMap.put(pessoa1.getCpf(), pessoa1);
        treeMap.put(pessoa3.getCpf(), pessoa3);

        List<String> keys = treeMap.keys();

        assertEquals(3, keys.size());
        assertEquals(pessoa1.getCpf(), keys.get(0));
        assertEquals(pessoa2.getCpf(), keys.get(1));
        assertEquals(pessoa3.getCpf(), keys.get(2));
    }

    @Test
    void testValues() {
        treeMap.put(pessoa2.getCpf(), pessoa2);
        treeMap.put(pessoa1.getCpf(), pessoa1);
        treeMap.put(pessoa3.getCpf(), pessoa3);

        List<Pessoa> values = treeMap.values();

        assertEquals(3, values.size());
        assertEquals(pessoa1, values.get(0));
        assertEquals(pessoa2, values.get(1));
        assertEquals(pessoa3, values.get(2));
    }

    @Test
    void testIterator() {
        treeMap.put(pessoa2.getCpf(), pessoa2);
        treeMap.put(pessoa1.getCpf(), pessoa1);
        treeMap.put(pessoa3.getCpf(), pessoa3);

        var iterator = treeMap.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(pessoa1.getCpf(), iterator.next());
        assertEquals(pessoa2.getCpf(), iterator.next());
        assertEquals(pessoa3.getCpf(), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testIteratorThrowsNoSuchElementException() {
        var iterator = treeMap.iterator();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testUpdateValue() {
        treeMap.put(pessoa1.getCpf(), pessoa1);

        Pessoa updatedPessoa = new Pessoa("João Silva Updated", pessoa1.getCpf(), 35, "11 99999-0000", "01/01/1990");
        treeMap.put(updatedPessoa.getCpf(), updatedPessoa);

        assertEquals(1, treeMap.size());
        assertEquals(updatedPessoa, treeMap.get(pessoa1.getCpf()));
    }

    @Test
    void testValoresExtremosKeyInsertion() {
        // Menor valor possível para chave String
        Pessoa minPessoa = new Pessoa("Min", "000.000.000-00", 0, "00 00000-0000", "01/01/1900");
        treeMap.put(minPessoa.getCpf(), minPessoa);

        // Maior valor possível para chave String
        Pessoa maxPessoa = new Pessoa("Max", "999.999.999-99", 150, "99 99999-9999", "31/12/2100");
        treeMap.put(maxPessoa.getCpf(), maxPessoa);

        assertEquals(minPessoa, treeMap.get("000.000.000-00"));
        assertEquals(maxPessoa, treeMap.get("999.999.999-99"));
    }

    @Test
    void testAltoVolumeOperacoes() {
        // Teste com 10.000 elementos
        for (int i = 0; i < 10000; i++) {
            String cpf = String.format("%011d", i);
            treeMap.put(cpf, new Pessoa("Pessoa " + i, cpf, i % 100, "", "01/01/2000"));
        }

        assertTrue(treeMap.isBalanced());
        assertEquals(10000, treeMap.size());
        assertEquals("Pessoa 0", treeMap.get("00000000000").getNome());
    }


    @Test
    void testDelatarEmCascata() {

        Pessoa p2 = new Pessoa("Pessoa 2", "222.222.222-22", 22, "22 22222-2222", "02/02/2000");
        Pessoa p3 = new Pessoa("Pessoa 3", "333.333.333-33", 33, "33 33333-3333", "03/03/1990");
        Pessoa p4 = new Pessoa("Pessoa 4", "444.444.444-44", 44, "44 44444-4444", "04/04/1980");
        Pessoa p5 = new Pessoa("Pessoa 5", "555.555.555-55", 55, "55 55555-5555", "05/05/1970");
        Pessoa p7 = new Pessoa("Pessoa 7", "777.777.777-77", 77, "77 77777-7777", "07/07/1950");
        Pessoa p8 = new Pessoa("Pessoa 8", "888.888.888-88", 88, "88 88888-8888", "08/08/1940");


        treeMap.put("5", p5);
        treeMap.put("3", p3);
        treeMap.put("7", p7);
        treeMap.put("2", p2);
        treeMap.put("4", p4);
        treeMap.put("8", p8);


        assertEquals(6, treeMap.size());
        assertTrue(treeMap.isBalanced());
        assertEquals(p5, treeMap.get("5"));


        treeMap.delete("5");


        assertEquals(5, treeMap.size());
        assertFalse(treeMap.containsKey("5"));
        assertTrue(treeMap.isBalanced());


        assertEquals(p7, treeMap.get("7"));
        assertNotNull(treeMap.get("3"));
        assertNull(treeMap.get("6"));


        List<String> keys = treeMap.keys();
        assertEquals(List.of("2", "3", "4", "7", "8"), keys);
    }

    @Test
    void testHashCollision() {
        Pessoa p1 = new Pessoa("A", "111.111.111-11", 30, "...", "...");
        Pessoa p2 = new Pessoa("B", "111.111.111-11", 40, "...", "...");

        treeMap.put(p1.getCpf(), p1);
        treeMap.put(p2.getCpf(), p2); // Deve sobrescrever

        assertEquals(1, treeMap.size());
        assertEquals("B", treeMap.get("111.111.111-11").getNome());
    }
}