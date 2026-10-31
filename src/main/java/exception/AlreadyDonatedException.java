package exception;

public class AlreadyDonatedException extends BusinessException {

    private static final String DEFAULT_MESSAGE =
            "이미 후원한 프로젝트입니다.";

    public AlreadyDonatedException() {
        super(DEFAULT_MESSAGE);
    }

    public AlreadyDonatedException(String message) {
        super(message);
    }
}