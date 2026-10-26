package exception;

public class AnnotationMissingException extends StructureException {
    public AnnotationMissingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationMissingException(String message) {
        super(message);
    }
}
