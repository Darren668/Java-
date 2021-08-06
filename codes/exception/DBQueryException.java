package exception;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:31
 *
 * The following is the tree of exceptions in database query
 * 1.DBQueryException
 *      1)InvalidSqlException
 *              InvalidUseSqlException
 *              InvalidCreateSqlException
 *              InvalidDropSqlException
 *              InvalidAlterSqlException
 *              InvalidInsertSqlException
 *              InvalidSelectSqlException
 *              InvalidUpdateSqlException
 *                      InvalidSetException
 *              InvalidDeleteSqlException
 *              InvalidJoinSqlException
 *
 *              InvalidWhereConditionException
 *              CommandTypeNotFoundException
 *              AttributeNotFoundException
 *      2)DatabaseNotChosenException
 *
 *      3)InvalidLiteralException
 *               InvalidNameException
 *               InvalidStringLiteralException
 *
 * 2.FileNotFoundException(JDK)
 *              TableFileNotFoundException
 *              DataBaseFileNotFoundException
 *
 * 3.FileAlreadyExistedException(JDK)
 *              TableFileExistedException
 *              DatabaseFileExistedException
 *
 * 4.NumberFormatException(JDK)
 *
 */
public class DBQueryException extends Exception {
    public DBQueryException() {
    }

    public DBQueryException(String message) {
        super(message);
    }
}
