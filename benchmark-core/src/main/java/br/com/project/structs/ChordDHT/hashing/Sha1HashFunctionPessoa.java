package main.java.br.com.project.structs.ChordDHT.hashing;

import br.com.project.entities.Pessoa;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1HashFunctionPessoa implements HashFunction<Pessoa> {

    @Override
    public BigInteger hash(Pessoa pessoa) {
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa não pode ser nula");
        }
        String cpf = pessoa.getCpf();
        if (cpf == null) {
            throw new IllegalArgumentException("CPF da Pessoa não pode ser nulo");
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
