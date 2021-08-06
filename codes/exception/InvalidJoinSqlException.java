package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:56
 */
public class InvalidJoinSqlException extends InvalidSqlException {
    public InvalidJoinSqlException() {
    }

    public InvalidJoinSqlException(String message) {
        super(message);
    }
}
