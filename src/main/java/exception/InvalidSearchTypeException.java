package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class InvalidSearchTypeException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Invalid Search Type";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.BAD_REQUEST;

    public InvalidSearchTypeException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public InvalidSearchTypeException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public InvalidSearchTypeException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public InvalidSearchTypeException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
