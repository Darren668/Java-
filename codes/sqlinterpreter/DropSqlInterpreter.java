package sqlinterpreter;

import exception.DatabaseNotChosenException;
import exception.InvalidDropSqlException;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-17-17:14
 */
public class DropSqlInterpreter extends BaseSqlInterpreter{
    private Pattern dropTablePattern = Pattern.compile("drop\\s+table\\s(.+)",Pattern.CASE_INSENSITIVE);
    private Pattern dropDatabasePattern = Pattern.compile("drop\\s+database\\s(.+)",Pattern.CASE_INSENSITIVE);
    public DropSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {

        Matcher dropTableMatcher = dropTablePattern.matcher(sql);
        if(dropTableMatcher.find()){
            dropTable(dropTableMatcher);
            return;
        }
        Matcher dropDatabaseMatcher = dropDatabasePattern.matcher(sql);
        if(dropDatabaseMatcher.find()){
            dropDatabase(dropDatabaseMatcher);
            return;
        }
        //exception
        throw new InvalidDropSqlException("Invalid DROP sql");
    }

    private void dropTable(Matcher dropTableMatcher) throws Exception {
        if(curDatabase == null){
            throw new DatabaseNotChosenException("Database has not been chosen");
        }
        String tableName = dropTableMatcher.group(1).trim();
        checkName(tableName);
        //check the validation of file
        File tableFile = getTableFile(curDatabase+"\\"+tableName + ".tab");
        //delete the target table
        if(tableFile.isFile()){
            tableFile.delete();
        }
    }

    private void dropDatabase(Matcher dropDatabaseMatcher) throws Exception {
        String databaseName = dropDatabaseMatcher.group(1).trim();
        checkName(databaseName);
        File databaseDirectory = getDatabaseFile(databaseName);
        File[] files = databaseDirectory.listFiles();
        for(File file : files){
            if(file!=null && file.isFile()){
                file.delete();
            }
        }
        if(databaseDirectory.isDirectory()){
            databaseDirectory.delete();
        }
        //after dropping the database, the current database should be set null
        curDatabase = null;
    }

}
