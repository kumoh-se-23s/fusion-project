package exception;

import lombok.Getter;

//비즈니스 로직이 발생했을때 발생시키는 에러 -> 프론트와 소통을 위해 stateCode를 가짐
@Getter
public abstract class BusinessException extends AppException {
    private static final int DEFAULT_STATE_CODE = 510;

    @Override
    public String toJsonString() {
        return "{"
                + "\"exception\":" + quote(getClass().getName()) + ","
                + "\"message\":" + quote(getMessage()) + ","
                + "\"cause\":" + quote(getCause() == null ? null : getCause().toString())
                + "\"stateCode\":" + DEFAULT_STATE_CODE
                + "}";
    }

    private final int stateCode;

    public BusinessException(String message) {
        this(message, null, DEFAULT_STATE_CODE);
    }

    public BusinessException(String message, int stateCode) {
        this(message, null, stateCode);
    }

    public BusinessException(String message, Throwable cause) {
        this(message, cause, DEFAULT_STATE_CODE);
    }

    public BusinessException(String message, Throwable cause, int stateCode) {
        super(message, cause);
        this.stateCode = stateCode;
    }
}
