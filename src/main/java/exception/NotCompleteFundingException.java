package exception;

public class NotCompleteFundingException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Not Complete Funding";
    private static final int DEFAULT_STATE_CODE = 409;

    public NotCompleteFundingException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public NotCompleteFundingException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public NotCompleteFundingException(String message) {
        super(message,  DEFAULT_STATE_CODE);
    }

    public NotCompleteFundingException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
