package edu.uob.commands;

import edu.uob.utilities.Data;
import edu.uob.utilities.SqlExceptions;

import java.util.ArrayList;
import java.util.Map;

public class CommandDelete extends SqlCommand implements DatabaseOperations {
    public CommandDelete(ArrayList<String> tokens) { super(tokens); }

    public void parser () throws SqlExceptions.ParsingException {
        //The first word should be a From name
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("From"))
            throw new SqlExceptions.ParsingException("from");
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid table name");
        //The first word should be a where
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("Where"))
            throw new SqlExceptions.ParsingException("where");
        currentWord++;
        parserCondition();
        //Check whether the command is terminated
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        //you always need to use a database before joining;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        String tableName = tokens.get(2);
        checkTableNameExistence(tableName);
        Data data = new Data(databaseName,tableName);
        //find the start of the condition
        currentWord = 4;
        data.selectData();
        ArrayList<Integer> finalResult = selectResult(data);
        data.deleteRecord(finalResult);
        return "[OK]";
    }
}
