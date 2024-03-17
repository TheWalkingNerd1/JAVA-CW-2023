package edu.uob.commands;

import edu.uob.utilities.FileEditor;
import edu.uob.utilities.SqlExceptions;

import java.io.File;
import java.util.ArrayList;

public class SqlCommand {
    protected ArrayList<String> tokens;
    protected final String command;
    protected int currentWord;
    protected static String databaseName;
    protected ArrayList<String> values;

    public  SqlCommand(ArrayList<String> tokens) {
        command = ""; //Command is not needed for parser
        this.tokens = tokens;
        currentWord = 0;
    }

    public  SqlCommand(ArrayList<String> tokens, String command) {
        this.tokens = tokens;
        this.command = command;
        values = new ArrayList<>(); //Value List are needed for interpreter
    }

    //Default implement for avoiding casting the sqlCommand reference
    public void parser () throws SqlExceptions.ParsingException {}
    public String interpreter() throws SqlExceptions.InterpretingException {return "";}


    public static SqlCommand parserCommandType (ArrayList<String> tokens) {
        return switch (tokens.get(0)) {
            case "SELECT" -> new CommandSelect(tokens);
            case "CREATE" -> new CommandCreate(tokens);
            case "USE" -> new CommandUse(tokens);
            case "INSERT" -> new CommandInsert(tokens);
            case "DROP" -> new CommandDrop(tokens);
            case "ALTER" -> new CommandAlter(tokens);
            default -> null;
        };
    }

    public static SqlCommand interpreterCommandType (ArrayList<String> tokens, String command) {
        return switch (tokens.get(0)) {
            case "SELECT" -> new CommandSelect(tokens, command);
            case "CREATE" -> new CommandCreate(tokens, command);
            case "USE" -> new CommandUse(tokens, command);
            case "INSERT" -> new CommandInsert(tokens, command);
            case "DROP" -> new CommandDrop(tokens, command);
            case "ALTER" -> new CommandAlter(tokens, command);
            default -> null;
        };
    }

    protected void checkTableNameExistence(String tableName) throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        if(!fileEditor.isPathExisting(databaseName + File.separator + tableName.toLowerCase() + ".tab"))
            throw new SqlExceptions.InterpretingException("This table doesn't exist!");
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

    protected void parsingList(String ending, boolean isAttributeList) throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("list is needed");
        //The first token must be an attribute
        if(!(isAttributeList ? isPlainText() : isValidValue()))
            throw new SqlExceptions.ParsingException("This is not a valid list");
        //Return condition
        currentWord++;
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        if(tokens.get(currentWord).equals(ending))  return;
        //Recursively parsing the command
        while (!tokens.get(currentWord).equals(ending)) {
            //This word must be a comma
            if (!tokens.get(currentWord).equals(",")) 
                throw new SqlExceptions.ParsingException("Expected a comma between words");
            //This word must be an attribute
            currentWord++;
            if (currentWord >= tokens.size() || tokens.get(currentWord).equals(ending)) 
                throw new SqlExceptions.ParsingException("Invalid command");
            parsingList(ending, isAttributeList);
        }
    }

    protected void setDatabaseName (String databaseName) {
        SqlCommand.databaseName = databaseName;
    }

    protected boolean isValidValue() throws SqlExceptions.ParsingException {
        return isStringLiteral(tokens.get(currentWord)) || isBooleanLiteral() || isNumber() || tokens.get(currentWord).equals("NULL");
    }

    protected boolean isStringLiteral(String token) {
        return token.charAt(0) == '\'' && token.charAt(token.length() - 1) == '\'';
    }

    private boolean isBooleanLiteral() {
        return tokens.get(currentWord).equals("TRUE") || tokens.get(currentWord).equals("FALSE");
    }

    private boolean isNumber() {
        String token = tokens.get(currentWord);

        if (token.startsWith("+") || token.startsWith("-")) token = token.substring(1);
        if (token.isEmpty() || token.startsWith(".") || token.endsWith(".")) return false;

        // Check each character to ensure it's either a digit or at most one dot
        boolean isDot = false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '.') {
                if (isDot) return false;
                isDot = true;
            } else if (!Character.isDigit(c)) return false;
        }
        
        return true;
    }
}
