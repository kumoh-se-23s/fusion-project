package exception;

public class AlreadyExistException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Already Exist Data";
    private static final int DEFAULT_STATE_CODE = 409;

    public AlreadyExistException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public AlreadyExistException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public AlreadyExistException(String message) {
        super(message,  DEFAULT_STATE_CODE);
    }

    public AlreadyExistException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
