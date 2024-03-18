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

    public boolean isAttributeExisting(String attribute) { return attributes.contains(attribute); }

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
        attributes.add(attributeName);
        if (!records.isEmpty()) {
            for (Map<String, String> record : records) {
                record.put(attributeName, " ");
            }
        }
        writeResults();
    }

    public void dropAttribute(String attributeName) throws SqlExceptions.InterpretingException {
        if (!records.isEmpty()) {
            for (Map<String, String> record : records) {
                record.remove(attributeName);
            }
        }
        attributes.remove(attributeName);
        writeResults();
    }

    public String joinData(Data dataOne, String attributeOne, String attributeTwo) throws SqlExceptions.InterpretingException {
        int id = 1;
        StringBuilder stringBuilder = new StringBuilder();
        if(dataOne.records.isEmpty() || records.isEmpty()) throw new SqlExceptions.InterpretingException("One of the table is empty");
        //Build headline
        stringBuilder.append(String.format("%-10s", "id"));
        for(int i = 1; i < dataOne.getAttributeNumber(); i++) {
            if(!dataOne.attributes.get(i).equals(attributeOne))
                stringBuilder.append(String.format("%-10s", dataOne.tableName + "." + dataOne.attributes.get(i)));
        }
        for(int i = 1; i < getAttributeNumber(); i++) {
            if(!attributes.get(i).equals(attributeTwo))
                stringBuilder.append(String.format("%-10s",tableName + "." + attributes.get(i)));
        }
        stringBuilder.append('\n');
        //build the table
        for (int i = 0; i< dataOne.records.size(); i++) {
            for (int j = 0; j< records.size(); j++) {
                if(dataOne.records.get(i).get(attributeOne).equals(records.get(j).get(attributeTwo))) {
                    stringBuilder.append(joinLine(dataOne, i, j, attributeOne, attributeTwo, id));
                    id++;
                }
            }
        }
        return stringBuilder.toString();
    }

    public String constructOutput() {
        StringBuilder result = new StringBuilder();
        for (String attribute : attributes) {
            result.append(String.format("%-10s",attribute));
        }
        result.append('\n');
        if(!records.isEmpty()) {
            for (Map<String, String> record : records) {
                for (String attribute : attributes) {
                    result.append(String.format("%-10s",record.get(attribute)));
                }
                result.append('\n');
            }
        }
        return result.toString();
    }

    public String constructOutput(ArrayList<String> attributeList) throws SqlExceptions.InterpretingException {
        StringBuilder result = new StringBuilder();
        //Check whether the attribute exists
        for (String attributeToAdd : attributeList) {
            System.out.println(attributeToAdd);
            if(!attributes.contains(attributeToAdd)) throw new SqlExceptions.InterpretingException("You can't query a non-existed attribute");
        }
        //Build the headline
        for (String attribute : attributeList) {
            result.append(String.format("%-10s",attribute));
        }
        result.append('\n');
        if(!records.isEmpty()) {
           result.append(addRecords(attributeList));
        }
        return result.toString();
    }

    private String addRecords(ArrayList<String> attributeList) {
        StringBuilder result = new StringBuilder();
        for (Map<String, String> record : records) {
            for (String attributeToAdd : attributeList) {
                for (String attribute : attributes) {
                    if(attributeToAdd.equals(attribute))
                        result.append(String.format("%-10s", record.get(attribute)));

                }
            }
            result.append('\n');
        }
        return result.toString();
    }

    private String joinLine( Data dataOne, int indexOne, int indexTwo, String attributeOne, String attributeTwo, int id ) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%-10s", id));
        for(int i = 1; i < dataOne.getAttributeNumber(); i++) {
            if(!dataOne.attributes.get(i).equals(attributeOne))
                stringBuilder.append(String.format("%-10s", dataOne.records.get(indexOne).get(dataOne.attributes.get(i))));
        }
        for(int i = 1; i < getAttributeNumber(); i++) {
            if(!attributes.get(i).equals(attributeTwo))
                stringBuilder.append(String.format("%-10s", records.get(indexTwo).get(attributes.get(i))));
        }
        stringBuilder.append('\n');
        return stringBuilder.toString();
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
