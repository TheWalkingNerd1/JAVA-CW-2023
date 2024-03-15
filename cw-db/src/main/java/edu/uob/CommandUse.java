package edu.uob;

import java.util.ArrayList;

public class CommandUse extends SqlCommand implements DatabaseOperations {
    public CommandUse(ArrayList<String> tokens) {
        super(tokens);
    }
    public CommandUse(ArrayList<String> tokens, String command) {
        super(tokens, command);
    }

    public void parser() throws SqlExceptions.ParsingException {
        currentWord++;
        if (currentWord >= tokens.size() || !isPlainText())
            throw new SqlExceptions.ParsingException("Invalid command");
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("Invalid command");
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        String databaseName = tokens.get(1);
        FileEditor fileEditor = new FileEditor();
        if (!fileEditor.isDirectoryExisting(databaseName))
            throw new SqlExceptions.InterpretingException("Database not existing");
        setDatabaseName(databaseName);
        System.out.println(databaseName);
        return "[OK]";
    }
}
