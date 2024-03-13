package edu.uob;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Objects;

public class CommandCreate extends SqlCommand implements DatabaseOperations{
    public CommandCreate(ArrayList<String> tokens) {
        super(tokens);
    }

    public void parsingResult() throws ParsingException {
        switch (tokens.get(currentWord)) {
            case "DATABASE":
                currentWord++;
                parsingDatabase();
                return;
            case "TABLE":
                currentWord++;
                parsingDatabase();
                return;
            default:
                throw new ParsingException("Please use table or database for create command!");
        }
    }

    private void parsingDatabase () throws ParsingException {
        System.out.println(currentWord);
        if( tokens.size() < 4)
            throw new ParsingException("Too short command");
        if( tokens.size() > 4)
            throw new ParsingException("Too long command");
        if( !Objects.equals(tokens.get(currentWord + 1), ";"))
            throw new ParsingException("Please finish the command with a ;");
    }
}
