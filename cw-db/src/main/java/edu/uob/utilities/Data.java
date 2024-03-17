package edu.uob.utilities;

import java.io.File;
import java.util.*;
import java.util.Arrays;

public class Data {
    private ArrayList<Map<String, String>> records;
    private int id;
    private ArrayList<String> attributes;
    private final String tableName;
    private final String databaseName;

    public Data(String databaseName, String tableName) throws SqlExceptions.InterpretingException {
        this.databaseName = databaseName.toLowerCase();
        this.tableName = tableName.toLowerCase();
        id = readID();
        records = new ArrayList<>();
        createData();
    }

    public int getAttributeNumber() {
        return attributes.size();
    }

    public void insertValues(ArrayList<String> values) throws SqlExceptions.InterpretingException {
        //Robustness check
        if(values.size() != attributes.size() - 1) throw new SqlExceptions.InterpretingException("Something Wrong when trying to insert values");
        id++;
        Map<String, String> record = new HashMap<>();
        record.put(attributes.get(0), String.valueOf(id));
        for (int i = 1; i < attributes.size(); i++) {
            record.put(attributes.get(i), values.get(i - 1));
        }
        records.add(record);
        writeId(id);
    }

    public void writeResults() throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        String result = constructResult();
        fileEditor.writeToFile(databaseName + File.separator + tableName, result);
    }

    public void insertAttribute(String attributeName) throws SqlExceptions.InterpretingException {
        //You can't insert the existed attributes
        if(attributes.contains(attributeName))throw new SqlExceptions.InterpretingException("Attribute name exists!");

        attributes.add(attributeName);
        if (!records.isEmpty()) {
            for (Map<String, String> record : records) {
                record.put(attributeName, " ");
            }
        }
        writeResults();
    }

    public void dropAttribute(String attributeName) throws SqlExceptions.InterpretingException {
        //You can't delete the existed attributes
        if(!attributes.contains(attributeName))throw new SqlExceptions.InterpretingException("Attribute name doesn't exist!");
        if (!records.isEmpty()) {
            for (Map<String, String> record : records) {
                record.remove(attributeName);
            }
        }
        attributes.remove(attributeName);
        writeResults();
    }

    private int readID() throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        return fileEditor.readID(databaseName, tableName);
    }

    private void createData() throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        List<String> buffer = fileEditor.readTable(databaseName, tableName);
        //The attributes must exist
        String[] attributesFromTable = buffer.get(0).split("\t");
        attributes = new ArrayList<>(Arrays.asList(attributesFromTable));
        if(buffer.size() > 1) createMap(buffer);
    }

    private void createMap(List<String> buffer) {
        for (int i = 1; i < buffer.size(); i++) {
            String[] values = buffer.get(i).split("\\t");
            Map<String, String> record = new HashMap<>();
            for (int j = 0; j < attributes.size(); j++) {
                record.put(attributes.get(j), values[j]);
            }
            records.add( record);
        }
    }

    private void writeId(int id) throws SqlExceptions.InterpretingException {
        FileEditor fileEditor = new FileEditor();
        fileEditor.writeId(databaseName, tableName, id);
    }

    private String constructResult() {
        StringBuilder result = new StringBuilder();

        for (String attribute : attributes) {
            result.append(attribute);
            result.append('\t');
        }
        result.append('\n');

        if(!records.isEmpty()) {
            for (Map<String, String> record : records) {
                for (String attribute : attributes) {
                    result.append(record.get(attribute));
                    result.append('\t');
                }
                result.append('\n');
            }
        }

        return result.toString();
    }
}
