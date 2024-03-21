package edu.uob.commands;

import edu.uob.utilities.Data;
import edu.uob.utilities.SqlExceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommandUpdate extends SqlCommand implements DatabaseOperations {
    public CommandUpdate(ArrayList<String> tokens) { super(tokens); }
    public void parser() throws SqlExceptions.ParsingException {
        //The first word should be a TABLE name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid table name");
        //Next word is SET
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("SET"))
            throw new SqlExceptions.ParsingException("set");
        currentWord++;
        parsingNameList();
        currentWord++;
        parserCondition();
        //Check whether the command is terminated
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        //you always need to use a database before joining;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        String tableName = tokens.get(1);
        checkTableNameExistence(tableName);
        int index =  findIndex();
        Data data = new Data(databaseName,tableName);
        //find the start of the condition
        currentWord = findIndex();
        data.selectData();
        ArrayList<Integer> finalResult = selectResult(data);
        Map<String, String> valuePair = constructValuePair();
        data.updateValue(valuePair, finalResult);
        return "[OK]";
    }

    private Map<String, String> constructValuePair() {
        Map<String, String> valuePair = new HashMap<>();
        int ending = findIndex() - 1;
        int index = 3;
        while(index < ending) {
            String value = processString(tokens.get(index + 2));
            valuePair.put(tokens.get(index).toLowerCase(),value);
            index += 4;
        }
        return valuePair;
    }

    private String processString(String token) {
        if(token.equalsIgnoreCase("NULL")) return " ";
        if(isStringLiteral(token)) return token.substring(1, token.length() - 1);
        return token;
    }


    private void parsingNameList() throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("list is needed");
        //The first token must be an attribute or value
        if (!isPlainText())
            throw new SqlExceptions.ParsingException("attribute name");
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equals("="))
            throw new SqlExceptions.ParsingException("=");
        currentWord++;
        if (currentWord >= tokens.size() || !isValidValue())
            throw new SqlExceptions.ParsingException("set");
        //Return condition
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        if(tokens.get(currentWord).equalsIgnoreCase("WHERE"))  return;
        //Recursively parsing the command
        while (!tokens.get(currentWord).equalsIgnoreCase("WHERE")) {
            //This word must be a comma
            if (!tokens.get(currentWord).equals(","))
                throw new SqlExceptions.ParsingException("Expected a comma between words");
            //This word must be an attribute
            currentWord++;
            if (currentWord >= tokens.size() || tokens.get(currentWord).equalsIgnoreCase("WHERE"))
                throw new SqlExceptions.ParsingException("Invalid command");
            parsingNameList();
        }
    }
}
