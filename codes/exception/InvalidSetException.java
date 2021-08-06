package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:55
 */
public class InvalidSetException extends InvalidUpdateSqlException {
    public InvalidSetException() {
    }

    public InvalidSetException(String message) {
        super(message);
    }
}
