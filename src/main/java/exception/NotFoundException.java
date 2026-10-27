package exception;

public class NotFoundException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="NOT FOUND";

    public NotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    public NotFoundException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
