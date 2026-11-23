package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class InvalidStateException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Invalid Project State";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.BAD_REQUEST;

    public InvalidStateException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public InvalidStateException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public InvalidStateException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public InvalidStateException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }

}
