package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class NotCompleteProjectException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Not Complete Project";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.CONFLICT;

    public NotCompleteProjectException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public NotCompleteProjectException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public NotCompleteProjectException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public NotCompleteProjectException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
