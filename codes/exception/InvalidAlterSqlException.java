package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:54
 */
public class InvalidAlterSqlException extends InvalidSqlException {
    public InvalidAlterSqlException() {
    }

    public InvalidAlterSqlException(String message) {
        super(message);
    }
}
