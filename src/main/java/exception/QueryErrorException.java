package exception;

public class QueryErrorException extends DatabaseException {
    public QueryErrorException(String message) {
        super(message);
    }

    public QueryErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
