package edu.uob.commands;

import edu.uob.utilities.FileEditor;
import edu.uob.utilities.SqlExceptions;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class CommandCreate extends SqlCommand implements DatabaseOperations{
    public CommandCreate(ArrayList<String> tokens) { super(tokens); }

    public void parser() throws SqlExceptions.ParsingException {
        currentWord++;
        if (currentWord >= tokens.size())
                    throw new SqlExceptions.ParsingException("Invalid command");
        switch (tokens.get(currentWord).toUpperCase()) {
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
        return switch (tokens.get(1).toUpperCase()) {
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
            throw new SqlExceptions.ParsingException("Invalid table name");
        //It is either an attribute list starting with ( or an empty attribute list which means the command should end with ;
        currentWord++;
        if (isEmptyAttributeList()) return;
        if (!tokens.get(currentWord).equals("("))
            throw new SqlExceptions.ParsingException("( is expected for starting of attribute list");
        //Parsing the attribute list if the command does not end;
        currentWord++;
        parsingList(")", true);
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
        //you always need to use a database before creating table;
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        //Check Attribute list
        ArrayList<String> attributeToAdd = checkAttribute();
        //Build the table
        String path = databaseName.toLowerCase() + File.separator  + tableName.toLowerCase();
        fileEditor.createFile(path);
        String attributeList = constructAttributeList(attributeToAdd);
        fileEditor.writeToFile(path, attributeList);
        //!!!!!Manipulate the unique_id.txt for the id tracing
        initiateId(databaseName, tableName);
    }

    private ArrayList<String> checkAttribute() throws SqlExceptions.InterpretingException {
        ArrayList<String> attributeToAdd = new ArrayList<>();
        attributeToAdd.add("id");
        if(tokens.size() == 4 ) return attributeToAdd;
        for (int i  = 4; i < tokens.size() - 2; i++) {
            if(!tokens.get(i).equals(",")) {
                for(String string : attributeToAdd) {
                    if(string.equalsIgnoreCase(tokens.get(i)))
                        throw new SqlExceptions.InterpretingException("You can't add identical attributes");
                }
                attributeToAdd.add(tokens.get(i));
            }
        }
        return attributeToAdd;
    }

    private String constructAttributeList (ArrayList<String> attributeToAdd) throws SqlExceptions.InterpretingException {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : attributeToAdd) {
                stringBuilder.append(string);
                stringBuilder.append("\t");
        }
        return stringBuilder.toString();
    }
     private void initiateId (String databaseName, String tableName) throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        fileEditor.modifyIdFile(databaseName.toLowerCase(), tableName.toLowerCase());
     }
}
