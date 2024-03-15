package edu.uob;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class SqlCommand {
    protected ArrayList<String> tokens;
    protected final String command;
    protected int currentWord;
    protected static String databaseName;

    public  SqlCommand(ArrayList<String> tokens) {
        command = ""; //Command is not needed for parser
        this.tokens = tokens;
        currentWord = 0;
    }

    public  SqlCommand(ArrayList<String> tokens, String command) {
        this.tokens = tokens;
        this.command = command;
        currentWord = 1;
    }

    //Default implement for aoiding casting the sqlCommand reference 
    public void parser () throws SqlExceptions.ParsingException {}
    public String interpreter() throws SqlExceptions.InterpretingException {return "";}


    public static SqlCommand parserCommandType (ArrayList<String> tokens) {
        return switch (tokens.get(0)) {
            case "SELECT" -> new CommandSelect(tokens);
            case "CREATE" -> new CommandCreate(tokens);
            case "USE" -> new CommandUse(tokens);
            default -> null;
        };
    }

    public static SqlCommand interpreterCommandType (ArrayList<String> tokens, String command) {
        return switch (tokens.get(0)) {
            case "SELECT" -> new CommandSelect(tokens, command);
            case "CREATE" -> new CommandCreate(tokens, command);
            case "USE" -> new CommandUse(tokens, command);
            default -> null;
        };
    }

    public String getDatabaseName () {
        return databaseName;
    }

    protected boolean isPlainText() throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        String token = tokens.get(currentWord);
        //Check the validation of the word
        for(int i = 0; i < token.length(); i++) {
            char character = token.charAt(i);
            if (!Character.isDigit(character) && !Character.isLetter(character)) return false;
        }
        return true;
    }

    protected void parsingAttributeList(String ending) throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        //The first token must be an attribute
        if(!isPlainText())
            throw new SqlExceptions.ParsingException("This is not a valid attribute");
        //Return condition
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        if(tokens.get(currentWord).equals(ending))  return;
        //Recursively parsing the command
        while (!tokens.get(currentWord).equals(ending)) {
            //This word must be a comma
            if (!tokens.get(currentWord).equals(",")) 
                throw new SqlExceptions.ParsingException("Expected a comma between attributes");
            //This word must be an attribute
            currentWord++;
            if (currentWord >= tokens.size() || tokens.get(currentWord).equals(ending)) 
                throw new SqlExceptions.ParsingException("Invalid command");
            parsingAttributeList(ending);
        }
    }

    protected void setDatabaseName (String databaseName) {
        this.databaseName = databaseName;
    }
}
