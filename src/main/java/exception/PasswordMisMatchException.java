package exception;

public class PasswordMisMatchException extends BusinessException{
    private static final String DEFAULT_MESSAGE = "비밀번호가 일치하지 않습니다.";

    public PasswordMisMatchException() {
        super(DEFAULT_MESSAGE);
    }

    //로그인(기본값)과 비밀번호 변경(400)처럼 상황에 따라 응답 코드를 고를 수 있다
    public PasswordMisMatchException(int stateCode) {
        super(DEFAULT_MESSAGE, stateCode);
    }
}
