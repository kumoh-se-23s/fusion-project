package exception;

//입력 누락을 처리할
public class InvalidRequestException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "잘못된 요청";

    public InvalidRequestException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidRequestException(String message) {
        super(message);
    }

}