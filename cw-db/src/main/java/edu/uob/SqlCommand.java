package edu.uob;

import java.util.ArrayList;

public class SqlCommand {
    protected ArrayList<String> tokens = new ArrayList<String>();
    protected String command;
    protected int currentWord;

    public  SqlCommand(ArrayList<String> tokens) {
        this.tokens = tokens;
        currentWord = 1;
    }

    public  SqlCommand(ArrayList<String> tokens, String command) {
        this.tokens = tokens;
        this.command = command;
        currentWord = 1;
    }

    public void parser () throws SqlExceptions.ParsingException {}
    public String interpreter() throws SqlExceptions.InterpretingException {return "";}


    public static SqlCommand parserCommandType (ArrayList<String> tokens) {
        return switch (tokens.get(0)) {
            case "SELECT" -> new CommandSelect(tokens);
            case "CREATE" -> new CommandCreate(tokens);
            default -> null;
        };
    }

    public static SqlCommand interpreterCommandType (ArrayList<String> tokens, String command) {
        return switch (tokens.get(0)) {
            case "SELECT" -> new CommandSelect(tokens, command);
            case "CREATE" -> new CommandCreate(tokens, command);
            default -> null;
        };
    }

    protected boolean isPlainText(String token) {
        return true;
    }

    protected void parsingAttributeList(String ending) throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        if(tokens.get(currentWord).equals(ending))  return;
        if(tokens.get(currentWord).equals(","))
            throw new SqlExceptions.ParsingException(", error");
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        while (!tokens.get(currentWord).equals(ending)) {
            if (!tokens.get(currentWord).equals(",")) {
                throw new SqlExceptions.ParsingException("Expected a comma between attributes");
            }
            currentWord++;
            if (currentWord >= tokens.size())
                throw new SqlExceptions.ParsingException("Invalid command");
            if (tokens.get(currentWord).equals(ending)) {
                throw new SqlExceptions.ParsingException("unexpected ending near " + ending);
            }
            parsingAttributeList(ending);
        }
    }
}
