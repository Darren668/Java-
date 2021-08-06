package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:54
 */
public class InvalidDropSqlException extends InvalidSqlException {
    public InvalidDropSqlException() {
    }

    public InvalidDropSqlException(String message) {
        super(message);
    }
}
