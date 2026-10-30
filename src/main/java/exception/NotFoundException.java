package exception;

public class NotFoundException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="NOT FOUND";
    private static final int DEFAULT_STATE_CODE = 404;
    public NotFoundException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }

    public NotFoundException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }

    public NotFoundException(String message) {
        super(message, DEFAULT_STATE_CODE);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
