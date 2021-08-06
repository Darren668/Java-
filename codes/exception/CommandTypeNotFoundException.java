package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:57
 */
public class CommandTypeNotFoundException extends InvalidSqlException {
    public CommandTypeNotFoundException() {
    }

    public CommandTypeNotFoundException(String message) {
        super(message);
    }
}
