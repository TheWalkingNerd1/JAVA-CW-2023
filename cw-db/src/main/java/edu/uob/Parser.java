package edu.uob;

import edu.uob.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Parser {
    private String command;
    private final String storageFolderPath;
    private String filename;
    ArrayList<String> tokens = new ArrayList<String>();

    Parser() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
    }

    // public String parsingResult(String command) {
    //     StringBuilder contentBuilder = new StringBuilder();
    //     try {
    //         FileReader fileReader = new FileReader(storageFolderPath + File.separator + "people.tab");
    //         BufferedReader bufferedReader = new BufferedReader(fileReader);
    //         String line;
    //         while ((line = bufferedReader.readLine()) != null) {
    //             contentBuilder.append(line).append(System.lineSeparator());
    //         }
    //     } catch (IOException e) {
    //         System.out.println("Error");
    //     }
    //     Data data = new Data(contentBuilder.toString());
    //     return data.constructResult();
    // }

    public String parsingResult(String command) {
        if (command == null || command.trim().isEmpty()) return "";
        else if (command.contains("\t")) return "[Error]";
        return tokenParsingResult(command);
    }

    private String tokenParsingResult(String command) {
        Tokenizer tokenizer = new Tokenizer(command);
        tokens = tokenizer.getTokens();
        SqlCommand sqlCommand = SqlCommand.parseCommandType(tokens);
        if (sqlCommand == null) return "[Error]";
        try {
            ((DatabaseOperations)sqlCommand).parsingResult();
            return "[OK]";
        } catch (ParsingException e) {
            return "[ERROR]" + e.getMessage();
        }
    }
}
