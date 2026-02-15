package dto;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.annotation.JsonInclude;
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
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public ApiError(String error, String message) {
        this.error = error;
        this.message = message;
    }
    //</editor-fold>

    //<editor-fold desc="Getters and Setters">
    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
    //</editor-fold>
}
