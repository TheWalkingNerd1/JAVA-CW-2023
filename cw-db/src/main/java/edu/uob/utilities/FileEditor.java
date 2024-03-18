package edu.uob.utilities;

import java.io.File;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileEditor {
    private final String storageFolderPath;
    private final String idFileName = "unique_id.txt";

    public FileEditor() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
    }

    public void createDirectory (String directoryName) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + directoryName);
        if (Files.exists(path)) throw new SqlExceptions.InterpretingException("Database already exists");
        try {Files.createDirectories(path);} catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    public boolean isPathExisting (String pathToCheck) {
        Path path = Path.of(storageFolderPath + File.separator + pathToCheck);
        return Files.exists(path);
    }

    public void createFile (String filePath) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator  + filePath + ".tab");
        if (Files.exists(path)) throw new SqlExceptions.InterpretingException("Table already exists");
        try {Files.createFile(path);} catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    public void writeToFile (String pathToWrite,String buffer) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + pathToWrite + ".tab");
        try {Files.write(path, buffer.getBytes());} catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    public void modifyIdFile(String databaseName, String tableName) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + databaseName + File.separator + idFileName);
        String context = tableName + " 0\n" ;
        try {
            Files.write(path, context.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    public int readID(String databaseName, String tableName) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + databaseName + File.separator + idFileName);
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if(line.contains(tableName)){
                    return readIdFromLine(line);
                }
            }
            throw new SqlExceptions.InterpretingException("Didn't find the line.");
        } catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    private int readIdFromLine(String line) throws SqlExceptions.InterpretingException {
        String[] words = line.split(" ");
        String lastWord = words[words.length - 1];
        try {
            return Integer.parseInt(lastWord);
        } catch (NumberFormatException e) {throw new SqlExceptions.InterpretingException("Something is wrong when reading id");}
    }

    public List<String> readTable(String databaseName, String tableName) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + databaseName + File.separator + tableName + ".tab");
        try {
            List<String> buffer = Files.readAllLines(path);
            if (buffer.isEmpty()) throw new SqlExceptions.InterpretingException("Something is Wrong with the table's context");
            return buffer;
        } catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    public void writeId(String databaseName, String tableName, int id) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + databaseName + File.separator + idFileName);
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(path));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                List<String> words = List.of(line.split("\\s+"));
                if (words.contains(tableName)) {
                    String[] parts = line.split(" ");
                    parts[parts.length - 1] = String.valueOf(id);
                    lines.set(i, String.join(" ", parts));
                }
            }
            Files.write(path, lines);
        } catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    public void deleteFile(String filePath) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + filePath);
        try {
            Files.delete(path);
        } catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    public void deleteIdRecord(String databaseName, String tableName) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + databaseName + File.separator + idFileName);
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(path));
            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                List<String> words = Arrays.asList(line.split("\\s+"));
                if (!words.contains(tableName)) {
                    updatedLines.add(line);
                }
            }
            Files.write(path, updatedLines);
        } catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }

    public void deleteDirectory(String databaseName) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + databaseName );
        try (DirectoryStream<Path> tree = Files.newDirectoryStream(path)) {
            for (Path file : tree) {
                Files.delete(file);
            }
        } catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
        // Delete the directory itself
        try {
            Files.delete(path);
        } catch (IOException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
    }
}
