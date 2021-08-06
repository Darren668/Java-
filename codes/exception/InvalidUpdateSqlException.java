package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:55
 */
public class InvalidUpdateSqlException extends InvalidSqlException {
    public InvalidUpdateSqlException() {
    }

    public InvalidUpdateSqlException(String message) {
        super(message);
    }
}
