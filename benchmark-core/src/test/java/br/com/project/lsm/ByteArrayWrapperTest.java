package br.com.project.lsm;

import br.com.project.entities.Pessoa;
import br.com.project.structs.lsm.types.ByteArrayWrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ByteArrayWrapperTest {

    @Test
    void comparaByteString() {
        byte[] b1 = "teste".getBytes();
        byte[] b2 = "teste".getBytes();

        assertNotEquals(b1, b2);

        ByteArrayWrapper b1Wrapper = new ByteArrayWrapper(b1);
        ByteArrayWrapper b2Wrapper = new ByteArrayWrapper(b2);

        assertEquals(b1Wrapper, b2Wrapper);
    }

    @Test
    void comparaBytePessoa() {
        Pessoa p1 = new Pessoa("PESSOA", "111.111.111-11", 30, "+55 11 1111-1111", "01/01/1993");
        Pessoa p2 = new Pessoa("OUTRA", "222.222.222-22", 25, "+55 22 2222-2222", "02/02/1998");

        byte[] p1Byte = p1.toJson().getBytes();
        byte[] p2Byte = p1.toJson().getBytes();

        assertNotEquals(p1Byte, p2Byte);

        ByteArrayWrapper b1Wrapper = new ByteArrayWrapper(p1.toJson().getBytes());
        ByteArrayWrapper b2Wrapper = new ByteArrayWrapper(p1.toJson().getBytes());

        assertEquals(b1Wrapper, b2Wrapper);
    }
}
