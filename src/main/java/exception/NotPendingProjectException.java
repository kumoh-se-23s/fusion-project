package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class NotPendingProjectException extends BusinessException {
    private static final String DEFAULT_MESSAGE ="Not Pending Project";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.CONFLICT;

    public NotPendingProjectException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public NotPendingProjectException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public NotPendingProjectException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public NotPendingProjectException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
