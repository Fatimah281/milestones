package util;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
//</editor-fold>
public final class JsonUtil {

    //<editor-fold desc="Fields">
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    //</editor-fold>

    //<editor-fold desc="Constructors">
    private JsonUtil() {
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public static String readBodyAsString(Reader reader) throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader br = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
        }
        return json.toString();
    }

    public static <T> T fromJson(Reader reader, Class<T> clazz) throws IOException {
        return MAPPER.readValue(readBodyAsString(reader), clazz);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return MAPPER.readValue(json, clazz);
    }

    public static String toJson(Object object) throws IOException {
        return MAPPER.writeValueAsString(object);
    }
    //</editor-fold>
}
