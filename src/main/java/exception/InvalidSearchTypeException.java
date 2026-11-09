package exception;

public class InvalidSearchTypeException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Invalid Search Type";
    private static final int DEFAULT_STATE_CODE = 400;

    public InvalidSearchTypeException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public InvalidSearchTypeException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public InvalidSearchTypeException(String message) {
        super(message, DEFAULT_STATE_CODE);
    }

    public InvalidSearchTypeException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }

}
