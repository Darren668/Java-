package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:57
 */
public class AttributeNotFoundException extends InvalidSqlException {
    public AttributeNotFoundException() {
    }

    public AttributeNotFoundException(String message) {
        super(message);
    }
}
