package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:53
 */
public class InvalidUseSqlException extends InvalidSqlException {
    public InvalidUseSqlException() {
    }

    public InvalidUseSqlException(String message) {
        super(message);
    }
}
