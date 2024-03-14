package edu.uob;

public interface DatabaseOperations {
    void parser () throws SqlExceptions.ParsingException;

    String interpreter() throws SqlExceptions.InterpretingException;

}
