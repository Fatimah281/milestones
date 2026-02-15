package util;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
//</editor-fold>

/**
 * Validates JSON request payloads against JSON Schema before database operations.
 * Modular: add new schema paths and reuse validate(json, schemaPath).
 */
public final class JsonSchemaValidator {

    //<editor-fold desc="Constants">
    private static final String SCHEMA_BASE = "/schemas/";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    //</editor-fold>

    //<editor-fold desc="Constructors">
    private JsonSchemaValidator() {
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    /**
     * Validates JSON string against the schema at the given resource path.
     *
     * @param json       raw JSON request body
     * @param schemaPath resource path under /schemas/ (e.g. "employee.schema.json")
     * @return empty set if valid; otherwise set of validation error messages
     */
    public static Set<String> validate(String json, String schemaPath) throws IOException {
        if (json == null || json.isBlank()) {
            return Set.of("Request body is required");
        }
        String path = schemaPath.startsWith("/") ? schemaPath : SCHEMA_BASE + schemaPath;
        try (InputStream is = JsonSchemaValidator.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Schema not found: " + path);
            }
            JsonSchema schema = SCHEMA_FACTORY.getSchema(is);
            JsonNode node = MAPPER.readTree(json);
            Set<ValidationMessage> errors = schema.validate(node);
            return errors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Validates JSON string against the employee schema.
     */
    public static Set<String> validateEmployee(String json) throws IOException {
        return validate(json, "employee.schema.json");
    }
    //</editor-fold>
}
