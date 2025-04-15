package br.com.project.lsm;

import br.com.project.structs.lsm.types.ByteArrayPair;
import br.com.project.structs.lsm.types.ByteArrayWrapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ByteArrayPairTest {

    @Test
    void testSize() {
        byte[] key = "123".getBytes(StandardCharsets.UTF_8);
        byte[] value = "abc".getBytes(StandardCharsets.UTF_8);

        ByteArrayPair pair = new ByteArrayPair(key, value);

        assertEquals(6, pair.size());
    }

    @Test
    void testGetKeyReturnsWrapper() {
        byte[] key = "abc".getBytes(StandardCharsets.UTF_8);
        ByteArrayPair pair = new ByteArrayPair(key, new byte[]{1, 2, 3});

        ByteArrayWrapper wrapper = pair.getKey();

        assertArrayEquals(key, wrapper.getData());
    }

    @Test
    void testCompareTo() {
        ByteArrayPair a = new ByteArrayPair("a".getBytes(), new byte[]{1});
        ByteArrayPair b = new ByteArrayPair("b".getBytes(), new byte[]{2});

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(new ByteArrayPair("a".getBytes(), new byte[]{99})));
    }

    @Test
    void testHashCodeUsesKeyOnly() {
        ByteArrayPair p1 = new ByteArrayPair("key".getBytes(), "value1".getBytes());
        ByteArrayPair p2 = new ByteArrayPair("key".getBytes(), "value2".getBytes());

        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testToStringWithValue() {
        ByteArrayPair pair = new ByteArrayPair(
                new byte[]{1, 2},
                new byte[]{10, 20}
        );

        String result = pair.toString();

        assertTrue(result.contains("Key: [1, 2]"));
        assertTrue(result.contains("Value: [10, 20]"));
    }

    @Test
    void testToStringWithTombstone() {
        ByteArrayPair pair = new ByteArrayPair(
                new byte[]{1, 2, 3},
                new byte[]{} // tombstone
        );

        String result = pair.toString();

        assertTrue(result.contains("TOMBSTONE"));
    }
}
