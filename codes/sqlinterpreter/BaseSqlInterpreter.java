package sqlinterpreter;

import exception.DataBaseFileNotFoundException;
import exception.InvalidNameException;
import exception.InvalidStringLiteralException;
import exception.TableFileNotFoundException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-13-19:29
 */
public class BaseSqlInterpreter {
    /**the curDatabase field should be a class variable, which can be shared by each child class*/
    public static String curDatabase;
    public String sql;
    public List<String> tableColumnNames;
    public List<List<String>> queryResult;

    private Pattern namePattern = Pattern.compile("[^a-zA-Z0-9]+");
    private Pattern literalPattern = Pattern.compile("^\\'[^\\'\\t]+\\'$");
    public BaseSqlInterpreter() {
    }

    public BaseSqlInterpreter(String sql) {
        this.sql = sql;
    }

    public void sqlExecution() throws Exception {
    }

    public void updateTable(File tableFile) throws Exception {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(tableFile));
            for (int i = 0; i < queryResult.size(); i++) {
                for (String value : queryResult.get(i)) {
                    bufferedWriter.write(value + "\t");
                }
                if (i < queryResult.size() - 1) {bufferedWriter.newLine();}
            }
            bufferedWriter.flush();
        } finally {
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
        }

    }

    public int getIndexOfColumn(List<String> values, String columnName) {
        for (int i = 0; i < values.size(); i++) {
            if (columnName.equals(values.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**check the if one table is existed,then return an instance of table File if it is existed*/
    public File getTableFile(String tableName) throws Exception {
        File tableFile = new File(tableName);
        if (!tableFile.exists()) {
            //no such table existed exception
            throw new TableFileNotFoundException("Table " + tableName + " can not be found");
        }
        return tableFile;
    }

    /**check the if one database is existed,then return an instance of database File if it is existed*/
    public File getDatabaseFile(String databaseName) throws Exception {
        File databaseFile = new File(databaseName);
        if (!databaseFile.exists()) {
            //no such database existed exception
            throw new DataBaseFileNotFoundException("Database " + databaseName + " can not be found");
        }
        return databaseFile;
    }

    public void formatQueryResult() {
        //remove the single quotes of literal strings
        //format with fixed length, if it is not enough , one \t would help to split different column
        for (List<String> row : queryResult) {
            for (int i = 0; i < row.size(); ++i) {
                String value = row.get(i);
                //if it equals the default value 'null',then set it as empty string
                value = ("N U L L".equals(value)) ? "" : value;
                //the query result of SELECT or JOIN should be strings without single quote
                value = value.replaceAll("\\'", "");
                //format the length of id column to 5
                //format the length of other columns to 15
                value = (i == 0) ? String.format("%-5s", value) : String.format("%-20s", value);
                row.set(i, value + "\t");
            }

        }
    }

    /**check if the TableName ColumnName DatabaseName and StringLiterals are valid*/
    public void checkName(String name) throws Exception {
        Matcher nameMatcher = namePattern.matcher(name);
        //if there are sequences that are not alphanumeric, throw exception
        if (nameMatcher.find()) {
            throw new InvalidNameException("Invalid name here, it should be purely alphanumeric");
        }
    }

    public void checkStringLiteral(String literal) throws Exception {
        final String singleQuote = "'";
        if (literal.contains(singleQuote)) {
            //match the StringLiteral strictly
            Matcher literalMatcher = literalPattern.matcher(literal);
            //not match, there are single quote or tabs,throw exception
            if (!literalMatcher.find()) {
                throw new InvalidStringLiteralException("Invalid StringLiteral");
            }
        }
    }
}
