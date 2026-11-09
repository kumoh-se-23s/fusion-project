package exception;

public class InvalidUserException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Invalid User";
    private static final int DEFAULT_STATE_CODE = 409;

    public InvalidUserException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public InvalidUserException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public InvalidUserException(String message) {
        super(message, DEFAULT_STATE_CODE);
    }

    public InvalidUserException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }

}
