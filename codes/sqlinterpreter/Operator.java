package sqlinterpreter;
/**
 * @author xinhaojie
 * @create 2021-03-21-15:24
 */
public enum Operator {
    //operators in BNF are: == != > >= < <= like
    EQ("=="),
    NE("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    LIKE("like");
    private String operator;
    Operator(String s) {
        this.operator = s;
    }

    public String getOperator() {
        return operator;
    }

}
