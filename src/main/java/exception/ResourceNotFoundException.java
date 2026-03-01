package exception;

/**
 * Thrown when a requested resource (e.g. employee by id) does not exist.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
