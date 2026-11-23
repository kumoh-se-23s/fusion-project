package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class AlreadyDonatedException extends BusinessException {

    private static final String DEFAULT_MESSAGE =
            "이미 후원한 프로젝트입니다.";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.CONFLICT;

    public AlreadyDonatedException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public AlreadyDonatedException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public AlreadyDonatedException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public AlreadyDonatedException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}