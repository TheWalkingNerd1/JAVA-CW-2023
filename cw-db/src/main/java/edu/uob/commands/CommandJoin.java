package edu.uob.commands;

import edu.uob.utilities.Data;
import edu.uob.utilities.SqlExceptions;

import java.util.ArrayList;

public class CommandJoin extends SqlCommand implements DatabaseOperations {
    public CommandJoin(ArrayList<String> tokens) { super(tokens); }

    public void parser () throws SqlExceptions.ParsingException {
        //The first word should be a TABLE name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid table name");
        //Next word is AND
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("AND"))
            throw new SqlExceptions.ParsingException("AND is expected");
        //Next word must be another table name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid table name");
        //Next word is ON
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("ON"))
            throw new SqlExceptions.ParsingException("ON is expected");
        //Next word must be an attribute name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid attribute name");
        //Next word is And
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equalsIgnoreCase("AND"))
            throw new SqlExceptions.ParsingException("Second AND is expected");
        //Next word must be another attribute name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid attribute name");
        //Check whether the command is terminated correctly
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        //you always need to use a database before joining;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        //Check the table names first
        checkTableNameExistence(tokens.get(1));
        checkTableNameExistence(tokens.get(3));
        //Check the attribute names
        Data dataOne = new Data(databaseName, tokens.get(1));
        Data dataTwo= new Data(databaseName, tokens.get(3));
        if(!dataOne.isAttributeExisting(tokens.get(5)))
            throw new SqlExceptions.InterpretingException("The first attribute doesn't exist!");
        if(!dataTwo.isAttributeExisting(tokens.get(7)))
            throw new SqlExceptions.InterpretingException("The Second attribute doesn't exist!");
        //try join
        return "[OK]\n" + dataTwo.joinData(dataOne, tokens.get(5), tokens.get(7) );
    }
}
