package edu.uob.commands;

import edu.uob.utilities.FileEditor;
import edu.uob.utilities.SqlExceptions;

import java.io.File;
import java.util.ArrayList;

public class CommandDrop extends SqlCommand implements DatabaseOperations {
    public CommandDrop(ArrayList<String> tokens) { super(tokens); }
    public CommandDrop(ArrayList<String> tokens, String command) { super(tokens, command); }

    public void parser () throws SqlExceptions.ParsingException {
        //The first word should be neither a DATABASE nor a TABLE
        currentWord++;
        if (currentWord >= tokens.size() || (!tokens.get(currentWord).equalsIgnoreCase("DATABASE") && !tokens.get(currentWord).equalsIgnoreCase("TABLE")))
            throw new SqlExceptions.ParsingException("Invalid drop command");
        //This word must be a plaintext for table name or database name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid name");
        //Check whether the command is terminated correctly
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");
    }
    public String interpreter() throws SqlExceptions.InterpretingException {
        return switch (tokens.get(1).toUpperCase()) {
            case "TABLE" -> {
                dropTable();
                yield "[OK]";
            }
            case "DATABASE" -> {
                dropDatabase();
                yield "[OK]";
            }
            default -> throw new SqlExceptions.InterpretingException("");
        };
    }

    private void dropTable() throws SqlExceptions.InterpretingException {
        //you always need to use a database before dropping a table;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        String tableName = tokens.get(2);
        FileEditor fileEditor = new FileEditor();
        String path = databaseName + File.separator + tableName.toLowerCase() + ".tab";
        //You can't drop a table which doesn't exist
        if( !fileEditor.isPathExisting(path) )
            throw new SqlExceptions.InterpretingException("the table doesn't exist!");
        fileEditor.deleteFile(path);
        //Update the id record
        fileEditor.deleteIdRecord(databaseName, tableName.toLowerCase());
    }

    private void dropDatabase() throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        //It is allowed to delete database without using it, but the existence needs to be checked first
        if(!fileEditor.isPathExisting(tokens.get(2).toLowerCase()))
            throw new SqlExceptions.InterpretingException("You can't delete a non-existence database");
        if(databaseName == null) {
            fileEditor.deleteDirectory(tokens.get(2).toLowerCase());
            return;
        }
        //If the current database is being used, it should not be valid after dropping
        if(databaseName.equals(tokens.get(2).toLowerCase())) setDatabaseName(null);
        fileEditor.deleteDirectory(tokens.get(2).toLowerCase());
    }
}
