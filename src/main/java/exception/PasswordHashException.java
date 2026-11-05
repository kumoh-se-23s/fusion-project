package exception;

public class PasswordHashException extends AppException{
    private static final String DEFAULT_MESSAGE = "비밃번호 암호화 설정에 문제가 있습니다.";

    public PasswordHashException() {
        super(DEFAULT_MESSAGE);
    }
}
