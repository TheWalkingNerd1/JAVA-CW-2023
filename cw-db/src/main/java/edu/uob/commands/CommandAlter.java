package edu.uob.commands;

import edu.uob.utilities.Data;
import edu.uob.utilities.SqlExceptions;

import java.util.ArrayList;

public class CommandAlter extends SqlCommand implements DatabaseOperations {
    public CommandAlter(ArrayList<String> tokens) { super(tokens); }
    public CommandAlter(ArrayList<String> tokens, String command) { super(tokens, command); }

    public void parser () throws SqlExceptions.ParsingException {
        //The first word should be a TABLE
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equals("TABLE"))
            throw new SqlExceptions.ParsingException("Invalid ALTER command");
        //This word must be a plaintext for table name or database name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid name");
        //Next word should be either ADD or DROP
        currentWord++;
        if (currentWord >= tokens.size() || (!tokens.get(currentWord).equals("ADD") && !tokens.get(currentWord).equals("DROP")))
            throw new SqlExceptions.ParsingException("Please use ADD or DROP for altering a table");
        //This word must be a plaintext for attribute;
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid attribute name");
        //Check whether the command is terminated correctly
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        //you always need to use a database before altering a table;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        String tableName = tokens.get(2);
        checkTableNameExistence(tableName);
        return switch (tokens.get(3)) {
            case "ADD" -> {
                addAttribute(tableName);
                yield "[OK]";
            }
            case "DROP" -> {
                dropAttribute(tableName);
                yield "[OK]";
            }
            default -> throw new SqlExceptions.InterpretingException("");
        };
    }

    private void addAttribute(String tableName) throws SqlExceptions.InterpretingException{
        Data data = new Data(databaseName, tableName);
        //You can't add existing id
        if(data.isAttributeExisting(tokens.get(4))) throw new SqlExceptions.InterpretingException("You can't add existing id");
        data.insertAttribute(tokens.get(4));
    }

    private void dropAttribute(String tableName) throws SqlExceptions.InterpretingException{
        //you can't drop id
        if(tokens.get(4).equalsIgnoreCase("id")) throw new SqlExceptions.InterpretingException("You can't drop id");
        Data data = new Data(databaseName, tableName);
        //You can't drop a non-existed id
        if(!data.isAttributeExisting(tokens.get(4))) throw new SqlExceptions.InterpretingException("You can't drop non-existing id");
        data.dropAttribute(tokens.get(4));
    }
}
