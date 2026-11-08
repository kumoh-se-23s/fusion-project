package exception;

public class InvalidMakerException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Invalid Maker";
    private static final int DEFAULT_STATE_CODE = 409;

    public InvalidMakerException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public InvalidMakerException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public InvalidMakerException(String message) {
        super(message, DEFAULT_STATE_CODE);
    }

    public InvalidMakerException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }

}
