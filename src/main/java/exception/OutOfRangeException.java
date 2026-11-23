package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class OutOfRangeException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Value is Out of range";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.BAD_REQUEST;

    public OutOfRangeException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }

    public OutOfRangeException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public OutOfRangeException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public OutOfRangeException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
