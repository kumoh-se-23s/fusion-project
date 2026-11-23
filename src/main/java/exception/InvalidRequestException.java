package exception;

import com.minihttp.packet.protocol.HttpStatus;

//입력 누락을 처리할
public class InvalidRequestException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "잘못된 요청";

    private static final HttpStatus STATUS_CODE = HttpStatus.BAD_REQUEST;

    public InvalidRequestException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidRequestException(String message) {
        super(message);
    }
}