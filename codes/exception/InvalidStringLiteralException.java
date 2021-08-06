package exception;

/**
 * @author xinhaojie
 * @create 2021-03-19-22:08
 */
public class InvalidStringLiteralException extends InvalidLiteralException {
    public InvalidStringLiteralException() {
    }

    public InvalidStringLiteralException(String message) {
        super(message);
    }
}
