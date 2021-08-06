package exception;

/**
 * @author xinhaojie
 * @create 2021-03-19-22:09
 */
public class InvalidLiteralException extends DBQueryException {
    public InvalidLiteralException() {
    }

    public InvalidLiteralException(String message) {
        super(message);
    }
}
