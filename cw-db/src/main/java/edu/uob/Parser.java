package edu.uob;

import java.io.*;
import java.nio.file.Paths;

public class Parser {
    private String command;
    private final String storageFolderPath;
    private String filename;

    Parser() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
    }

    public String parsingResult(String command) {
        StringBuilder contentBuilder = new StringBuilder();
        System.out.println(storageFolderPath + File.separator + "people.tab");
        try {
            FileReader fileReader = new FileReader(storageFolderPath + File.separator + "people.tab");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Error");
        }
        return contentBuilder.toString();
    }

}
