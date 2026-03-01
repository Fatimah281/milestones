package DTO;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
//</editor-fold>

/**
 * Standard JSON error response for API consumers.
 * Never exposes stack traces; for internal debugging use logs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    //<editor-fold desc="Fields">
    private final String error;
    private final String message;
    private final List<String> details;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public ApiError(String error, String message) {
        this.error = error;
        this.message = message;
        this.details = null;
    }

    public ApiError(String error, String message, List<String> details) {
        this.error = error;
        this.message = message;
        this.details = details;
    }
    //</editor-fold>

    //<editor-fold desc="Getters and Setters">
    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }
    //</editor-fold>
}
