package edu.uob.commands;

import edu.uob.utilities.Data;
import edu.uob.utilities.FileEditor;
import edu.uob.utilities.SqlExceptions;

import java.io.File;
import java.util.ArrayList;

public class SqlCommand {
    protected ArrayList<String> tokens;
    protected int currentWord;
    protected static String databaseName;
    protected ArrayList<String> values;

    public  SqlCommand(ArrayList<String> tokens) {
        this.tokens = tokens;
        currentWord = 0;
        values = new ArrayList<>();
    }

    //Default implement for avoiding casting the sqlCommand reference
    public void parser () throws SqlExceptions.ParsingException {}
    public String interpreter() throws SqlExceptions.InterpretingException {return "";}


    public static SqlCommand parserCommandType (ArrayList<String> tokens) {
        return switch (tokens.get(0).toUpperCase()) {
            case "SELECT" -> new CommandSelect(tokens);
            case "CREATE" -> new CommandCreate(tokens);
            case "USE" -> new CommandUse(tokens);
            case "INSERT" -> new CommandInsert(tokens);
            case "DROP" -> new CommandDrop(tokens);
            case "ALTER" -> new CommandAlter(tokens);
            case "JOIN" -> new CommandJoin(tokens);
            case "UPDATE" -> new CommandUpdate(tokens);
            case "DELETE" -> new CommandDelete(tokens);
            default -> null;
        };
    }

