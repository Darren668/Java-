package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:57
 */
public class InvalidWhereConditionException extends InvalidSqlException {
    public InvalidWhereConditionException() {
    }

    public InvalidWhereConditionException(String message) {
        super(message);
    }
}
