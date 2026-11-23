package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class AlreadyExistException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Already Exist Data";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.CONFLICT;

    public AlreadyExistException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public AlreadyExistException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }

    public AlreadyExistException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public AlreadyExistException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
