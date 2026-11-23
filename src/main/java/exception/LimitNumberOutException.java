package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class LimitNumberOutException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Limit Number is Out of range";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.BAD_REQUEST;

    public LimitNumberOutException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public LimitNumberOutException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public LimitNumberOutException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public LimitNumberOutException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
