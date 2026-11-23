package exception;

public class InvalidTokenException extends BusinessException{
    private static final String MESSAGE = "유효하지 않은 토큰";
    private static final int DEFAULT_STATE_CODE = 403;

    public InvalidTokenException() {
        super(MESSAGE,DEFAULT_STATE_CODE);
    }
}
