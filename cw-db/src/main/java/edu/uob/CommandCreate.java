package edu.uob;

import java.io.File;
import java.util.ArrayList;

public class CommandCreate extends SqlCommand implements DatabaseOperations{
    public CommandCreate(ArrayList<String> tokens) {
        super(tokens);
    }
    public CommandCreate(ArrayList<String> tokens, String command) {
        super(tokens, command);
    }

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
        //Parse the attribute list
        currentWord++;
        parsingAttributeList(";");
        //Check the ending of the command
        if (currentWord != tokens.size() - 1 || !tokens.get(currentWord).equals(";"))
            throw new SqlExceptions.ParsingException("Invalid command");
    }

    private void createDatabase () throws SqlExceptions.InterpretingException {
        String databaseName = tokens.get(2);
        FileEditor fileEditor = new FileEditor();
        fileEditor.createDirectory(databaseName.toLowerCase());
    }

    private void creteTable () throws SqlExceptions.InterpretingException {
        String tableName = tokens.get(2);
        FileEditor fileEditor = new FileEditor();
        if(databaseName == null) throw new SqlExceptions.InterpretingException ("Please use a database first");
        String path = databaseName.toLowerCase() + File.separator  + tableName.toLowerCase();
        fileEditor.createFile(path);
        String attributeList = constructAttributeList();
        fileEditor.writeToFile(path, attributeList);
    }

    private String constructAttributeList () {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id");
        stringBuilder.append("\t");
        for (int i =3; i < tokens.size() - 1; i++) {
            if (!tokens.get(i).equals(",")) {
               // System.out.println(tokens.get(i));
                stringBuilder.append(tokens.get(i));
                if(i != tokens.size() - 2) stringBuilder.append("\t");
            }
        }
        return stringBuilder.toString();
    }
}
