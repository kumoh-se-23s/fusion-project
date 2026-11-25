package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class InvalidRequestException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "잘못된 요청";

    private static final HttpStatus STATUS_CODE = HttpStatus.BAD_REQUEST;

    public InvalidRequestException() {
        super(STATUS_CODE, DEFAULT_MESSAGE);
    }

    public InvalidRequestException(String message) {
        super(STATUS_CODE, message);
    }

    public InvalidRequestException(String message, HttpStatus status) {
        super(status, message);
    }

}