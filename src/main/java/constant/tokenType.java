package constant;

public enum tokenType {
    ACCESS("access"),

    REFRESH("refresh");

    private final String format;

    tokenType(String format) {
        this.format = format;
    }
}
