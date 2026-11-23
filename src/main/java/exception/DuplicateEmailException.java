package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class DuplicateEmailException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "중복된 이메일입니다";
    private static final HttpStatus DEFAULT_STATUS_CODE = HttpStatus.BAD_REQUEST;
    public DuplicateEmailException() {
        super(DEFAULT_STATUS_CODE, DEFAULT_MESSAGE);
    }
    public DuplicateEmailException(Object message) {
        super(DEFAULT_STATUS_CODE, message);
    }
    public DuplicateEmailException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public DuplicateEmailException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
