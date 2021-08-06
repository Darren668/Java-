package sqlinterpreter;

import exception.AttributeNotFoundException;
import exception.InvalidWhereConditionException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xinhaojie
 * @create 2021-03-15-15:49
 */
public class SqlWhereConditionParser extends BaseSqlInterpreter {
    /**define some constants*/
    private final String andLiteral = "and";
    private final String orLiteral  = "or";

    /**the original conditions after WHERE in sql*/
    private String conditions;
    /**check every line read from the local*/
    private List<String> values;
    private List<String> attributes;

    private Pattern pureNumberPattern = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+$");
    private Pattern likePattern = Pattern.compile("\\s+like\\s+", Pattern.CASE_INSENSITIVE);
    public SqlWhereConditionParser(String condition, List<String> values, List<String> attributes) {
        this.conditions = condition.trim();
        this.values = values;
        this.attributes = attributes;
    }

    /**
     * The conditions parsing work is like the work of turning an infix expression to an suffix expression
     *  stack1: to store the ( ) and and/or  (like the ( ) and operator in calculation)
     *          the priority of and is higher than or
     *  stack2: to store the useful single condition including <AttributeName> <Operator> <Value>   (like the numbers)
     *      after the work, the suffix expression could follow the original judge priority
     * */
    public boolean parseConditions() throws Exception {
        //define the ( )
        final char leftBracket = '(';
        final char rightBracket = ')';
        //recursion way to check whole condition
        Deque<String> stack1 = new ArrayDeque<>();
        Deque<String> stack2 = new ArrayDeque<>();
        char[] conditionChars = conditions.toCharArray();
        //locate the OR and AND keywords and replace them into lower cases
        //Modify the or/and into " or " in order to split
        //and avoid the "or"/""and" subString is somewhere in attribute or value
        conditions = conditions.replaceAll("(?i)\\)\\s*or\\s*\\(", ") or (");
        conditions = conditions.replaceAll("(?i)\\)\\s*and\\s*\\(", ") and (");
        int indexOfOr = conditions.indexOf(" or ");
        int indexOfAnd = conditions.indexOf(" and ");
        StringBuilder subCondition = new StringBuilder();
        if (indexOfAnd == -1 && indexOfOr == -1) {
            return parseSingleCondition(conditions);
        }
        for (int i = 0; i < conditionChars.length; ++i) {
            char curChar = conditionChars[i];
            //push the ( into stack1 directly
            if (curChar == leftBracket) {
                stack1.push(String.valueOf(curChar));
            } else if (i == indexOfOr) {
                //pop stack1 until the top one is ( or it is empty
                while (!stack1.isEmpty() && !String.valueOf(leftBracket).equals(stack1.peek())) {
                    stack2.push(stack1.pop());
                }
                //OR priority is lower,push it when the stack1 is empty / top one is ( / top one is AND
                //or pop out the top of stack1 to stack2
                stack1.push(orLiteral);
                i += 2;
                //update the index of or from the remaining substring
                indexOfOr = conditions.substring(i + 1).indexOf(" or ") + i + 1;
            } else if (i == indexOfAnd) {
                //the top of stack1 is AND, pop it and  push  into stack2
                while (!stack1.isEmpty() && andLiteral.equals(stack1.peek())) {
                    stack2.push(stack1.pop());
                }
                //AND priority is higher,push it into stack1 when it is empty, the top one is OR or (
                stack1.push(andLiteral);
                i += 3;
                //update the index of or from the remaining substring
                indexOfAnd = conditions.substring(i + 1).indexOf(" and ") + i + 1;
            } else if (curChar == rightBracket) {
                //if it is ),push the subCondition into stack2
                while (!stack1.isEmpty() && !String.valueOf(leftBracket).equals(stack1.peek())) {
                    stack2.push(stack1.pop());
                }
                if (!stack1.isEmpty()) {
                    stack1.pop();
                }
                //flush the subCondition into stack2
                if (subCondition.length() > 0) {
                    stack2.push(subCondition.toString().trim());
                    subCondition.delete(0, subCondition.length());
                }
            } else {
                //the rest chars are the useful condition we need
                subCondition.append(curChar);
            }
        }
        while (!stack1.isEmpty()) {
            stack2.push(stack1.pop());
        }
        return analyzeConditions(stack2);
    }

    /**
     * Once we get the suffix expression of conditions ,we could traverse the stack2 in reverse order
     * Once current string is and /or, we pop two single condition strings to do the and / or judge work
     *      and push true/false string into stack result
     * Once current string is single condition, we push it into  stack result
     * The final true/false left in the stack is our final judge result
     * However, if there is more than one string left, it means the conditions string are invalid
     * */
    public boolean analyzeConditions(Deque<String> suffixCondition) throws Exception {
        Deque<String> result = new ArrayDeque<>();
        while (!suffixCondition.isEmpty()) {
            String subCondition = suffixCondition.getLast();
            if (isAndOr(subCondition) && result.size() > 1) {
                String condition1 = result.pop();
                String condition2 = result.pop();
                Boolean compareResult;
                compareResult = andLiteral.equals(subCondition) ?
                        parseSingleCondition(condition1) && parseSingleCondition(condition2) :
                        parseSingleCondition(condition1) || parseSingleCondition(condition2);
                result.push(String.valueOf(compareResult));
            } else {
                result.push(subCondition);
            }
            suffixCondition.removeLast();
        }
        if (result.size() > 1 || result.isEmpty()) {
            throw new InvalidWhereConditionException("Invalid conditions after where");
        }
        return Boolean.valueOf(result.pop());
    }

