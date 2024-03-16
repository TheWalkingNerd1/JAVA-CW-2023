package edu.uob.commands;

import edu.uob.utilities.SqlExceptions;

public interface DatabaseOperations {
    void parser () throws SqlExceptions.ParsingException;

    String interpreter() throws SqlExceptions.InterpretingException;

}
