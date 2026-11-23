package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class DuplicateIdException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "중복된 아이디입니다.";

    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.BAD_REQUEST;

    public DuplicateIdException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public DuplicateIdException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public DuplicateIdException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public DuplicateIdException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
