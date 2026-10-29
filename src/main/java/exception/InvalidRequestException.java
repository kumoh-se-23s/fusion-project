package exception;

public class InvalidRequestException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Invalid Request";
    private static final int DEFAULT_STATE_CODE = 400;

    public InvalidRequestException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public InvalidRequestException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public InvalidRequestException(String message) {
        super(message,  DEFAULT_STATE_CODE);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
