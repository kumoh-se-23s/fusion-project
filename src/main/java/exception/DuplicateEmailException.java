package exception;

public class DuplicateEmailException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "중복된 이메일입니다";

    public DuplicateEmailException() {
        super(DEFAULT_MESSAGE);
    }
}
