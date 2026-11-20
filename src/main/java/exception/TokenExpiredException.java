package exception;

public class TokenExpiredException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "Token Expired";
    private static final int DEFAULT_STATE_CODE = 401;

    public TokenExpiredException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }
}
