package sqlinterpreter;

import exception.AttributeNotFoundException;
import exception.DatabaseNotChosenException;
import exception.InvalidAlterSqlException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-17-18:17
 */
public class AlterSqlInterpreter extends BaseSqlInterpreter {

    private Pattern addPattern = Pattern.compile("alter\\s+table\\s(.+)\\sadd\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern dropPattern = Pattern.compile("alter\\s+table\\s(.+)\\sdrop\\s(.+)", Pattern.CASE_INSENSITIVE);

    public AlterSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {
        //check if the sql is to add columns
        if (curDatabase == null) {
            throw new DatabaseNotChosenException("No database has been chosen");
        }
        Matcher addMatcher = addPattern.matcher(sql);
        if (addMatcher.find()) {
            addColumn(addMatcher);
            return;
        }

        //check if the sql is to drop columns
        Matcher dropMatcher = dropPattern.matcher(sql);
        if (dropMatcher.find()) {
            dropColumn(dropMatcher);
            return;
        }
        //exception
        throw new InvalidAlterSqlException("Invalid ALTER sql");
    }

    private void addColumn(Matcher addMatcher) throws Exception {
        queryResult = new ArrayList<>();
        BufferedReader valuesReader = null;
        try {
            final String defaultValue = "N U L L";
            //get the table name
            String tableName = addMatcher.group(1).trim();
            checkName(tableName);
            //check the validation of file
            File tableFile = getTableFile(curDatabase + "\\" + tableName.trim() + ".tab");
            valuesReader = new BufferedReader(new FileReader(tableFile));
            //add all attributes and values into outcomes in order to do some filter in memory
            tableColumnNames = new ArrayList<>(Arrays.asList(valuesReader.readLine().split("\t")));
            //add the target column into tableColumnNames
            String columnName = addMatcher.group(2).trim();
            checkName(columnName);
            tableColumnNames.add(columnName);
            queryResult.add(tableColumnNames);
            String tableRow = valuesReader.readLine();
            while (tableRow != null) {
                if (!"".equals(tableRow)) {
                    List<String> newValues = new ArrayList<>(Arrays.asList(tableRow.split("\t")));
                    //add a new attribute, assume that the initial value of this attribute is 'null'
                    newValues.add(defaultValue);
                    queryResult.add(newValues);
                }
                tableRow = valuesReader.readLine();
            }
            //update the table
            updateTable(tableFile);
            //after the write operation, make the queryResult to null,because update does not return back any data
            queryResult = null;
            tableColumnNames = null;
        } finally {
            if (valuesReader != null) {
                valuesReader.close();
            }
        }
    }

    private void dropColumn(Matcher dropMatcher) throws Exception {
        queryResult = new ArrayList<>();
        BufferedReader valuesReader = null;
        try {
            //get the table name
            String tableName = dropMatcher.group(1).trim();
            checkName(tableName);
            //check the validation of file
            File tableFile = getTableFile(curDatabase + "\\" + tableName + ".tab");
            valuesReader = new BufferedReader(new FileReader(tableFile));
            //add all attributes and values into outcomes in order to do some filter in memory
            tableColumnNames = new ArrayList<>(Arrays.asList(valuesReader.readLine().split("\t")));
            queryResult.add(tableColumnNames);
            String tableRow = valuesReader.readLine();
            while (tableRow != null) {
                if (!"".equals(tableRow)) {
                    queryResult.add(new ArrayList<>(Arrays.asList(tableRow.split("\t"))));
                }
                tableRow = valuesReader.readLine();
            }
            //delete the whole column
            String columnName = dropMatcher.group(2).trim();
            checkName(columnName);
            deleteValues(columnName);
            //update the table
            updateTable(tableFile);
            //after the write operation, make the queryResult to null,because update does not return back any data
            queryResult = null;
            tableColumnNames = null;
        } finally {
            if (valuesReader != null) {
                valuesReader.close();
            }
        }
    }

    private void deleteValues(String columnName) throws Exception {
        int targetColumn = getIndexOfColumn(tableColumnNames, columnName);
        //exception
        if (targetColumn == -1) {
            throw new AttributeNotFoundException("Attribute " + columnName + " can not be found");
        }
        for (List<String> row : queryResult) {
            //remove the column value in each row
            if (targetColumn < row.size()) {
                row.remove(targetColumn);
            }
        }
    }

}
