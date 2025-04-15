package br.com.project.lsm;

import br.com.project.entities.Pessoa;
import br.com.project.structs.lsm.serialization.ObjectSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectSerializerTest {

    @Test
    void testSerializeAndDeserializeString() throws IOException {
        String original = "Teste de string com acentuação: çãê!";
        byte[] serialized = ObjectSerializer.convertToBytes(original);
        String result = ObjectSerializer.convertBytesToObject(serialized, String.class);

        assertEquals(original, result);
    }

    @Test
    void testSerializeAndDeserializePessoa() throws IOException {
        Pessoa p1 = new Pessoa("PESSOA SOBRENOME", "250.341.876-71", 71, "+55 04 9 5705-0142", "20/10/1952");
        byte[] serialized = ObjectSerializer.convertToBytes(p1);
        Pessoa deserialized = ObjectSerializer.convertBytesToObject(serialized, Pessoa.class);

        assertEquals(p1.getNome(), deserialized.getNome());
        assertEquals(p1.getCpf(), deserialized.getCpf());
        assertEquals(p1.getIdade(), deserialized.getIdade());
    }

    @Test
    void testSerializeNull() {
        try {
            ObjectSerializer.convertToBytes(null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
