package exception;

public class UnauthorizedException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Unauthorized";
    private static final int DEFAULT_STATE_CODE = 401;

    public UnauthorizedException() {
        super(DEFAULT_MESSAGE, DEFAULT_STATE_CODE);
    }

}
