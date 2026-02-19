package util;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.Reader;
//</editor-fold>
public final class JsonUtil {

    //<editor-fold desc="Fields">
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    //</editor-fold>

    //<editor-fold desc="Constructors">
    private JsonUtil() {
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public static <T> T fromJson(Reader reader, Class<T> clazz) throws IOException {
        return MAPPER.readValue(reader, clazz);
    }

    public static String toJson(Object object) throws IOException {
        return MAPPER.writeValueAsString(object);
    }
    //</editor-fold>
}
