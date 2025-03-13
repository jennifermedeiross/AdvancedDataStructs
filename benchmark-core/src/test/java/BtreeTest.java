

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.project.entities.Pessoa;
import br.com.project.structs.BTree;

class BTreeTest {

    private BTree bTree;
    private Pessoa pessoa1;
    private Pessoa pessoa2;
    private Pessoa pessoa3;

    @BeforeEach
    void setUp() {
        // Inicializa a B-Tree com um grau mínimo de 2
        bTree = new BTree(2);

        // Inicializa algumas instâncias de Pessoa para inserir na árvore
        pessoa1 = new Pessoa("João", 30, "12345678901", "987654321", LocalDate.of(1993, 5, 15));
        pessoa2 = new Pessoa("Maria", 28, "23456789012", "912345678", LocalDate.of(1995, 8, 22));
        pessoa3 = new Pessoa("Carlos", 35, "34567890123", "998877665", LocalDate.of(1988, 2, 10));
    }

    @Test
    void testAdd() {
        // Adiciona pessoas na árvore
        bTree.add(pessoa1);
        bTree.add(pessoa2);
        bTree.add(pessoa3);

        // Testa se as pessoas foram inseridas corretamente
        assertNotNull(bTree.search("12345678901"));
        assertNotNull(bTree.search("23456789012"));
        assertNotNull(bTree.search("34567890123"));
    }

    @Test
    void testSearch() {
        bTree.add(pessoa1);
        bTree.add(pessoa2);

        // Testa a busca por CPF
        Pessoa pessoaEncontrada = bTree.search("12345678901");
        assertNotNull(pessoaEncontrada);
        assertEquals("João", pessoaEncontrada.getNome());

        // Testa se a busca por um CPF inexistente retorna null
        assertNull(bTree.search("99999999999"));
    }

    @Test
    void testDelete() {
        bTree.add(pessoa1);
        bTree.add(pessoa2);
        bTree.add(pessoa3);

        // Testa se as pessoas estão presentes na árvore antes da remoção
        assertNotNull(bTree.search("12345678901"));
        assertNotNull(bTree.search("23456789012"));
        assertNotNull(bTree.search("34567890123"));

        // Remove uma pessoa
        bTree.delete("23456789012");

        // Testa se a pessoa foi removida corretamente
        assertNull(bTree.search("23456789012"));
        assertNotNull(bTree.search("12345678901"));
        assertNotNull(bTree.search("34567890123"));
    }

    @Test
    void testDeleteNonExistent() {
        bTree.add(pessoa1);
        bTree.add(pessoa2);

        // Testa se não gera erro ao tentar remover uma pessoa que não está na árvore
        bTree.delete("99999999999");

        // Testa se a árvore ainda contém as pessoas válidas
        assertNotNull(bTree.search("12345678901"));
        assertNotNull(bTree.search("23456789012"));
    }

    @Test
    void testBTreeAfterMultipleInsertions() {
        // Insere múltiplos elementos
        bTree.add(pessoa1);
        bTree.add(pessoa2);
        bTree.add(pessoa3);

        // Verifica se a árvore mantém as pessoas corretamente
        assertNotNull(bTree.search("12345678901"));
        assertNotNull(bTree.search("23456789012"));
        assertNotNull(bTree.search("34567890123"));
    }

    @Test
    void testEmptyTree() {
        // Testa se a árvore está vazia e retorna null para qualquer busca
        assertNull(bTree.search("12345678901"));
    }
}
