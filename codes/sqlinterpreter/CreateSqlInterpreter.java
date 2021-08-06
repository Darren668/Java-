package sqlinterpreter;

import exception.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-13-19:43
 */
public class CreateSqlInterpreter extends BaseSqlInterpreter {

    private Pattern createTablePattern = Pattern.compile("create\\s+table\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern createDatabasePattern = Pattern.compile("create\\s+database\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern columnPattern = Pattern.compile("table\\s(.+)\\((.+)\\)", Pattern.CASE_INSENSITIVE);
    private Pattern attributePattern = Pattern.compile("table\\s(.+)\\((.+)\\)", Pattern.CASE_INSENSITIVE);

    public CreateSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {
        Matcher createTableMatcher = createTablePattern.matcher(sql);
        if (createTableMatcher.find()) {
            createTable(createTableMatcher);
            return;
        }

        Matcher createDatabaseMatcher = createDatabasePattern.matcher(sql);
        if (createDatabaseMatcher.find()) {
            createDatabase(createDatabaseMatcher);
            return;
        }
        //exception
        throw new InvalidCreateSqlException("Invalid CREATE sql");
    }

    private void createDatabase(Matcher createDatabaseMatcher) throws Exception {
        String databaseName = createDatabaseMatcher.group(1).trim();
        checkName(databaseName);
        File file = new File(databaseName);
        if (file.exists()) {
            throw new DatabaseFileExistedException(databaseName + " existed, can not create again");
        }
        file.mkdir();

    }

    private void createTable(Matcher createTableMatcher) throws Exception {
        if (curDatabase == null) {
            throw new DatabaseNotChosenException("No database has been chosen");
        }
        //parse the sql, get the attributes in the () if there are attributes
        Matcher columnMatcher = columnPattern.matcher(sql);
        String tableName;
        tableName = columnMatcher.find() ? columnMatcher.group(1).trim() : createTableMatcher.group(1).trim();
        checkName(tableName);
        //check if the columnName is valid, if there is exception, the file won't be created
        String[] columnNames = null;
        Matcher attributeMatcher = attributePattern.matcher(sql);
        if (attributeMatcher.find()) {
            columnNames = attributeMatcher.group(2).split(",");
            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = columnNames[i].trim();
                checkName(columnNames[i]);
            }
        }
        File tableFile = new File(curDatabase + "\\" + tableName + ".tab");
        if (tableFile.exists()) {
            throw new TableFileExistedException(tableName + " existed, can not create again");
        }
        tableFile.createNewFile();
        //add columns to this new table according to attributes
        addNewAttribute(tableFile, columnNames);
    }

    private void addNewAttribute(File tableFile, String[] columnNames) throws Exception {
        //add 'id' column whenever
        BufferedWriter attributesWriter = null;
        try {
            FileWriter tableFileWriter = new FileWriter(tableFile);
            attributesWriter = new BufferedWriter(tableFileWriter);
            //once create a new table, add the id column
            attributesWriter.write("id\t");
            attributesWriter.flush();
            if (columnNames != null) {
                for (String columnName : columnNames) {
                    attributesWriter.write(columnName + "\t");
                }
                attributesWriter.flush();
            }
        } finally {
            if (attributesWriter != null) {
                attributesWriter.close();
            }
        }

    }


}
