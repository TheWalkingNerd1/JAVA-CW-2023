package edu.uob;

import com.sun.source.tree.ReturnTree;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.security.PublicKey;

public class FileEditor {
    private final String storageFolderPath;

    public FileEditor() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
    }

    public void createDirectory (String directoryName) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + directoryName);
        if (Files.exists(path)) throw new SqlExceptions.InterpretingException("Database already exists");
        try { Files.createDirectories(path); } catch (IOException e) {
            throw new SqlExceptions.InterpretingException(e.getMessage());
        }
    }

    public boolean isDirectoryExisting (String directoryName) {
        Path path = Path.of(storageFolderPath + File.separator + directoryName);
        return Files.exists(path);
    }

    public void createFile (String fileNamePath) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator  + fileNamePath + ".tab");
        System.out.println(path);
        if (Files.exists(path)) throw new SqlExceptions.InterpretingException("Table already exists");
        try { Files.createFile(path); } catch (IOException e) {
            throw new SqlExceptions.InterpretingException(e.getMessage());
        }
    }

    public void writeToFile (String pathToWrite,String buffer) throws SqlExceptions.InterpretingException {
        Path path = Path.of(storageFolderPath + File.separator + pathToWrite + ".tab");
        try { Files.write(path, buffer.getBytes()); } catch (IOException e) {
            throw new SqlExceptions.InterpretingException(e.getMessage());
        }
    }
}
