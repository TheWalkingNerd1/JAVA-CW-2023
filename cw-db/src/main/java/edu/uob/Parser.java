package edu.uob;

import java.io.*;
import java.nio.file.Paths;
import edu.uob.Data;

public class Parser {
    private String command;
    private final String storageFolderPath;
    private String filename;

    Parser() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
    }

    public String parsingResult(String command) {
        StringBuilder contentBuilder = new StringBuilder();
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
        Data data = new Data(contentBuilder.toString());
        return data.constructResult();
    }

}
