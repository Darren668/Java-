package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:52
 */
public class InvalidSqlException extends DBQueryException {
    public InvalidSqlException() {
    }

    public InvalidSqlException(String message) {
        super(message);
    }
}
