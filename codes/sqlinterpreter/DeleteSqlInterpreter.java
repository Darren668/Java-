package sqlinterpreter;

import exception.DatabaseNotChosenException;
import exception.InvalidDeleteSqlException;
import exception.InvalidWhereConditionException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-17-15:29
 */
public class DeleteSqlInterpreter extends BaseSqlInterpreter {

    private Pattern deleteSqlPattern = Pattern.compile("delete\\s+from\\s(.+)where(.+)", Pattern.CASE_INSENSITIVE);

    public DeleteSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {
        if (curDatabase == null) {
            throw new DatabaseNotChosenException("No database has been chosen");
        }
        Matcher deleteSqlMatcher = deleteSqlPattern.matcher(sql);
        if (deleteSqlMatcher.find()) {
            deleteValues(deleteSqlMatcher);
            return;
        }
        //exception
        throw new InvalidDeleteSqlException("Invalid DELETE sql");
    }

    private void deleteValues(Matcher deleteSqlMatcher) throws Exception {
        queryResult = new ArrayList<>();
        BufferedReader valuesReader = null;
        try {
            //get the table name
            String tableName = deleteSqlMatcher.group(1).trim();
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
            //filter outcomes that are not qualified to the where condition
            String whereConditions = deleteSqlMatcher.group(2).trim();
            if ("".equals(whereConditions)) {
                throw new InvalidWhereConditionException("The condition after where can not be empty");
            }
            filterByWhere(whereConditions);
            //finish the delete in memory, then write the data into local database
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

    private void filterByWhere(String whereConditions) throws Exception {
        //traverse the outcomes from the back and filter values by where condition
        for (int i = queryResult.size() - 1; i > 0; --i) {
            SqlWhereConditionParser sqlCondition = new SqlWhereConditionParser(whereConditions, queryResult.get(i), tableColumnNames);
            if (sqlCondition.parseConditions()) {
                queryResult.remove(i);
            }
        }
    }

}
