package edu.uob.commands;

import edu.uob.utilities.Data;
import edu.uob.utilities.SqlExceptions;

import java.util.ArrayList;

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
        //Check whether the command is terminated correctly
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        //you always need to use a database before joining;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
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

}