    protected void checkTableNameExistence(String tableName) throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        if(!fileEditor.isPathExisting(databaseName + File.separator + tableName.toLowerCase() + ".tab"))
            throw new SqlExceptions.InterpretingException("This table doesn't exist!");
    }

    protected boolean isPlainText() throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        String token = tokens.get(currentWord);
        //Check the validation of the word
        for(int i = 0; i < token.length(); i++) {
            char character = token.charAt(i);
            if (!Character.isDigit(character) && !Character.isLetter(character)) return false;
        }
        return true;
    }

    protected void parsingList(String ending, boolean isAttributeList) throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("list is needed");
        //The first token must be an attribute or value
        if(!(isAttributeList ? isPlainText() : isValidValue()))
            throw new SqlExceptions.ParsingException("This is not a valid list");
        //Return condition
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        if(tokens.get(currentWord).equalsIgnoreCase(ending))  return;
        //Recursively parsing the command
        while (!tokens.get(currentWord).equalsIgnoreCase(ending)) {
            //This word must be a comma
            if (!tokens.get(currentWord).equals(",")) 
                throw new SqlExceptions.ParsingException("Expected a comma between words");
            //This word must be an attribute
            currentWord++;
            if (currentWord >= tokens.size() || tokens.get(currentWord).equalsIgnoreCase(ending))
                throw new SqlExceptions.ParsingException("Invalid command");
            parsingList(ending, isAttributeList);
        }
    }

    protected void setDatabaseName (String databaseName) {
        SqlCommand.databaseName = databaseName;
    }

    protected boolean isValidValue() throws SqlExceptions.ParsingException {
        return isStringLiteral(tokens.get(currentWord)) || isBooleanLiteral() || isNumber() || tokens.get(currentWord).equalsIgnoreCase("NULL");
    }

    protected boolean isStringLiteral(String token) {
        return token.charAt(0) == '\'' && token.charAt(token.length() - 1) == '\'';
    }

    private boolean isBooleanLiteral() {
        return tokens.get(currentWord).equalsIgnoreCase("TRUE") || tokens.get(currentWord).equalsIgnoreCase("FALSE");
    }

    private boolean isNumber() {
        String token = tokens.get(currentWord);

        if (token.startsWith("+") || token.startsWith("-")) token = token.substring(1);
        if (token.isEmpty() || token.startsWith(".") || token.endsWith(".")) return false;

        // Check each character to ensure it's either a digit or at most one dot
        boolean isDot = false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '.') {
                if (isDot) return false;
                isDot = true;
            } else if (!Character.isDigit(c)) return false;
        }
        
        return true;
    }

    protected void parserCondition() throws SqlExceptions.ParsingException {
        //Look the first word, it is neither a ( or a expression
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid Condition");
        if(tokens.get(currentWord).equals("(")) {
            parseBrackets();
        }
        else {
            parseExpression();
        }
        //look at the next word, if it's a ),return
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid Condition");
        if (tokens.get(currentWord).equals(";") || tokens.get(currentWord).equals(")")) return;
        if (isBoolOperator(tokens.get(currentWord))) {
            currentWord++;
            parserCondition();
        }
    }

    protected void parseBrackets() throws SqlExceptions.ParsingException {
        //Check the first world, if it's a (, keep parsing (
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid Condition");
        if(tokens.get(currentWord).equals("(")) {
            parseBrackets();
        }
        else {
            parseExpression();
        }
        //look at the next word, if it's a )return
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid Condition");
        if (tokens.get(currentWord).equals(";") || tokens.get(currentWord).equals(")")) return;
        if (isBoolOperator(tokens.get(currentWord))) {
            currentWord++;
            parserCondition();
        }
    }

    protected void parseExpression() throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid expression");
        currentWord++;
        if (currentWord >= tokens.size() || !isComparator(tokens.get(currentWord)))
            throw new SqlExceptions.ParsingException("Invalid expression");
        currentWord++;
        if (currentWord >= tokens.size() || !isValidValue())
            throw new SqlExceptions.ParsingException("value is need for expression");
    }
    protected boolean isBoolOperator(String token) {
        return token.equalsIgnoreCase("and") || token.equalsIgnoreCase("or");
    }

    protected boolean isComparator(String token) {
        return token.equals("<") ||
                token.equals(">") ||
                token.equals("<=") ||
                token.equals(">=") ||
                token.equals("==") ||
                token.equals("!=") ||
                token.equalsIgnoreCase("LIKE");
    }

    protected ArrayList<Integer> selectResult(Data data) throws SqlExceptions.InterpretingException {
        ArrayList<Integer> currentResult = new ArrayList<>();
        //Look the first word, it is neither a ( or a expression
        if(tokens.get(currentWord).equals("(")) {
            currentResult = selectBrackets(data);
        }else {
            currentResult = selectExpression(data);
        }

        //look at the next word, if it's a ),return
        currentWord++;
        if (tokens.get(currentWord).equals(";") || tokens.get(currentWord).equals(")")) return currentResult;
        if (isBoolOperator(tokens.get(currentWord))) {
            if(tokens.get(currentWord).equalsIgnoreCase("and")){
                currentWord++;
                ArrayList<Integer> tempResult = selectResult(data);
                currentResult = combineResult(currentResult, tempResult);
            }else {
                currentWord++;
                ArrayList<Integer> tempResult = selectResult(data);
                currentResult = includeResult(currentResult, tempResult);
            }
        }
        return currentResult;
    }

    protected ArrayList<Integer> selectBrackets(Data data)  throws SqlExceptions.InterpretingException {
        ArrayList<Integer> currentResult = new ArrayList<>();
        currentWord++;
        if(tokens.get(currentWord).equals("(")) {
            currentResult = selectBrackets (data);
        } else {
            currentResult = selectExpression(data);
        }

        currentWord++;
        if (tokens.get(currentWord).equals(";") || tokens.get(currentWord).equals(")")) return currentResult;
        if (isBoolOperator(tokens.get(currentWord))) {
            if(tokens.get(currentWord).equalsIgnoreCase("and")){
                currentWord++;
                ArrayList<Integer> tempResult = selectResult(data);
                currentResult = combineResult(currentResult, tempResult);
            }else {
                currentWord++;
                ArrayList<Integer> tempResult = selectResult(data);
                currentResult = includeResult(currentResult, tempResult);
            }
        }
        return currentResult;
    }

    protected ArrayList<Integer> selectExpression(Data data) throws SqlExceptions.InterpretingException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tokens.get(currentWord).toLowerCase());
        stringBuilder.append(" ");
        currentWord++;
        stringBuilder.append(tokens.get(currentWord));
        stringBuilder.append(" ");
        currentWord++;
        stringBuilder.append(tokens.get(currentWord));
        System.out.println(stringBuilder.toString());
        return data.selectDataOnExpression(data, stringBuilder.toString());
    }

    protected ArrayList<Integer> combineResult( ArrayList<Integer> one, ArrayList<Integer> two) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Integer integer : one) {
            for (Integer value : two) {
                if (integer == value) {
                    result.add(integer);
                    break;
                }
            }
        }
        return result;
    }
    protected ArrayList<Integer> includeResult(ArrayList<Integer> one, ArrayList<Integer> two) {
        ArrayList<Integer> result = new ArrayList<>(one);
        for (int i = 0; i < two.size(); i++) {
            boolean found = false;
            for (Integer integer : one) {
                if (integer == two.get(i)) found = true;
            }
            if(found == false) result.add(two.get(i));
        }
        return result;
    }

    protected int findIndex() {
        for (int i = 0; i < tokens.size(); i++) {
            if(tokens.get(i).equalsIgnoreCase("WHERE")){
                return i + 1;
            }
        }
        return 0;
    }
}
