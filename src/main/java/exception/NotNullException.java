package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class NotNullException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Not Null Value";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.BAD_REQUEST;

    public NotNullException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public NotNullException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public NotNullException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public NotNullException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
