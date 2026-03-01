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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
//</editor-fold>

public final class JsonUtil {

    //<editor-fold desc="Fields">
    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(JsonUtil.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String EMPLOYEE_SCHEMA_PATH = "/schemas/employee.schema.json";
    private static volatile JsonSchema employeeSchema;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    private JsonUtil() {
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    /** Deserialize JSON from reader to the given type (no schema validation). */
    public static <T> T fromJson(Reader reader, Class<T> clazz) throws IOException {
        LOG.debug("Deserializing JSON to {}", clazz.getSimpleName());
        T value = MAPPER.readValue(reader, clazz);
        LOG.trace("Deserialized: {}", value);
        return value;
    }

    /** Serialize object to JSON string. */
    public static String toJson(Object object) throws IOException {
        LOG.debug("Serializing object to JSON: {}", object != null ? object.getClass().getSimpleName() : "null");
        String json = MAPPER.writeValueAsString(object);
        LOG.trace("Serialized length: {} chars", json != null ? json.length() : 0);
        return json;
    }

    /** Parse JSON to a tree (JsonNode) for validation or inspection. */
    public static JsonNode readTree(Reader reader) throws IOException {
        return MAPPER.readTree(reader);
    }

    /** Parse JSON string to a tree. */
    public static JsonNode readTree(String json) throws IOException {
        return MAPPER.readTree(json);
    }

    /**
     * Validate JSON node against the employee schema.
     * @param node parsed JSON
     * @throws ValidationException if validation fails, with details from the schema validator
     */
    public static void validateEmployeeSchema(JsonNode node) {
        Set<ValidationMessage> errors = getEmployeeSchema().validate(node);
        if (!errors.isEmpty()) {
            List<String> details = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.toList());
            LOG.warn("Employee JSON schema validation failed: {} error(s)", details.size());
            LOG.debug("Validation errors: {}", details);
            throw new ValidationException("Request body does not match employee schema", details);
        }
        LOG.trace("Employee schema validation passed");
    }

    /**
     * Parse request body to JsonNode, validate against employee schema, then deserialize to Employee.
     * Use this for POST/PUT employee payloads.
     * @throws InvalidJsonException if the body is not valid JSON
     * @throws ValidationException if the body does not match the employee schema
     */
    public static <T> T fromJsonWithEmployeeValidation(Reader reader, Class<T> clazz) throws IOException {
        LOG.debug("Reading and validating JSON for {}", clazz.getSimpleName());
        JsonNode node;
        try {
            node = MAPPER.readTree(reader);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            LOG.warn("Invalid JSON: {}", e.getMessage());
            throw new InvalidJsonException("Invalid JSON: " + e.getMessage(), e);
        }
        if (node == null || node.isMissingNode()) {
            LOG.warn("Empty or missing JSON body");
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
    /**
     * Normalize hobbies array: convert string items to {"name": "<string>"} so that
     * both ["Reading", "Gaming"] and [{"name": "Reading"}] are accepted and deserialize correctly.
     */
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

    private static JsonSchema getEmployeeSchema() {
        if (employeeSchema == null) {
            synchronized (JsonUtil.class) {
                if (employeeSchema == null) {
                    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
                    try (InputStream is = JsonUtil.class.getResourceAsStream(EMPLOYEE_SCHEMA_PATH)) {
                        if (is == null) {
                            LOG.fatal("Employee schema not found on classpath: {}", EMPLOYEE_SCHEMA_PATH);
                            throw new IllegalStateException("Schema not found: " + EMPLOYEE_SCHEMA_PATH);
                        }
                        employeeSchema = factory.getSchema(is);
                        LOG.info("Loaded employee JSON schema from {}", EMPLOYEE_SCHEMA_PATH);
                    } catch (IOException e) {
                        LOG.error("Failed to load employee schema", e);
                        throw new IllegalStateException("Failed to load schema", e);
                    }
                }
            }
        }
        return employeeSchema;
    }
    //</editor-fold>
}
