package exception;

public abstract class StructureException extends AppException {
    public StructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public StructureException(String message) {
        super(message);
    }
}
