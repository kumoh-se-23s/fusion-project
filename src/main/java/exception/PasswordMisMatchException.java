package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class PasswordMisMatchException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "비밀번호가 일치하지 않습니다.";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.BAD_REQUEST;

    public PasswordMisMatchException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public PasswordMisMatchException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public PasswordMisMatchException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public PasswordMisMatchException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
