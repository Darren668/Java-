package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:56
 */
public class InvalidDeleteSqlException extends InvalidSqlException {
    public InvalidDeleteSqlException() {
    }

    public InvalidDeleteSqlException(String message) {
        super(message);
    }
}
