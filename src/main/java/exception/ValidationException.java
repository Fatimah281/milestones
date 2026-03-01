package exception;

import java.util.Collections;
import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<String> details;

    public ValidationException(String message) {
        super(message);
        this.details = null;
    }

    public ValidationException(String message, List<String> details) {
        super(message);
        this.details = details == null ? null : Collections.unmodifiableList(details);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.details = null;
    }

    public List<String> getDetails() {
        return details;
    }
}
