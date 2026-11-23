package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class UnauthorizedException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Unauthorized";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.UNAUTHORIZED;

    public UnauthorizedException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public UnauthorizedException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public UnauthorizedException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public UnauthorizedException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }

}
