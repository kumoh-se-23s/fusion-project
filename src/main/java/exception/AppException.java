package exception;


import model.JsonSerializable;

public abstract class AppException extends RuntimeException implements JsonSerializable {


    public AppException(String message) {
        this(message, null);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toJsonString() {
        return "{"
                + "\"exception\":" + quote(getClass().getName()) + ","
                + "\"message\":" + quote(getMessage()) + ","
                + "\"cause\":" + quote(getCause() == null ? null : getCause().toString())
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
