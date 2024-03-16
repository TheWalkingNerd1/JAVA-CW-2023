package edu.uob.commands;

import edu.uob.utilities.SqlExceptions;

import java.util.ArrayList;

public class CommandSelect extends SqlCommand implements DatabaseOperations {
    
    public CommandSelect(ArrayList<String> tokens) { super(tokens); }
    public CommandSelect(ArrayList<String> tokens, String command) { super(tokens, command); }

    public void parser() throws SqlExceptions.ParsingException {
        if (!tokens.contains("FROM")) 
            throw new SqlExceptions.ParsingException("From must be included in select");
        if (tokens.get(currentWord).equals("FROM")) 
            throw new SqlExceptions.ParsingException("Select command must contain Attributes");
        
        parsingWildAttributeList();
        
        currentWord++;
        if(currentWord == tokens.size())
            throw new SqlExceptions.ParsingException("Table name is need!");

        currentWord++;
        if(!tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is need to finish the command");
    }

    private void parsingWildAttributeList() throws SqlExceptions.ParsingException {
        if(tokens.get(currentWord).equals("*")) return;
        super.parsingList("FROM",true);
    }
        

}
