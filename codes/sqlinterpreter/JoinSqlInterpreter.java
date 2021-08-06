package sqlinterpreter;

import exception.AttributeNotFoundException;
import exception.DatabaseNotChosenException;
import exception.InvalidJoinSqlException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-17-21:22
 */
public class JoinSqlInterpreter extends BaseSqlInterpreter {
    private final String joinRegex = "join\\s(.+)\\sand\\s(.+)\\son\\s(.+)\\sand\\s(.+)";
    private Pattern joinTablePattern = Pattern.compile(joinRegex, Pattern.CASE_INSENSITIVE);

    public JoinSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {
        if (curDatabase == null){
            throw new DatabaseNotChosenException("No database has been chosen");
        }
        Matcher joinTableMatcher = joinTablePattern.matcher(sql);
        if (joinTableMatcher.find()) {
            joinTable(joinTableMatcher);
            return;
        }
        //exception
        throw new InvalidJoinSqlException("Invalid JOIN sql");
    }

    private void joinTable(Matcher joinTableMatcher) throws Exception {
        queryResult = new ArrayList<>();
        //get the names of two tables and get the key columns
        String tableName1 = joinTableMatcher.group(1).trim();
        checkName(tableName1);
        String columnName1 = joinTableMatcher.group(3).trim();
        checkName(columnName1);
        List<List<String>> table1 = getTable(tableName1);

        String tableName2 = joinTableMatcher.group(2).trim();
        checkName(tableName2);
        String columnName2 = joinTableMatcher.group(4).trim();
        checkName(columnName2);
        List<List<String>> table2 = getTable(tableName2);

        //build the new joined table with new columns
        ArrayList<String> newColumns = new ArrayList<>();
        newColumns.add("id\t");
        joinTableColumns(newColumns, table1, tableName1);
        joinTableColumns(newColumns, table2, tableName2);
        queryResult.add(newColumns);

        //add the values into outcomes according to the join condition
        int targetIndex1 = getIndexOfColumn(table1.get(0), columnName1);
        if (targetIndex1 == -1) {
            throw new AttributeNotFoundException("Attribute " + columnName1 + " can not be found");
        }
        int targetIndex2 = getIndexOfColumn(table2.get(0), columnName2);
        if (targetIndex2 == -1) {
            throw new AttributeNotFoundException("Attribute " + columnName2 + " can not be found");
        }
        int nextLineNumber = 1;
        for (List<String> valuesOfTable1 : table1) {
            for (List<String> valuesOfTable2 : table2) {
                //once match the join key, add the values from table1 and table2
                if (valuesOfTable1.get(targetIndex1).equals(valuesOfTable2.get(targetIndex2))) {
                    ArrayList<String> newValues = new ArrayList<>();
                    newValues.add(nextLineNumber + "\t");
                    joinTableValues(newValues, valuesOfTable1);
                    joinTableValues(newValues, valuesOfTable2);
                    queryResult.add(newValues);
                    nextLineNumber++;
                }
            }
        }

        //remove the single quotes of Literal values and decorate it with \t
        formatQueryResult();
    }

    private void joinTableValues(List<String> newValues, List<String> valuesOfTable) {
        for (int i = 1; i < valuesOfTable.size(); i++) {
            newValues.add(valuesOfTable.get(i) + "\t");
        }
    }

    private void joinTableColumns(List<String> newColumns, List<List<String>> table, String tableName) {
        List<String> columnsOfTable = table.get(0);
        for (int i = 1; i < columnsOfTable.size(); i++) {
            newColumns.add(tableName + "." + columnsOfTable.get(i) + "\t");
        }
    }

    private List<List<String>> getTable(String tableName) throws Exception {
        List<List<String>> table;
        BufferedReader valuesReader = null;
        try {
            table = new ArrayList<>();
            File tableFile = getTableFile(curDatabase + "\\" + tableName + ".tab");
            valuesReader = new BufferedReader(new FileReader(tableFile));
            //add all attributes and values into outcomes in order to do some filter in memory
            String tableRow = valuesReader.readLine();
            while (tableRow != null) {
                if (!"".equals(tableRow)) {
                    table.add(new ArrayList<>(Arrays.asList(tableRow.split("\t"))));
                }
                tableRow = valuesReader.readLine();
            }
        } finally {
            if(valuesReader!=null){
                valuesReader.close();
            }
        }
        return table;
    }

}
