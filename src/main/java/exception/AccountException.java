package exception;

public class AccountException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";

    public AccountException() {
        super(DEFAULT_MESSAGE);
    }
}
