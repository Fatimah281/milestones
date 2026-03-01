package util;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import exception.InvalidJsonException;
import exception.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
//</editor-fold>

public final class JsonUtil {

    //<editor-fold desc="Fields">
    private static final Logger LOG = LogManager.getLogger(JsonUtil.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String EMPLOYEE_SCHEMA_PATH = "/schemas/employee.schema.json";
    private static final JsonSchema EMPLOYEE_SCHEMA = loadEmployeeSchema();
    //</editor-fold>

    //<editor-fold desc="Constructors">
    private JsonUtil() {
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public static String toJson(Object object) throws IOException {
        LOG.debug("Serializing object to JSON: {}", object != null ? object.getClass().getSimpleName() : "null");
        String json = MAPPER.writeValueAsString(object);
        LOG.trace("Serialized length: {} chars", json != null ? json.length() : 0);
        return json;
    }

    public static <T> T fromJsonWithEmployeeValidation(Reader reader, Class<T> clazz) throws IOException {
        LOG.debug("Reading and validating JSON for {}", clazz.getSimpleName());
        JsonNode node;
        try {
            node = MAPPER.readTree(reader);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            LOG.error("Invalid JSON: {}", e.getMessage());
            throw new InvalidJsonException("Invalid JSON: " + e.getMessage(), e);
        }
        if (node == null || node.isMissingNode()) {
            LOG.error("Empty or missing JSON body");
            throw new ValidationException("Request body is required");
        }
        normalizeHobbiesToObjects(node);
        validateEmployeeSchema(node);
        T value = MAPPER.treeToValue(node, clazz);
        LOG.trace("Validated and deserialized: {}", value);
        return value;
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private static void validateEmployeeSchema(JsonNode node) {
        Set<ValidationMessage> errors = EMPLOYEE_SCHEMA.validate(node);
        if (!errors.isEmpty()) {
            List<String> details = new ArrayList<>();
            for (ValidationMessage m : errors) {
                details.add(m.getMessage());
            }
            LOG.error("Employee JSON schema validation failed: {} error(s)", details.size());
            LOG.debug("Validation errors: {}", details);
            throw new ValidationException("Request body does not match employee schema", details);
        }
        LOG.trace("Employee schema validation passed");
    }

    private static void normalizeHobbiesToObjects(JsonNode root) {
        JsonNode hobbies = root.path("hobbies");
        if (!hobbies.isArray()) return;
        ArrayNode arr = (ArrayNode) hobbies;
        for (int i = 0; i < arr.size(); i++) {
            JsonNode item = arr.get(i);
            if (item.isTextual()) {
                ObjectNode obj = MAPPER.createObjectNode().put("name", item.asText());
                arr.set(i, obj);
                LOG.trace("Normalized hobby[{}] from string to object", i);
            }
        }
    }

    private static JsonSchema loadEmployeeSchema() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        try (InputStream is = JsonUtil.class.getResourceAsStream(EMPLOYEE_SCHEMA_PATH)) {
            if (is == null) {
                LOG.fatal("Employee schema not found on classpath: {}", EMPLOYEE_SCHEMA_PATH);
                throw new IllegalStateException("Schema not found: " + EMPLOYEE_SCHEMA_PATH);
            }
            LOG.info("Loaded employee JSON schema from {}", EMPLOYEE_SCHEMA_PATH);
            return factory.getSchema(is);
        } catch (IOException e) {
            LOG.error("Failed to load employee schema", e);
            throw new IllegalStateException("Failed to load schema", e);
        }
    }
    //</editor-fold>
}
