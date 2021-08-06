package sqlinterpreter;

import exception.CommandTypeNotFoundException;
import exception.InvalidSqlException;

import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-13-19:24
 */
public class SqlClassification {
    private Pattern selectSqlPattern = Pattern.compile("^select\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern useSqlPattern = Pattern.compile("^use\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern createSqlPattern = Pattern.compile("^create\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern insertSqlPattern = Pattern.compile("^insert\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern updateSqlPattern = Pattern.compile("^update\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern deleteSqlPattern = Pattern.compile("^delete\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern dropSqlPattern = Pattern.compile("^drop\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern alterSqlPattern = Pattern.compile("^alter\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern joinSqlPattern = Pattern.compile("^join\\s(.+)", Pattern.CASE_INSENSITIVE);

    public SqlClassification() {
    }

    public BaseSqlInterpreter getSpecificInterpreter(String originalSql) throws Exception {
        //do some process on sql
        String sql = processSemiColon(originalSql);
        if (isUseSql(sql)) {
            return new UseSqlInterpreter(sql);
        }
        if (isCreateSql(sql)) {
            return new CreateSqlInterpreter(sql);
        }
        if (isInsertSql(sql)) {
            return new InsertSqlInterpreter(sql);
        }
        if (isSelectSql(sql)) {
            return new SelectSqlInterpreter(sql);
        }
        if (isUpdateSql(sql)) {
            return new UpdateSqlInterpreter(sql);
        }
        if (isDeleteSql(sql)) {
            return new DeleteSqlInterpreter(sql);
        }
        if (isDropSql(sql)) {
            return new DropSqlInterpreter(sql);
        }
        if (isAlterSql(sql)) {
            return new AlterSqlInterpreter(sql);
        }
        if (isJoinSql(sql)) {
            return new JoinSqlInterpreter(sql);
        }
        //exception
        throw new CommandTypeNotFoundException("can not find the CommandType");
    }

    private boolean isSelectSql(String sql) {
        return selectSqlPattern.matcher(sql).find();
    }

    private boolean isUseSql(String sql) {
        return useSqlPattern.matcher(sql).find();
    }

    private boolean isCreateSql(String sql) {
        return createSqlPattern.matcher(sql).find();
    }

    private boolean isInsertSql(String sql) {
        return insertSqlPattern.matcher(sql).find();
    }

    private boolean isUpdateSql(String sql) {
        return updateSqlPattern.matcher(sql).find();
    }

    private boolean isDeleteSql(String sql) {
        return deleteSqlPattern.matcher(sql).find();
    }

    private boolean isDropSql(String sql) {
        return dropSqlPattern.matcher(sql).find();
    }

    private boolean isAlterSql(String sql) {
        return alterSqlPattern.matcher(sql).find();
    }

    private boolean isJoinSql(String sql) {
        return joinSqlPattern.matcher(sql).find();
    }

    /**some preparations are done here to make the sql more easy to deal with*/
    private String processSemiColon(String originalSql) throws Exception {
        //1.trim the spaces in the head and end
        String sql = originalSql.trim();
        //2.check if there is only one semi colon at the end
        int firstSemicolon = sql.indexOf(";");
        int lastSemicolon = sql.lastIndexOf(";");
        //remove the semi colon in order to deal with remaining main function sql
        if (firstSemicolon != lastSemicolon || lastSemicolon != sql.length() - 1) {
            //if there are more than one semi colon or no one, throw the InvalidSqlException
            throw new InvalidSqlException("Single semi colon expected at end of line");
        }
        sql = sql.substring(0, sql.length() - 1);
        //3.trim the spaces in the head and end again
        sql = sql.trim();
        return sql;

    }


}
