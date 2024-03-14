package edu.uob;

import java.util.ArrayList;

public class Worker {
    private final String command;
    ArrayList<String> tokens = new ArrayList<>();

    Worker(String command) {
        this.command = command;
    }

    public String parsingResult() {
        if (command == null || command.trim().isEmpty()) return "";
        return tokenParser(command);
    }

    public String interpretingResult() {
        SqlCommand sqlCommand = SqlCommand.interpreterCommandType(tokens, command);
        try {
            return sqlCommand.interpreter();
        } catch (SqlExceptions.InterpretingException e) {
            return "[Error]" + e.getMessage();
        }
    }

    private String tokenParser(String command) {
        Tokenizer tokenizer = new Tokenizer(command);
        tokens = tokenizer.getTokens();
        //Instantiate the command instances based on the first token
        SqlCommand sqlCommand = SqlCommand.parserCommandType(tokens);
        if (sqlCommand == null) return "[Error]";
        //Run the parser
        try {
            sqlCommand.parser();
            return "";
        } catch (SqlExceptions.ParsingException e) {
            return "[ERROR]" + e.getMessage();
        }
    }
}
