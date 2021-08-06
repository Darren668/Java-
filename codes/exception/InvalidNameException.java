package exception;

/**
 * @author xinhaojie
 * @create 2021-03-19-22:06
 */
public class InvalidNameException extends InvalidLiteralException{
    public InvalidNameException() {
    }

    public InvalidNameException(String message) {
        super(message);
    }
}
