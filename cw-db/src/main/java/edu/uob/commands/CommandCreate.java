package edu.uob.commands;

import edu.uob.utilities.FileEditor;
import edu.uob.utilities.SqlExceptions;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class CommandCreate extends SqlCommand implements DatabaseOperations{
    public CommandCreate(ArrayList<String> tokens) { super(tokens); }
    public CommandCreate(ArrayList<String> tokens, String command) { super(tokens, command); }

    public void parser() throws SqlExceptions.ParsingException {
        currentWord++;
        if (currentWord >= tokens.size())
                    throw new SqlExceptions.ParsingException("Invalid command");
        switch (tokens.get(currentWord)) {
            case "DATABASE":
                currentWord++;
                parsingDatabase();
                return;
            case "TABLE":
                currentWord++;
                parsingTable();
                return;
            default:
                throw new SqlExceptions.ParsingException("Please use table or database for create command!");
        }
    }

    public String interpreter() throws SqlExceptions.InterpretingException {
        return switch (tokens.get(1)) {
            case "DATABASE" -> {
                createDatabase();
                yield "[OK]";
            }
            case "TABLE" -> {
                creteTable();
                yield "[OK]";
            }
            default -> throw new SqlExceptions.InterpretingException("");
        };
    }

    private void parsingDatabase () throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        //Parse database name 
        if (!super.isPlainText())
            throw new SqlExceptions.ParsingException("Invalid database name");
        //Check the ending of the command
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("Invalid command");
    }

    private void parsingTable () throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        //Check the table name
        if (!super.isPlainText())
            throw new SqlExceptions.ParsingException("Invalid database name");
        //It is either an attribute list starting with ( or an empty attribute list which means the command should end with ;
        currentWord++;
        if (isEmptyAttributeList()) return;
        if (!tokens.get(currentWord).equals("("))
            throw new SqlExceptions.ParsingException("( is expected for starting of attribute list");
        //Parsing the attribute list if the command does not end;
        currentWord++;
        parsingList(")", true);
        //Check the ending of the attribute list
        if (currentWord >= tokens.size() || !tokens.get(currentWord).equals(")"))
            throw new SqlExceptions.ParsingException(") is expected for the ending of attribute list");
        //Check whether the command is terminated correctly
        currentWord++;
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("; is expected for the ending of attribute list");    
    }

    private boolean isEmptyAttributeList () throws SqlExceptions.ParsingException {
        if (currentWord >= tokens.size())
            throw new SqlExceptions.ParsingException("Invalid command");
        if (tokens.get(currentWord).equals(";")) {
            //If it's already a ';', check whether the command has terminated correctly
            if (currentWord != tokens.size() - 1)
                throw new SqlExceptions.ParsingException("Invalid command");
            return true;
        }
        return false;
    }

    private void createDatabase () throws SqlExceptions.InterpretingException {
        String databaseName = tokens.get(2);
        FileEditor fileEditor = new FileEditor();
        fileEditor.createDirectory(databaseName.toLowerCase());
    }

    private void creteTable () throws SqlExceptions.InterpretingException {
        String tableName = tokens.get(2);
        FileEditor fileEditor = new FileEditor();
        //When server is restarted, you always need to use a database;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        //Build the table
        String path = databaseName.toLowerCase() + File.separator  + tableName.toLowerCase();
        fileEditor.createFile(path);
        String attributeList = constructAttributeList();
        fileEditor.writeToFile(path, attributeList);
        //!!!!!Manipulate the unique_id.txt for the id tracing
        initiateId(databaseName, tableName);
    }

    private String constructAttributeList () {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id");
        if (tokens.size() == 4 ) return stringBuilder.toString(); //Empty attribute list
        stringBuilder.append("\t");
        for (int i = 4; i < tokens.size() - 2; i++) {
            if (!tokens.get(i).equals(",")) {
                stringBuilder.append(tokens.get(i));
                if(i != tokens.size() - 3) stringBuilder.append("\t"); //Discard the last tab
            }
        }
        return stringBuilder.toString();
    }
     private void initiateId (String databaseName, String tableName) throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        fileEditor.modifyIdFile(databaseName.toLowerCase(), tableName.toLowerCase());
     }
}
