package exception;

import com.minihttp.core.route.exception.HttpStatusException;
import com.minihttp.packet.protocol.HttpHeaderField;
import com.minihttp.packet.protocol.HttpStatus;
import lombok.Getter;
import lombok.NonNull;
import model.JsonSerializable;

import java.util.Map;

//비즈니스 로직이 발생했을때 발생시키는 에러 -> 프론트와 소통을 위해 stateCode를 가짐
@Getter
public abstract class BusinessException extends HttpStatusException implements JsonSerializable {
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public BusinessException() {
        super(DEFAULT_STATUS);
    }
    public BusinessException(Object detail) {
        super(DEFAULT_STATUS, detail);
    }
    public BusinessException(Object detail, @NonNull Map<HttpHeaderField, String> headers) {
        super(DEFAULT_STATUS, detail, headers);
    }

    public BusinessException(@NonNull HttpStatus status) {
        super(status);
    }

    public BusinessException(@NonNull HttpStatus status, Object detail) {
        super(status, detail);
    }

    public BusinessException(@NonNull HttpStatus status, Object detail, @NonNull Map<HttpHeaderField, String> headers) {
        super(status, detail, headers);
    }


    @Override
    public String toJsonString() {
        return "{"
                + "\"exception\":" + quote(getClass().getName()) + ","
                + "\"message\":" + quote(getMessage()) + ","
                + "\"stateCode\":" + status().code() + " " + status().reason()
                + "}";
    }

    protected static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }

}
