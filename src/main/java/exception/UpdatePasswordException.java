package exception;

import com.minihttp.packet.protocol.HttpStatus;

public class UpdatePasswordException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "비밀번호 변경 실패";
    private static final HttpStatus STATUS_CODE = HttpStatus.BAD_REQUEST;

    public UpdatePasswordException(String message) {
        super(message);
    }
}
