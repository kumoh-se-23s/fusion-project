package exception;

public class OutOfRangeException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Value is Out of range";
    private static final int DEFAULT_STATE_CODE = 400;

    public OutOfRangeException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public OutOfRangeException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public OutOfRangeException(String message) {
        super(message,  DEFAULT_STATE_CODE);
    }

    public OutOfRangeException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
