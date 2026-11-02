package exception;

public class NotPendingProjectException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Not Pending Project";
    private static final int DEFAULT_STATE_CODE = 409;

    public NotPendingProjectException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public NotPendingProjectException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public NotPendingProjectException(String message) {
        super(message,  DEFAULT_STATE_CODE);
    }

    public NotPendingProjectException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
