package exception;

public class DuplicateIdException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "중복된 아이디입니다.";

    public DuplicateIdException() {
        super(DEFAULT_MESSAGE);
    }

}
