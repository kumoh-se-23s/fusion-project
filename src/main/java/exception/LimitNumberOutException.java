package exception;

public class LimitNumberOutException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Limit Number is Out of range";

    public LimitNumberOutException() {
        super(DEFAULT_MESSAGE);
    }
    public LimitNumberOutException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
    public LimitNumberOutException(String message) {
        super(message);
    }

    public LimitNumberOutException(String message, Throwable cause) {
        super(message, cause);
    }

}
