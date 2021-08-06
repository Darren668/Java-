package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:54
 */
public class InvalidInsertSqlException extends InvalidSqlException {
    public InvalidInsertSqlException() {
    }

    public InvalidInsertSqlException(String message) {
        super(message);
    }
}
