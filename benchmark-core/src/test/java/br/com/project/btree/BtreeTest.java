package br.com.project.btree;

import br.com.project.entities.Pessoa;
import br.com.project.structs.btree.BTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BtreeTest {

    private BTree bTree;

    @BeforeEach
    public void setUp() {
        bTree = new BTree(2); // Grau mínimo t = 2
    }

    private Pessoa criarPessoa(String cpf) {
        return new Pessoa("Teste", cpf, 30, "99999-9999", "1994-04-10");
    }

    @Test
    public void testInsercaoEBusca() {
        Pessoa p1 = criarPessoa("11111111111");
        Pessoa p2 = criarPessoa("22222222222");
        Pessoa p3 = criarPessoa("33333333333");

        bTree.add(p1);
        bTree.add(p2);
        bTree.add(p3);

        assertEquals(p1, bTree.search("11111111111"));
        assertEquals(p2, bTree.search("22222222222"));
        assertEquals(p3, bTree.search("33333333333"));
    }

    @Test
    public void testBuscaNaoExistente() {
        Pessoa p1 = criarPessoa("11111111111");
        bTree.add(p1);

        assertNull(bTree.search("99999999999"));
    }

    @Test
    public void testRemocaoElementoFolha() {
        Pessoa p1 = criarPessoa("11111111111");
        bTree.add(p1);

        assertNotNull(bTree.search("11111111111"));

        bTree.delete("11111111111");

        assertNull(bTree.search("11111111111"));
    }

    @Test
    public void testRemocaoComReestruturacao() {
        Pessoa p1 = criarPessoa("11111111111");
        Pessoa p2 = criarPessoa("22222222222");
        Pessoa p3 = criarPessoa("33333333333");
        Pessoa p4 = criarPessoa("44444444444");

        bTree.add(p1);
        bTree.add(p2);
        bTree.add(p3);
        bTree.add(p4);

        assertNotNull(bTree.search("11111111111"));
        bTree.delete("11111111111");
        assertNull(bTree.search("11111111111"));
        assertNotNull(bTree.search("33333333333"));
        bTree.delete("33333333333");
        assertNull(bTree.search("33333333333"));
    }

    @Test
    public void testInsercaoComDivisaoDeNo() {
        for (int i = 1; i <= 10; i++) {
            String cpf = String.format("%011d", i);
            bTree.add(criarPessoa(cpf));
        }

        assertNotNull(bTree.search("00000000001"));
        assertNotNull(bTree.search("00000000010"));
    }

    @Test
    public void testSize() {
        assertEquals(0, bTree.size());

        bTree.add(criarPessoa("11111111111"));
        bTree.add(criarPessoa("22222222222"));

        assertEquals(2, bTree.size());

        bTree.delete("11111111111");

        assertEquals(1, bTree.size());

        bTree.delete("22222222222");

        assertEquals(0, bTree.size());
    }

    @Test
    public void testSizeComRemocaoComReestruturacao() {
        Pessoa p1 = criarPessoa("11111111111");
        Pessoa p2 = criarPessoa("22222222222");
        Pessoa p3 = criarPessoa("33333333333");
        Pessoa p4 = criarPessoa("44444444444");

        bTree.add(p1);
        bTree.add(p2);
        bTree.add(p3);
        bTree.add(p4);

        assertEquals(4, bTree.size());

        bTree.delete("11111111111");
        assertEquals(3, bTree.size());

        bTree.delete("33333333333");
        assertEquals(2, bTree.size());
    }

    @Test
    public void testInsercaoComCPFRepetido() {
        Pessoa p1 = criarPessoa("11111111111");
        Pessoa p2 = criarPessoa("11111111111");

        bTree.add(p1);
        bTree.add(p2);

        assertEquals(2, bTree.size());
        assertEquals(p1, bTree.search("11111111111"));
    }

    @Test
    public void testBuscaEmArvoreVazia() {
        assertNull(bTree.search("00000000000"));
    }

    @Test
    public void testRemocaoEmArvoreVazia() {
        bTree.delete("00000000000");
        assertEquals(0, bTree.size());
    }

    @Test
    public void testInsercaoEDivisaoDeNiveis() {
        for (int i = 1; i <= 50; i++) {
            String cpf = String.format("%011d", i);
            bTree.add(criarPessoa(cpf));
        }

        assertEquals(50, bTree.size());
        assertNotNull(bTree.search("00000000001"));
        assertNotNull(bTree.search("00000000050"));
        assertNull(bTree.search("00000000500"));
    }
}