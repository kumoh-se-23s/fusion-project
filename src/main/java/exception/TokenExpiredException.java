package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class TokenExpiredException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "Token Expired";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.FORBIDDEN;

    public TokenExpiredException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public TokenExpiredException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public TokenExpiredException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public TokenExpiredException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
