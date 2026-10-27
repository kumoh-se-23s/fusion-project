package exception;

public class LazyInitializationException extends DatabaseException {
    public LazyInitializationException(String message) {
        super(message);
    }

    public LazyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