    /**make use of operator to split single condition to get the attributeName and value*/
    public boolean parseSingleCondition(String condition) throws Exception {
        final String trueLiteral = "true";
        final String falseLiteral  = "false";
        final String defaultValue = "N U L L";
        if (trueLiteral.equals(condition)) {
            return true;
        }
        if (falseLiteral.equals(condition)){
            return false;
        }
        String[] attributeValuePair = null;
        String operator = null;
        //find the like keyword
        Matcher likeMatcher = likePattern.matcher(condition);
        if (condition.contains(Operator.EQ.getOperator())) {
            attributeValuePair = condition.split("==");
            operator = Operator.EQ.getOperator();
        } else if (condition.contains(Operator.NE.getOperator())) {
            attributeValuePair = condition.split("!=");
            operator = Operator.NE.getOperator();
        } else if (condition.contains(Operator.GE.getOperator())) {
            attributeValuePair = condition.split(">=");
            operator = Operator.GE.getOperator();
        } else if (condition.contains(Operator.LE.getOperator())) {
            attributeValuePair = condition.split("<=");
            operator = Operator.LE.getOperator();
        } else if (condition.contains(Operator.GT.getOperator())) {
            attributeValuePair = condition.split(">");
            operator = Operator.GT.getOperator();
        } else if (condition.contains(Operator.LT.getOperator())) {
            attributeValuePair = condition.split("<");
            operator = Operator.LT.getOperator();
        } else if (likeMatcher.find()) {
            //Modify the like into " like " in order to split
            //and avoid the "like" subString is somewhere in attribute or value
            condition = condition.replaceAll("(?i)\\s+like\\s+", " like ");
            attributeValuePair = condition.split(" like ");
            operator = Operator.LIKE.getOperator();
        } else {
            throw new InvalidWhereConditionException("Can not find the operator in where condition");
        }
        //even though there is correct operator, the attribute and value on each side should be tested
        //there are single one attribute and single one value on each side of operator
        //so the number of 'attribute' and 'value' is 2
        final int numberOfColumnValue = 2;
        if (attributeValuePair.length != numberOfColumnValue) {
            throw new InvalidWhereConditionException("One attribute and one value are needed in each where condition");
        }
        String columnName = attributeValuePair[0].trim();
        checkName(columnName);
        String conditionValue = attributeValuePair[1].trim();
        checkStringLiteral(conditionValue);
        int targetIndex = getIndexOfColumn(attributes, columnName);
        if (targetIndex == -1) {
            throw new AttributeNotFoundException("Attribute " + columnName + " can not be found");
        }
        String originalValue = values.get(targetIndex);
        //if the original value is 'null',there is no need to compare, return false directly
        if(defaultValue.equals(originalValue)){
            return false;
        }
        return checkSingleCondition(operator, originalValue, conditionValue);

    }

    /**single condition, just match which operator it is and then return back the comparison result*/
    public boolean checkSingleCondition(String operator, String originalValue, String conditionValue) throws Exception {
        //when string(name or value) can not be parsed by Float, throw a exception
        if (Operator.GT.getOperator().equals(operator)) {
            try {
                return Float.parseFloat(originalValue) > Float.parseFloat(conditionValue);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Attribute/Value cannot be converted to number");
            }
        }

        if (Operator.GE.getOperator().equals(operator)) {
            try {
                return Float.parseFloat(originalValue) >= Float.parseFloat(conditionValue);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Attribute/Value cannot be converted to number");
            }
        }

        if (Operator.LT.getOperator().equals(operator)) {
            try {
                return Float.parseFloat(originalValue) < Float.parseFloat(conditionValue);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Attribute/Value cannot be converted to number");
            }
        }

        if (Operator.LE.getOperator().equals(operator)) {
            try {
                return Float.parseFloat(originalValue) <= Float.parseFloat(conditionValue);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Attribute/Value cannot be converted to number");
            }
        }

        if (Operator.EQ.getOperator().equals(operator)) {
            try {
                //if it can be parsed by Float, then compare the number
                float originalNumber = Float.parseFloat(originalValue);
                float conditionNumber = Float.parseFloat(conditionValue);
                return Float.compare(originalNumber, conditionNumber) == 0;
            } catch (Exception e) {
                //if it can not be parsed, then compare the String itself
                return originalValue.equals(conditionValue);
            }
        }

        if (Operator.NE.getOperator().equals(operator)) {
            try {
                float originalNumber = Float.parseFloat(originalValue);
                float conditionNumber = Float.parseFloat(conditionValue);
                return Float.compare(originalNumber, conditionNumber) != 0;
            } catch (Exception e) {
                return !originalValue.equals(conditionValue);
            }
        }

        if (Operator.LIKE.getOperator().equals(operator)) {
            //check if the conditionValue is numerical data, Use of LIKE on numerical data return exception
            if (pureNumberPattern.matcher(conditionValue).find()) {
                throw new InvalidWhereConditionException("Use of LIKE on numerical data is not allowed");
            }
            Pattern likePattern = Pattern.compile(conditionValue.replaceAll("\\'", ""));
            return likePattern.matcher(originalValue).find();
        }
        //no match. exception
        throw new InvalidWhereConditionException("Can not find the operator in where condition");
    }

    public boolean isAndOr(String alterationType) {
        return andLiteral.equals(alterationType) || orLiteral.equals(alterationType);
    }


}
