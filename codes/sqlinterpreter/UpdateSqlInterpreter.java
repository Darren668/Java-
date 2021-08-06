package sqlinterpreter;

import exception.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-16-19:24
 */
public class UpdateSqlInterpreter extends BaseSqlInterpreter {
    private String updateSqlRegex = "update\\s(.+)\\sset\\s(.+)\\swhere\\s(.+)";
    private Pattern updateSqlPattern = Pattern.compile(updateSqlRegex, Pattern.CASE_INSENSITIVE);
    public UpdateSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {
        if (curDatabase == null) {
            throw new DatabaseNotChosenException("No database has been chosen");
        }
        Matcher updateSqlMatcher = updateSqlPattern.matcher(sql);
        if (updateSqlMatcher.find()) {
            updateValues(updateSqlMatcher);
            return;
        }
        //exception
        throw new InvalidUpdateSqlException("Invalid UPDATE sql");
    }

    private void updateValues(Matcher updateSqlMatcher) throws Exception {
        queryResult = new ArrayList<>();
        BufferedReader valuesReader = null;
        try {
            //get the table name
            String tableName = updateSqlMatcher.group(1).trim();
            checkName(tableName);
            //check the validation of file
            File tableFile = getTableFile(curDatabase + "\\" + tableName + ".tab");
            valuesReader = new BufferedReader(new FileReader(tableFile));
            //add all attributes and values into outcomes in order to do some filter in memory
            tableColumnNames = Arrays.asList(valuesReader.readLine().split("\t"));
            queryResult.add(tableColumnNames);
            String tableRow = valuesReader.readLine();
            while (tableRow != null) {
                if (!"".equals(tableRow)) {
                    queryResult.add(new ArrayList<>(Arrays.asList(tableRow.split("\t"))));
                }
                tableRow = valuesReader.readLine();
            }
            //get where condition first, then update the corresponding value
            String whereConditions = updateSqlMatcher.group(3).trim();
            if ("".equals(whereConditions)) {
                throw new InvalidWhereConditionException("The condition after where can not be empty");
            }
            //get the set orders
            String setOrder = updateSqlMatcher.group(2).trim();
            for (int i = 1; i < queryResult.size(); ++i) {
                //check if the condition is qualified, then execute the set order
                SqlWhereConditionParser sqlCondition = new SqlWhereConditionParser(whereConditions, queryResult.get(i), tableColumnNames);
                if (sqlCondition.parseConditions()) {
                    parseSetOrders(setOrder, queryResult.get(i));
                }
            }
            //finish the update in memory, then write the data into local database
            updateTable(tableFile);
            //after the write operation, make the queryResult to null,because update does not return back any data
            queryResult = null;
            tableColumnNames = null;
        } finally {
            if(valuesReader!=null){
                valuesReader.close();
            }
        }

    }

    public void parseSetOrders(String setOrder, List<String> values) throws Exception {
        final String comma = ",";
        if (setOrder.contains(comma)) {
            String[] setPairs = setOrder.split(",");
            for (int i = 0; i < setPairs.length; i++) {
                executeSetOrder(setPairs[i], values);
            }
        } else {
            executeSetOrder(setOrder, values);
        }
    }

    public void executeSetOrder(String singleSetOrder, List<String> values) throws Exception {
        //check if there is only one "=" in set order
        final String equal = "=";
        if (!singleSetOrder.contains(equal) || singleSetOrder.indexOf(equal) != singleSetOrder.lastIndexOf(equal)) {
            throw new InvalidSetException("there must be only one \"=\" in set order");
        }
        String columnName = singleSetOrder.split("=")[0].trim();
        checkName(columnName);
        String targetValue = singleSetOrder.split("=")[1].trim();
        checkStringLiteral(targetValue);
        int columnIndex = getIndexOfColumn(tableColumnNames, columnName);
        //exception
        if (columnIndex == -1) {
            throw new AttributeNotFoundException("Attribute " + columnName + " can not be found");
        }
        if (columnIndex < values.size()) {
            values.set(columnIndex, targetValue);
        }

    }


}
