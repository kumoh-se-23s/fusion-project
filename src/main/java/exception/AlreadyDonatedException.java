package exception;

public class AlreadyDonatedException extends BusinessException {

    private static final String message = "이미 후원한 프로젝트 입니다.";

    public AlreadyDonatedException() {
        super(message);
    }
}
