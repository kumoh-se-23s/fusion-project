package exception;

public class NotNullException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Not Null Value";
    private static final int DEFAULT_STATE_CODE = 400;

    public NotNullException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public NotNullException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public NotNullException(String message) {
        super(message,  DEFAULT_STATE_CODE);
    }

    public NotNullException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
