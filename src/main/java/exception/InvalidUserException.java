package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class InvalidUserException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Invalid User Info";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.FORBIDDEN;

    public InvalidUserException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public InvalidUserException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public InvalidUserException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public InvalidUserException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
