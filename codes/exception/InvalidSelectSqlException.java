package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:55
 */
public class InvalidSelectSqlException extends InvalidSqlException {

    public InvalidSelectSqlException() {
    }

    public InvalidSelectSqlException(String message) {
        super(message);
    }
}
