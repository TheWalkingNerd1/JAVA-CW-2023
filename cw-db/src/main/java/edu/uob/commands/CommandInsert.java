package edu.uob.commands;

import edu.uob.utilities.Data;
import edu.uob.utilities.FileEditor;
import edu.uob.utilities.SqlExceptions;

import java.io.File;
import java.util.ArrayList;

public class CommandInsert extends SqlCommand implements DatabaseOperations {
    public CommandInsert(ArrayList<String> tokens) { super(tokens); }
    public CommandInsert(ArrayList<String> tokens, String command) { super(tokens, command); }

    public void parser () throws SqlExceptions.ParsingException {
        //The first word should be INTO
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("INTO"))
            throw new SqlExceptions.ParsingException("INTO is needed for INSERT");
        // Check the table name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("table name is needed for INSERT command");
        // The next word should be VALUE 
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("VALUES"))
            throw new SqlExceptions.ParsingException("VALUES is needed after table name");
        // The value list is mandatory and should start with (
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equals("("))
            throw new SqlExceptions.ParsingException("table name is needed for INSERT command");    
        currentWord++;
        parsingList(")", false);
        //Check the ending of the attribute list
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equals(")"))
            throw new SqlExceptions.ParsingException(") is expected for the ending of attribute list");
        //Check whether the command is terminated correctly
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        //you always need to use a database before inserting;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        //Check table name
        String tableName = tokens.get(2);
        checkTableNameExistence(tableName);
        //Create data structure
        Data data = new Data(databaseName, tableName);
        //Check whether the number of the values matches the number of the attributes
        setValues();
        if(values.size() != data.getAttributeNumber() - 1)
            throw new SqlExceptions.InterpretingException("Value number don't much the number of attributes");
        data.insertValues(values);
        data.writeResults();
        return "[OK]";
    }

    private void setValues() {
        for(int i = 5; i < tokens.size(); i++) {
             if(tokens.get(i).equals(")")) return;
             if(!tokens.get(i).equals(",")) values.add(processValues(tokens.get(i)));
        }
    }

    private String processValues(String value) {
        if(value.equalsIgnoreCase("NULL")) return " ";
        if(isStringLiteral(value)) return value.substring(1, value.length() - 1);
        return value;
    }
}
