package exception;

public class LimitNumberOutException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Limit Number is Out of range";
    private static final int DEFAULT_STATE_CODE = 400;

    public LimitNumberOutException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public LimitNumberOutException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public LimitNumberOutException(String message) {
        super(message, DEFAULT_STATE_CODE);
    }

    public LimitNumberOutException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }

}
