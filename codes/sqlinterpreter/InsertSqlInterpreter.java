package sqlinterpreter;

import exception.AttributeNotFoundException;
import exception.DatabaseNotChosenException;
import exception.InvalidInsertSqlException;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-13-19:46
 */
public class InsertSqlInterpreter extends BaseSqlInterpreter {
    private final String insertRegex = "insert\\s+into\\s(.+)\\svalues\\s*\\((.+)\\)";
    private Pattern insertSqlPattern = Pattern.compile(insertRegex, Pattern.CASE_INSENSITIVE);

    public InsertSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {
        if (curDatabase == null) {
            throw new DatabaseNotChosenException("No database has been chosen");
        }
        Matcher insertSqlMatcher = insertSqlPattern.matcher(sql);
        if (insertSqlMatcher.find()) {
            insertValues(insertSqlMatcher);
            return;
        }
        //exception
        throw new InvalidInsertSqlException("Invalid INSERT sql");
    }

    private void insertValues(Matcher insertSqlMatcher) throws Exception {
        //get the value of table name
        BufferedReader valuesReader = null;
        BufferedWriter valuesWriter = null;
        try {
            String tableName = insertSqlMatcher.group(1).trim();
            checkName(tableName);
            //get an instance of File according to table name and current database
            File tableFile = getTableFile(curDatabase + "\\" + tableName + ".tab");
            //calculate the last line id value by BufferedReader
            valuesReader = new BufferedReader(new FileReader(tableFile));
            valuesWriter = new BufferedWriter(new FileWriter(tableFile, true));
            //read the attributes line top line
            tableColumnNames = Arrays.asList(valuesReader.readLine().split("\t"));
            int nextRowId = 1;
            String tableRow = valuesReader.readLine();
            while (tableRow != null) {
                if (!"".equals(tableRow)) {
                    nextRowId = Integer.parseInt(tableRow.split("\t")[0]) + 1;
                }
                tableRow = valuesReader.readLine();
            }
            //get the values that need to be inserted
            String valueList = insertSqlMatcher.group(2).trim();
            String[] values = valueList.split(",");
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
                checkStringLiteral(values[i]);
            }
            //if the number of values in sql does not match the number in table(except id), exception
            if (values.length != tableColumnNames.size() - 1) {
                throw new AttributeNotFoundException("The numbers of attributes do not match to the table");
            }
            valuesWriter.newLine();
            valuesWriter.write(nextRowId + "\t");
            for (String value : values) {
                //keep the original data in case of literal Strings comparison
                valuesWriter.write(value + "\t");
            }
            valuesWriter.flush();
        } finally {
            if(valuesReader!=null){
                valuesReader.close();
            }
            if(valuesWriter!=null){
                valuesWriter.close();
            }
        }

    }


}
