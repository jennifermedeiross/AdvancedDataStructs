package br.com.project.structs.chordDHT.hashing;


import java.math.BigInteger;

/**
 * Interface para funções de hash.
 * 
 * @param <K> Tipo da chave a ser hasheada.
 */

public interface HashFunction<K> {

    /**
     * Gera um valor de hash (normalmente BigInteger para Chord)
     * a partir da entrada 'key'.
     */
    BigInteger hash(K key);
}
