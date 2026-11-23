package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class NotFoundException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="NOT FOUND";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.NOT_FOUND;

    public NotFoundException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public NotFoundException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public NotFoundException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public NotFoundException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
