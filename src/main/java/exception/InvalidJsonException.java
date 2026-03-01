package exception;

/**
 * Thrown when request body is not valid JSON or cannot be parsed.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidJsonException extends RuntimeException {

    public InvalidJsonException(String message) {
        super(message);
    }

    public InvalidJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
