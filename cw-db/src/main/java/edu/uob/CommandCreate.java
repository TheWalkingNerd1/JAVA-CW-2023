package edu.uob;

import java.util.ArrayList;

public class CommandCreate extends SqlCommand implements DatabaseOperations{
    public CommandCreate(ArrayList<String> tokens) {
        super(tokens);
    }
    public CommandCreate(ArrayList<String> tokens, String command) {
        super(tokens, command);
    }

    public void parser() throws SqlExceptions.ParsingException {
        switch (tokens.get(currentWord)) {
            case "DATABASE":
                currentWord++;
                if (currentWord >= tokens.size())
                    throw new SqlExceptions.ParsingException("Invalid command");
                parsingDatabase();
                return;
            case "TABLE":
                currentWord++;
                if (currentWord >= tokens.size())
                    throw new SqlExceptions.ParsingException("Invalid command");
                parsingTable();
                return;
            default:
                throw new SqlExceptions.ParsingException("Please use table or database for create command!");
        }
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        return switch (tokens.get(1)) {
            case "DATABASE" -> {
                createDatabase(tokens);
                yield "[OK]";
            }
            case "TABLE" -> {
                creteTable(tokens, command);
                yield "[OK]";
            }
            default -> "[Error]";
        };
    }

    private void parsingDatabase () throws SqlExceptions.ParsingException {
        currentWord++;
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("Invalid command");
    }

    private void parsingTable () throws SqlExceptions.ParsingException {
        currentWord++;
        if(currentWord >= tokens.size() || tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("Create table must contain Attributes");
        parsingAttributeList(";");
    }

    private void createDatabase (ArrayList<String> tokens) {
        System.out.println(tokens.get(2));
    }

    private void creteTable (ArrayList<String> tokens, String command) {

    }
}
