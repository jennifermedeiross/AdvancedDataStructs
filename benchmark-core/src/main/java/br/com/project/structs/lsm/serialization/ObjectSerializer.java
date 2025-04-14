package br.com.project.structs.lsm.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A classe {@code ObjectSerializer} provê métodos utilitários para serializar e desserializar objetos
 * genéricos para arrays de bytes e vice-versa, utilizando a biblioteca Jackson.
 */
public class ObjectSerializer {
    /**
     * Usando o Jackson para converter objetos em bytes.
     *
     * @param obj O objeto a ser convertido para byte[].
     * @return byte[] com a representação do objeto.
     * @throws JsonProcessingException Se houver falha na serialização.
     */
    public static byte[] convertToBytes(Object obj) throws JsonProcessingException {
        if (obj instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(obj);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converte um array de bytes num objeto do tipo especificado.
     * Para {@code String.class}, o conteúdo é interpretado diretamente como texto UTF-8.
     *
     * @param bytes O array de bytes contendo a representação serializada do objeto.
     * @param clazz A classe do tipo de objeto esperado no retorno.
     * @param <T>   O tipo do objeto a ser retornado.
     * @return Uma instância do objeto desserializado a partir dos bytes.
     * @throws IOException Se ocorrer erro na desserialização com Jackson.
     */
    public static <T> T convertBytesToObject(byte[] bytes, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        if (clazz == String.class) {
            String raw = new String(bytes, StandardCharsets.UTF_8);
            return objectMapper.readValue("\"" + raw + "\"", clazz);
        }

        String json = new String(bytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(json, clazz);
    }
}
