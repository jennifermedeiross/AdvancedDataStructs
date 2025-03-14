package br.com.project.structs.chordDHT.hashing;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementa {@link HashFunction} para strings,
 * tipicamente usado para hashear o CPF de uma Pessoa.
 */
public class Sha1HashFunctionPessoa implements HashFunction<String> {

    @Override
    public BigInteger hash(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF não pode ser nulo");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(cpf.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-1 não disponível.", e);
        }
    }
}