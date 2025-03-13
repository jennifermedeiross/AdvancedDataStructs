package main.java.br.com.project.structs.ChordDHT.node;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a tabela de dedos (finger table) de um nó Chord.
 * Cada entrada finger[i] aponta para o nó que inicia o intervalo
 * [ (selfId + 2^i) mod 2^m, ... ), onde m é o número de bits do ID.
 */
public class FingerTable {

    private final List<NodeReference> fingers;

    private final int size;

    /**
     * Construtor que inicializa a finger table com um tamanho definido (por exemplo, 160 para SHA-1).
     * 
     * @param size Número de posições da finger table (geralmente m, onde 2^m é o tamanho do espaço de IDs).
     */
    public FingerTable(int size) {
        this.size = size;
        this.fingers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            fingers.add(null);
        }
    }

    /**
     * Retorna o tamanho da finger table.
     */
    public int getSize() {
        return size;
    }

    /**
     * Define o nó de uma entrada específica da tabela.
     * 
     * @param index Índice (0 a size-1).
     * @param ref   Referência ao nó que inicia aquele intervalo.
     */
    public void setFinger(int index, NodeReference ref) {
        fingers.set(index, ref);
    }

    /**
     * Retorna a referência do nó na posição 'index' da finger table.
     */
    public NodeReference getFinger(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return fingers.get(index);
    }

    /**
     * Calcula o "start" para a posição i da finger table,
     * que é (nodeId + 2^i) mod (2^m).
     * 
     * @param selfId ID do nó atual.
     * @param i Índice da finger table.
     * @return Valor do "start".
     */
    public BigInteger calculateStart(BigInteger selfId, int i) {
        BigInteger twoToI = BigInteger.valueOf(2).pow(i);

        BigInteger ringSize = BigInteger.valueOf(2).pow(size);

        return selfId.add(twoToI).mod(ringSize);
    }
}

