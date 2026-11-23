package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class InvalidTokenException extends BusinessException{
    private static final String MESSAGE = "유효하지 않은 토큰";
    private static final HttpStatus DEFAULT_STATE_CODE = HttpStatus.FORBIDDEN;

    public InvalidTokenException() {
        super(DEFAULT_STATE_CODE, MESSAGE);
    }
}
