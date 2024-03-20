package edu.uob.commands;

import edu.uob.utilities.Data;
import edu.uob.utilities.SqlExceptions;

import java.util.ArrayList;
import java.util.PrimitiveIterator;

public class CommandSelect extends SqlCommand implements DatabaseOperations {
    
    public CommandSelect(ArrayList<String> tokens) { super(tokens); }
    public CommandSelect(ArrayList<String> tokens, String command) { super(tokens, command); }

    public void parser() throws SqlExceptions.ParsingException {
        //The first word should be wild attribute list.
        currentWord++;
        parsingWildAttributeList();
        //The next word should be a table name;
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("TABLE name is expected");
        //If the next word is a ; it must ends
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        if (currentWord == tokens.size() - 1 && tokens.get(currentWord).equals(";")) return;
        //else it must be a where
        if (!tokens.get(currentWord).equalsIgnoreCase("WHERE"))
            throw new SqlExceptions.ParsingException("You need to terminate the command use where");
        currentWord++;
        parserCondition();
        //Check whether the command is terminated
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        //you always need to use a database before joining;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        //Check whether condition is needed
//        for(String token: tokens) {
//            if (token.equalsIgnoreCase("where")) return selectCondition();
//        }
        //check table name
        checkTableNameExistence(tokens.get(tokens.size() - 2));
        Data data = new Data(databaseName,tokens.get(tokens.size() - 2));
        //* will print all the results
        if(tokens.get(1).equals("*")) return "[OK]\n" + data.constructOutput();
        //Else, construct the result based on the attribute list
        ArrayList<String> attributeList = constructAttributeList();
        if (attributeList.isEmpty()) throw new SqlExceptions.InterpretingException("Something is wrong for select command");
        return "[OK]\n" + data.constructOutput(attributeList);
    }

    private String selectCondition() throws SqlExceptions.InterpretingException {
        //Find table name
        String tableName = findTableName();
        //check table name
        checkTableNameExistence(tableName);
        Data data = new Data(databaseName,tableName);
        //find the start of the condition
        int index = findIndex();
        //First step: select data based on wild attribute list
        if(tokens.get(1).equals("*")) data.selectData();
        return selectResult(data, index);
    }

    private String selectResult(Data data, int index) throws SqlExceptions.InterpretingException {
        return "[OK]";
    }

    private int findIndex() {
        for (int i = 0; i < tokens.size(); i++) {
            if(tokens.get(i).equalsIgnoreCase("WHERE")){
                return i + 1;
            }
        }
        return 0;
    }

    private String findTableName () {
        for(int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equalsIgnoreCase("from")) return tokens.get(i + 1);
        }
        return "";
    }

    private void parsingWildAttributeList() throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size()) throw new SqlExceptions.ParsingException("Invalid command");
        if(tokens.get(currentWord).equals("*")) {
            currentWord++;
            if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("FROM"))
                throw new SqlExceptions.ParsingException("FROM is expected");
            return;
        }
        parsingList("FROM",true);
    }

    private ArrayList<String> constructAttributeList () {
        ArrayList<String> attributeList = new ArrayList<>();
        for (int i = 1; !tokens.get(i).equalsIgnoreCase("FROM"); i++) {
            if (!tokens.get(i).equals(",")) {
                attributeList.add(tokens.get(i));
            }
        }
        return attributeList;
    }

    private void parserCondition() throws SqlExceptions.ParsingException {
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

    void parseBrackets() throws SqlExceptions.ParsingException {
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

    private void parseExpression() throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid expression");
        currentWord++;
        if (currentWord >= tokens.size() || !isComparator(tokens.get(currentWord)))
            throw new SqlExceptions.ParsingException("Invalid expression");
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("value is need for expression");
    }
    private boolean isBoolOperator(String token) {
        return token.equalsIgnoreCase("and") || token.equalsIgnoreCase("or");
    }

    private boolean isComparator(String token) {
        return token.equals("<") ||
                token.equals(">") ||
                token.equals("<=") ||
                token.equals(">=") ||
                token.equals("=") ||
                token.equals("==") ||
                token.equals("!=") ||
                token.equalsIgnoreCase("LIKE");
    }
}
