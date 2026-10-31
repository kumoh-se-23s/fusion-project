package exception;

public class InvalidException extends AppException{
    private static final String DEFAULT_MESSAGE = "유효하지 않은 오류";

    public InvalidException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidException(String message) {
        super(message);
    }
}
