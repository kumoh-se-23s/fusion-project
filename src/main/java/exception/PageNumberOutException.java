package exception;

public class PageNumberOutException extends DatabaseException {
    private static final String DEFAULT_MESSAGE ="Page Number/Size is Out of range";

    public PageNumberOutException() {
        super(DEFAULT_MESSAGE);
    }
    public PageNumberOutException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
    public PageNumberOutException(String message) {
        super(message);
    }

    public PageNumberOutException(String message, Throwable cause) {
        super(message, cause);
    }


}
