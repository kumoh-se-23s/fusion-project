package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class AccountException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";

    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.BAD_REQUEST;

    public AccountException() {
        super(DEFAULT_STATE_CODE, DEFAULT_MESSAGE);
    }
    public AccountException(Object message) {
        super(DEFAULT_STATE_CODE, message);
    }
    public AccountException(HttpStatus httpStatus) {
        super(httpStatus, DEFAULT_MESSAGE);
    }
    public AccountException(HttpStatus httpStatus, Object message) {
        super(httpStatus, message);
    }
}
