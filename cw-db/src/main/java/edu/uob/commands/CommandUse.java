package edu.uob.commands;

import edu.uob.utilities.FileEditor;
import edu.uob.utilities.SqlExceptions;

import java.util.ArrayList;

public class CommandUse extends SqlCommand implements DatabaseOperations {
    public CommandUse(ArrayList<String> tokens) { super(tokens); }
    public CommandUse(ArrayList<String> tokens, String command) {
        super(tokens, command);
    }

    public void parser() throws SqlExceptions.ParsingException {
        //This word must be the database name
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid command");
        //Check whether the command is terminated correctly;
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("Invalid command");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        String databaseName = tokens.get(1);
        FileEditor fileEditor = new FileEditor();
        //If database already exists, throw exception
        if (!fileEditor.isPathExisting(databaseName.toLowerCase()))
            throw new SqlExceptions.InterpretingException("Database not existing");
        setDatabaseName(databaseName);
        return "[OK]";
    }
}
