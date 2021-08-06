package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-16:01
 */
public class DatabaseNotChosenException extends DBQueryException {
    public DatabaseNotChosenException() {
    }

    public DatabaseNotChosenException(String message) {
        super(message);
    }
}
