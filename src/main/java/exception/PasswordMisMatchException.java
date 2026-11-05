package exception;

public class PasswordMisMatchException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "비밀번호가 일치하지 않습니다.";

    public PasswordMisMatchException() {
        super(DEFAULT_MESSAGE);
    }
}
