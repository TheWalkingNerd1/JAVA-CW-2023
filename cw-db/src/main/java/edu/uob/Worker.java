package edu.uob;

import edu.uob.commands.SqlCommand;
import edu.uob.utilities.SqlExceptions;
import edu.uob.utilities.Tokenizer;

import java.util.ArrayList;

public class Worker {
    private final String command;
    private ArrayList<String> tokens = new ArrayList<>();

    Worker(String command) {
        this.command = command;
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        //Instantiate the command instances based on the first token
        SqlCommand sqlCommand = SqlCommand.interpreterCommandType(tokens, command);
        if (sqlCommand == null)
            throw new SqlExceptions.InterpretingException("");
        // Run the interpreter
        return sqlCommand.interpreter();
    }

    public void parser() throws SqlExceptions.ParsingException {
        Tokenizer tokenizer = new Tokenizer(command);
        tokens = tokenizer.getTokens();
        //Instantiate the command instances based on the first token
        SqlCommand sqlCommand = SqlCommand.parserCommandType(tokens);
        if (sqlCommand == null) 
            throw new SqlExceptions.ParsingException("Please provide a valid sql command");
        //Run the parser
        sqlCommand.parser();
    }
}
