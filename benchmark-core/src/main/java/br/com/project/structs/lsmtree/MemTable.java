package br.com.project.structs.lsmtree;

import br.com.project.entities.Pessoa;
import java.util.TreeMap;

/**
 * Representa uma MemTable, estrutura de dados utilizada em LSM-Trees
 * para armazenar dados temporariamente na memória antes de serem
 * gravados em disco como SSTables.
 * <p>
 * Utiliza um {@link TreeMap} para garantir que os dados sejam armazenados
 * de forma ordenada.
 * </p>
 *
 * @author jennifermedeiross
 */
public class MemTable {
    private final TreeMap<String, Pessoa> data;
    private final int capacity;

    /**
     * Construtor para criar uma MemTable com capacidade limitada.
     *
     * @param capacity O número máximo de elementos que a MemTable pode armazenar.
     */
    public MemTable(int capacity) {
        this.capacity = capacity;
        this.data = new TreeMap<>();
    }

    /**
     * Insere ou atualiza um valor na MemTable.
     *
     * @param key   A chave do registro.
     * @param value O valor associado à chave.
     */
    public void put(String key, Pessoa value) {
        data.put(key, value);
    }

    /**
     * Recupera o valor associado a uma chave específica.
     *
     * @param key A chave do registro.
     * @return O valor associado à chave ou {@code null} se a chave não existir.
     */
    public Pessoa get(String key) {
        return data.get(key);
    }

    /**
     * Marca um registro como removido utilizando um "tombstone" (valor {@code null}).
     * Esse conceito é usado em LSM-Trees para indicar que um registro foi excluído.
     *
     * @param key A chave do registro a ser removido.
     */
    public void delete(String key) {
        data.put(key, null);
    }

    /**
     * Verifica se a MemTable atingiu sua capacidade máxima.
     *
     * @return {@code true} se a MemTable estiver cheia, {@code false} caso contrário.
     */
    public boolean isFull() {
        return data.size() >= capacity;
    }

    /**
     * Realiza um flush dos dados da MemTable, copiando-os para um novo {@link TreeMap}
     * e limpando a estrutura atual.
     * <p>
     * Esse método é útil para mover os dados da MemTable para armazenamento em disco.
     * </p>
     *
     * @return Uma cópia dos dados armazenados antes do flush.
     */
    public TreeMap<String, Pessoa> flush() {
        TreeMap<String, Pessoa> copy = new TreeMap<>(data);
        data.clear();
        return copy;
    }
}
