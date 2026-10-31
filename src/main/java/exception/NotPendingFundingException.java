package exception;

public class NotPendingFundingException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Not Pending Funding";
    private static final int DEFAULT_STATE_CODE = 409;

    public NotPendingFundingException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
    public NotPendingFundingException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause, DEFAULT_STATE_CODE);
    }
    public NotPendingFundingException(String message) {
        super(message,  DEFAULT_STATE_CODE);
    }

    public NotPendingFundingException(String message, Throwable cause) {
        super(message, cause, DEFAULT_STATE_CODE);
    }
}
