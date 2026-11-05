package exception;
//입력 누락을 처리할
public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(String message) {
        super(message);
    }
}