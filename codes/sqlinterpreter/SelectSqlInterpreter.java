package sqlinterpreter;

import exception.AttributeNotFoundException;
import exception.DatabaseNotChosenException;
import exception.InvalidSelectSqlException;
import exception.InvalidWhereConditionException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-13-19:45
 */
public class SelectSqlInterpreter extends BaseSqlInterpreter {

    private Pattern selectSqlPattern = Pattern.compile("select\\s(.+)\\sfrom\\s(.+)", Pattern.CASE_INSENSITIVE);
    private Pattern hasWherePattern = Pattern.compile("\\swhere", Pattern.CASE_INSENSITIVE);
    private Pattern conditionPattern = Pattern.compile("\\s+where(.*)", Pattern.CASE_INSENSITIVE);
    public SelectSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {
        if (curDatabase == null){
            throw new DatabaseNotChosenException("No database has been chosen");
        }
        Matcher selectSqlMatcher = selectSqlPattern.matcher(sql);
        if (selectSqlMatcher.find()) {
            selectValues(selectSqlMatcher);
            return;
        }
        //exception
        throw new InvalidSelectSqlException("Invalid SELECT sql");
    }

    private void selectValues(Matcher selectSqlMatcher) throws Exception {
        queryResult = new ArrayList<>();
        BufferedReader valuesReader = null;
        try {
            //get the table name, no matter how many words after from, the table name is the first one
            String stringAfterFrom = selectSqlMatcher.group(2).trim();
            String[] strAfterFromArray = stringAfterFrom.split(" ");
            String tableName = strAfterFromArray[0];
            checkName(tableName);
            //If there are conditions but without where keyword,exception
            boolean hasWhere = hasWherePattern.matcher(stringAfterFrom).find();
            if (strAfterFromArray.length > 1 && !hasWhere) {
                throw new InvalidWhereConditionException("keyword WHERE can not be found in where condition");
            }
            //check the validation of file
            File tableFile = getTableFile(curDatabase + "\\" + tableName.trim() + ".tab");
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

            Matcher conditionMatcher = conditionPattern.matcher(sql);
            if (conditionMatcher.find()) {
                String whereConditions = conditionMatcher.group(1).trim();
                if ("".equals(whereConditions)) {
                    throw new InvalidWhereConditionException("The condition after where can not be empty");
                }
                filterByWhere(whereConditions);
            }
            //filter columns that are not needed
            //get wild attributes
            String columnString = selectSqlMatcher.group(1).trim();
            boolean selectAllColumns = (columnString.length() == 1 && "*".equals(columnString));
            if (!selectAllColumns) {
                String[] targetColumns = columnString.split(",");
                List<Integer> targetColIndexes = getTargetColIndexes(targetColumns);
                filterByColumn(targetColIndexes);
            }
            //remove the single quotes of Literal values and decorate it with \t
            formatQueryResult();
        } finally {
            if(valuesReader!=null){
                valuesReader.close();
            }
        }


    }

    private void filterByColumn(List<Integer> targetColIndexes) {
        List<List<String>> targetOutcome = new ArrayList<>();
        for (List<String> row : queryResult) {
            ArrayList<String> targetValue = new ArrayList<>();
            for (int targetColIndex : targetColIndexes) {
                if (targetColIndex < row.size()) {
                    targetValue.add(row.get(targetColIndex));
                }
            }
            targetOutcome.add(targetValue);
        }
        queryResult = targetOutcome;
    }


    private void filterByWhere(String whereConditions) throws Exception {
        //traverse the outcomes from the back and filter values by where condition
        for (int i = queryResult.size() - 1; i > 0; --i) {
            SqlWhereConditionParser sqlCondition = new SqlWhereConditionParser(whereConditions, queryResult.get(i), tableColumnNames);
            if (!sqlCondition.parseConditions()) {
                queryResult.remove(i);
            }
        }
    }


    private List<Integer> getTargetColIndexes(String[] targetColumns) throws Exception {
        List<Integer> targetColIndexes = new ArrayList<>();
        //find the index of every target columns according to the table columns
        //the order must follow the order of targetColumns in order that return back the same select order
        for (String targetColumn : targetColumns) {
            targetColumn = targetColumn.trim();
            checkName(targetColumn);
            int targetIndex = getIndexOfColumn(tableColumnNames, targetColumn);
            if (targetIndex == -1) {
                throw new AttributeNotFoundException("Attribute " + targetColumn + " can not be found");
            }
            targetColIndexes.add(targetIndex);
        }
        return targetColIndexes;
    }


}
