package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class EmptyImageException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Empty Images";
    private static final HttpStatus DEFAULT_STATUS_CODE = HttpStatus.NOT_FOUND;
    public EmptyImageException() {
        super(DEFAULT_STATUS_CODE, DEFAULT_MESSAGE);
    }
    public EmptyImageException(Object message) {
        super(DEFAULT_STATUS_CODE, message);
    }
    public EmptyImageException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public EmptyImageException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
