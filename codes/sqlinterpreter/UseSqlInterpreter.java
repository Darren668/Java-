package sqlinterpreter;
import exception.InvalidUseSqlException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-13-19:43
 */
public class UseSqlInterpreter extends BaseSqlInterpreter {
    private Pattern useSqlPattern = Pattern.compile("use\\s(.+)", Pattern.CASE_INSENSITIVE);
    public UseSqlInterpreter() {
    }

    public UseSqlInterpreter(String sql) {
        super(sql);
    }

    @Override
    public void sqlExecution() throws Exception {
        Matcher useSqlMatcher = useSqlPattern.matcher(sql);
        if (useSqlMatcher.find()) {
            useDatabase(useSqlMatcher);
            return;
        }
        //exception
        throw new InvalidUseSqlException("Invalid USE sql");
    }

    private void useDatabase(Matcher useSqlMatcher) throws Exception {
        String databaseName = useSqlMatcher.group(1).trim();
        checkName(databaseName);
        getDatabaseFile(databaseName);
        setCurDatabase(databaseName);
    }

    public void setCurDatabase(String curDatabase) {
        BaseSqlInterpreter.curDatabase = curDatabase;
    }
}
