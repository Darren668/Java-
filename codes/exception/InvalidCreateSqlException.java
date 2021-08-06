package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:54
 */
public class InvalidCreateSqlException extends InvalidSqlException {
    public InvalidCreateSqlException() {
    }

    public InvalidCreateSqlException(String message) {
        super(message);
    }
}
