package exception;

public class NotCompleteProjectException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Not Complete Project";
    private static final int DEFAULT_STATE_CODE = 409;

    public NotCompleteProjectException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public NotCompleteProjectException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public NotCompleteProjectException(String message) {
        super(message,  DEFAULT_STATE_CODE);
    }

    public NotCompleteProjectException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
