package exception;

public class InvalidStateException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Invalid Project State";
    private static final int DEFAULT_STATE_CODE = 400;

    public InvalidStateException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public InvalidStateException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public InvalidStateException(String message) {
        super(message, DEFAULT_STATE_CODE);
    }

    public InvalidStateException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }

}
