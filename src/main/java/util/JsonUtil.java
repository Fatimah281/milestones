package util;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
//</editor-fold>

public final class JsonUtil {

    //<editor-fold desc="Fields">
    private static final Logger LOG = LogManager.getLogger(JsonUtil.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new JavaTimeModule());

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
        validateDateOfBirth(node);
        validatePhoneNumber(node);
        validateNoDuplicateHobbies(node);
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
                String msg = m.getMessage();
                if (msg != null && msg.contains("dateOfBirth")) {
                    msg = "dateOfBirth: must be YYYY-MM-DD (e.g. 1990-05-15). Month must be 01-12 (there is no 13th month). Day must be valid for the month (e.g. 01-31 for January, 01-30 for April).";
                }
                details.add(msg);
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
                ObjectNode obj = MAPPER.createObjectNode().put("name", item.asText().trim());
                arr.set(i, obj);
                LOG.trace("Normalized hobby[{}] from string to object", i);
            } else if (item.isObject() && item.has("name")) {
                String name = item.get("name").asText(null);
                if (name != null) {
                    ((ObjectNode) item).put("name", name.trim());
                }
            }
        }
    }

    private static final LocalDate MIN_BIRTH_DATE = LocalDate.of(1900, 1, 1);

    private static void validateDateOfBirth(JsonNode node) {
        JsonNode dob = node.path("dateOfBirth");
        if (dob.isMissingNode() || dob.isNull() || !dob.isTextual()) return;
        String value = dob.asText().trim();
        if (value.isEmpty()) return;
        if (!value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            LOG.error("Invalid dateOfBirth format: {}", value);
            throw new ValidationException(
                    "Invalid dateOfBirth: must be YYYY-MM-DD (e.g. 1990-05-15). Month must be 01-12 (there is no 13th month). Day must be valid for the month.",
                    List.of("dateOfBirth: " + value));
        }
        LocalDate parsed;
        try {
            parsed = LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            String friendly = friendlyDateErrorMessage(e, value);
            LOG.error("Invalid dateOfBirth: {} - {}", value, friendly);
            throw new ValidationException("Invalid dateOfBirth: " + friendly, List.of("dateOfBirth: " + value));
        }
        if (parsed.isBefore(MIN_BIRTH_DATE)) {
            LOG.error("dateOfBirth too old: {}", value);
            throw new ValidationException(
                    "Invalid dateOfBirth: must be on or after 1900-01-01 (was " + value + ")",
                    List.of("dateOfBirth: " + value));
        }
        if (parsed.isAfter(LocalDate.now())) {
            LOG.error("dateOfBirth in future: {}", value);
            throw new ValidationException(
                    "Invalid dateOfBirth: cannot be in the future (was " + value + ")",
                    List.of("dateOfBirth: " + value));
        }
    }

    /** Phone must start with 0, 5, or 9 (then digits), or start with 66 (then digits). Null/empty allowed. */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:[059][0-9]+|66[0-9]*)$");

    private static void validatePhoneNumber(JsonNode node) {
        JsonNode phone = node.path("phoneNumber");
        if (phone.isMissingNode() || phone.isNull() || !phone.isTextual()) return;
        String value = phone.asText().trim();
        if (value.isEmpty()) return;
        if (!PHONE_PATTERN.matcher(value).matches()) {
            LOG.error("Invalid phoneNumber: {}", value);
            throw new ValidationException(
                    "Invalid phoneNumber: must start with 0, 5, or 9, or start with 66 (digits only)",
                    List.of("phoneNumber: " + value));
        }
    }

    private static String friendlyDateErrorMessage(DateTimeParseException e, String value) {
        String errMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (errMsg.contains("monthofyear") || errMsg.contains("month")) {
            return "month must be 01-12 (there is no 13th month). Value was: " + value;
        }
        if (errMsg.contains("dayofmonth") || errMsg.contains("day")) {
            return "day must be valid for the month (e.g. 01-31 for January, 01-30 for April, 01-28 for February). Value was: " + value;
        }
        if (errMsg.contains("year")) {
            return "year must be valid (1900-9999). Value was: " + value;
        }
        return "must be YYYY-MM-DD (e.g. 1990-05-15). Month 01-12, day valid for the month. Value was: " + value;
    }

    private static void validateNoDuplicateHobbies(JsonNode root) {
        JsonNode hobbies = root.path("hobbies");
        if (!hobbies.isArray()) return;
        Set<String> seen = new LinkedHashSet<>();
        List<String> duplicates = new ArrayList<>();
        for (int i = 0; i < hobbies.size(); i++) {
            JsonNode item = hobbies.get(i);
            String name = null;
            if (item.isObject() && item.has("name")) {
                name = item.get("name").asText(null);
            } else if (item.isTextual()) {
                name = item.asText();
            }
            if (name != null && !name.isBlank()) {
                String key = name.trim().toLowerCase();
                if (!seen.add(key)) {
                    if (!duplicates.contains(name.trim())) {
                        duplicates.add(name.trim());
                    }
                }
            }
        }
        if (!duplicates.isEmpty()) {
            LOG.error("Duplicate hobby names: {}", duplicates);
            throw new ValidationException(
                    "Duplicate hobby names are not allowed: " + String.join(", ", duplicates),
                    List.of("hobbies: duplicate names: " + String.join(", ", duplicates)));
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
