package br.com.project.lsm;

import br.com.project.entities.Pessoa;
import br.com.project.structs.lsm.memtable.AVLTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AVLTreeTest {
    private AVLTree<String, Pessoa> avl;

    @BeforeEach
    void setUp() {
        avl = new AVLTree<>(Pessoa::getCpf);
    }

    @Test
    void testAddAndSearch() {
        Pessoa p1 = new Pessoa("PESSOA SOBRENOME", "250.341.876-71", 71, "+55 04 9 5705-0142", "20/10/1952");
        avl.add(p1);
        assertEquals(p1, avl.search("250.341.876-71").getValue());
    }


    @Test
    void testUpdateOnDuplicateAdd() {
        Pessoa p1 = new Pessoa("PESSOA SOBRENOME", "250.341.876-71", 71, "+55 04 9 5705-0142", "20/10/1952");
        Pessoa p2 = new Pessoa("PESSOA Atualizado", "250.341.876-71", 72, "+55 04 9 5705-9999", "20/10/1952");

        avl.add(p1);
        avl.add(p2);

        Pessoa result = avl.search("250.341.876-71").getValue();
        assertEquals("PESSOA Atualizado", result.getNome());
        assertEquals("+55 04 9 5705-9999", result.getTelefone());
        assertEquals(1, avl.size());
    }

    @Test
    void testRemoveLeaf() {
        Pessoa p1 = new Pessoa("PESSOA", "111.111.111-11", 30, "+55 11 1111-1111", "01/01/1993");
        Pessoa p2 = new Pessoa("OUTRA", "222.222.222-22", 25, "+55 22 2222-2222", "02/02/1998");

        avl.add(p1);
        avl.add(p2);

        assertNotNull(avl.remove("222.222.222-22"));
        assertNull(avl.search("222.222.222-22"));
        assertEquals(1, avl.size());
    }

    @Test
    void testRemoveNodeWithTwoChildren() {
        avl.add(new Pessoa("Ana", "111.111.111-11", 30, "+55 11 1111-1111", "01/01/1993"));
        avl.add(new Pessoa("Bruno", "222.222.222-22", 31, "+55 22 2222-2222", "02/02/1992"));
        avl.add(new Pessoa("Carlos", "333.333.333-33", 32, "+55 33 3333-3333", "03/03/1991"));

        assertNotNull(avl.remove("222.222.222-22"));
        assertNull(avl.search("222.222.222-22"));
        assertEquals(2, avl.size());
    }

    @Test
    void testMinMax() {
        avl.add(new Pessoa("Ana", "111.111.111-11", 30, "+55 11 1111-1111", "01/01/1993"));
        avl.add(new Pessoa("Carlos", "333.333.333-33", 32, "+55 33 3333-3333", "03/03/1991"));
        avl.add(new Pessoa("Bruno", "222.222.222-22", 31, "+55 22 2222-2222", "02/02/1992"));

        assertEquals("111.111.111-11", avl.min().getValue().getCpf());
        assertEquals("333.333.333-33", avl.max().getValue().getCpf());
    }

    @Test
    void testPredecessor() {
        avl.add(new Pessoa("Ana", "111.111.111-11", 30, "+55 11 1111-1111", "01/01/1993"));
        avl.add(new Pessoa("Bruno", "222.222.222-22", 31, "+55 22 2222-2222", "02/02/1992"));
        avl.add(new Pessoa("Carlos", "333.333.333-33", 32, "+55 33 3333-3333", "03/03/1991"));

        assertEquals("Nome: Bruno\n" +
                "CPF: 222.222.222-22\n" +
                "Idade: 31\n" +
                "Telefone: +55 22 2222-2222\n" +
                "Data de Nasc.: 02/02/1992", avl.predecessor("333.333.333-33"));
    }

    @Test
    void testSucessor() {
        avl.add(new Pessoa("Ana", "111.111.111-11", 30, "+55 11 1111-1111", "01/01/1993"));
        avl.add(new Pessoa("Bruno", "222.222.222-22", 31, "+55 22 2222-2222", "02/02/1992"));
        avl.add(new Pessoa("Carlos", "333.333.333-33", 32, "+55 33 3333-3333", "03/03/1991"));

        assertNull(avl.sucessor("333.333.333-33"));
        assertEquals("Nome: Carlos\n" +
                "CPF: 333.333.333-33\n" +
                "Idade: 32\n" +
                "Telefone: +55 33 3333-3333\n" +
                "Data de Nasc.: 03/03/1991", avl.sucessor("222.222.222-22").getValue().toString());
    }
}
