package edu.uob.utilities;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.File;
import java.lang.invoke.SwitchPoint;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Arrays;

public class Data {
    private ArrayList<Map<String, String>> records;
    public ArrayList<Map<String, String>> selectedRecords;
    private ArrayList<String> selectAttributes;
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

    public boolean isAttributeExisting(String attribute) {
        for(String attributeToCompare : attributes) {
            if(attributeToCompare.equalsIgnoreCase(attribute)) return true;
        }
        return false;
    }

    public void insertValues(ArrayList<String> values) throws SqlExceptions.InterpretingException {
        //Robustness check
        if(values.size() != attributes.size() - 1) throw new SqlExceptions.InterpretingException("Something Wrong when trying to insert values");
        id++;
        Map<String, String> record = new HashMap<>();
        record.put(attributes.get(0), String.valueOf(id));
        for (int i = 1; i < attributes.size(); i++) {
            record.put(attributes.get(i).toLowerCase(), values.get(i - 1));
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
                record.put(attributeName.toLowerCase(), " ");
            }
        }
        writeResults();
    }

    public void dropAttribute(String attributeName) throws SqlExceptions.InterpretingException {
        if (!records.isEmpty()) {
            for (Map<String, String> record : records) {
                record.remove(attributeName.toLowerCase());
            }
        }
        attributes.removeIf(attributeToRemove -> attributeToRemove.equalsIgnoreCase(attributeName));

        writeResults();
    }

    public String joinData(Data dataOne, String attributeOne, String attributeTwo) throws SqlExceptions.InterpretingException {
        int id = 1;
        StringBuilder stringBuilder = new StringBuilder();
        if(dataOne.records.isEmpty() || records.isEmpty()) throw new SqlExceptions.InterpretingException("One of the table is empty");
        //Build headline
        stringBuilder.append(String.format("%-10s", "id"));
        for(int i = 1; i < dataOne.getAttributeNumber(); i++) {
            if(!dataOne.attributes.get(i).equalsIgnoreCase(attributeOne)) {
                stringBuilder.append(String.format("%-10s", dataOne.tableName + "." + dataOne.attributes.get(i)));
                stringBuilder.append(" ");
            }
        }
        for(int i = 1; i < getAttributeNumber(); i++) {
            if(!attributes.get(i).equalsIgnoreCase(attributeTwo)) {
                stringBuilder.append(String.format("%-10s",tableName + "." + attributes.get(i)));
                stringBuilder.append(" ");
            }
        }
        stringBuilder.append('\n');
        //build the table
        for (int i = 0; i< dataOne.records.size(); i++) {
            for (int j = 0; j< records.size(); j++) {
                if(dataOne.records.get(i).get(attributeOne.toLowerCase()).equals(records.get(j).get(attributeTwo).toLowerCase())) {
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
            result.append(" ");
        }
        result.append('\n');
        if(!records.isEmpty()) {
            for (Map<String, String> record : records) {
                for (String attribute : attributes) {
                    result.append(String.format("%-10s",record.get(attribute.toLowerCase())));
                    result.append(" ");
                }
                result.append('\n');
            }
        }
        return result.toString();
    }

    public void deleteRecord(ArrayList<Integer> recordsToDelte) throws SqlExceptions.InterpretingException{
        if(recordsToDelte.isEmpty()) return;
        for (int i = records.size(); i > 0; i--) {
            if(recordsToDelte.contains(i)) records.remove(i);
        }
        writeResults();
    }

    public String constructOutput(ArrayList<String> attributeList) throws SqlExceptions.InterpretingException {
        StringBuilder result = new StringBuilder();
        //Check whether the attribute exists
        for (String attributeToAdd : attributeList) {
            if(!isAttributeExisting(attributeToAdd)) throw new SqlExceptions.InterpretingException("You can't query a non-existed attribute");
        }
        //Build the headline
        for (String attribute : attributeList) {
            result.append(String.format("%-10s",attribute));
            result.append(" ");
        }
        result.append('\n');
        if(!records.isEmpty()) {
           result.append(addRecords(attributeList));
        }
        return result.toString();
    }

    public void selectData() {
        selectedRecords = records;
        selectAttributes = attributes;
    }

    public void updateValue(Map<String, String> valuePair, ArrayList<Integer> finalResult) throws SqlExceptions.InterpretingException {
        for (String attributeToAdd : valuePair.keySet()) {
            //Check the existence first
            if(!isAttributeExisting(attributeToAdd)) throw new SqlExceptions.InterpretingException("Can't update non-exist values");
        }
        for (String attributeToAdd : valuePair.keySet()) {
            for(int index : finalResult) {
                records.get(index).put(attributeToAdd, valuePair.get(attributeToAdd));
            }
        }
        writeResults();
    }

    public ArrayList<Integer> selectDataOnExpression(Data data, String command) throws SqlExceptions.InterpretingException {
        String[] tokens = command.split(" ");
        //Robustness check
        if(tokens.length != 3 ) throw new SqlExceptions.InterpretingException("Something is wrong for expression!");
        //Check attribute to compare
        String attribute =  tokens[0];
        if(!isAttributeExisting(attribute)) throw new SqlExceptions.InterpretingException("Unavailable attribute");
        //Robustness check for comparator
        return switch (tokens[1].toLowerCase()) {
            case ">" -> selectGreaterThan(tokens, data);
            case "<" -> selectLessThan(tokens, data);
            case ">=" -> selectGreaterEqual(tokens, data);
            case "<=" -> selectLessLessEqual(tokens, data);
            case "==" -> selectEqual(tokens, data);
            case "!=" -> selectNotEqual(tokens, data);
            case "like" -> selectLike(tokens, data);
            default -> throw new SqlExceptions.InterpretingException("Unknown error");
        };
    }

    private ArrayList<Integer> selectGreaterThan(String[] tokens, Data data) throws SqlExceptions.InterpretingException {
        ArrayList<Integer> result = new ArrayList<Integer>();
        float value1 = 0;
        float value2 = 0;
        //First, try to remove the '' for the string
        String word2 = processString(tokens[2]);
        for( int i = 0; i < data.selectedRecords.size(); i++ ) {
            if(isNumber(data.selectedRecords.get(i).get(tokens[0])) && isNumber(word2)) {
                try {
                    value1 = Float.parseFloat(data.selectedRecords.get(i).get(tokens[0]));
                    value2 = Float.parseFloat(word2);
                } catch (NumberFormatException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
                if(value1 > value2) result.add(i);
            }
        }

        return result;
    }

    private ArrayList<Integer> selectLessThan(String[] tokens, Data data) throws SqlExceptions.InterpretingException {
        ArrayList<Integer> result = new ArrayList<Integer>();
        float value1 = 0;
        float value2 = 0;
        //First, try to remove the '' for the string
        String word2 = processString(tokens[2]);
        for( int i = 0; i < data.selectedRecords.size(); i++ ) {
            if(isNumber(data.selectedRecords.get(i).get(tokens[0])) && isNumber(word2)) {
                try {
                    value1 = Float.parseFloat(data.selectedRecords.get(i).get(tokens[0]));
                    value2 = Float.parseFloat(word2);
                } catch (NumberFormatException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
                if(value1 < value2) result.add(i);
            }
        }

        return result;
    }


    private ArrayList<Integer> selectGreaterEqual(String[] tokens, Data data) throws SqlExceptions.InterpretingException {
        ArrayList<Integer> result = new ArrayList<Integer>();
        float value1 = 0;
        float value2 = 0;
        //First, try to remove the '' for the string
        String word2 = processString(tokens[2]);
        for( int i = 0; i < data.selectedRecords.size(); i++ ) {
            if(isNumber(data.selectedRecords.get(i).get(tokens[0])) && isNumber(word2)) {
                try {
                    value1 = Float.parseFloat(data.selectedRecords.get(i).get(tokens[0]));
                    value2 = Float.parseFloat(word2);
                } catch (NumberFormatException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
                if(value1 >= value2) result.add(i);
            }
        }

        return result;
    }

    private ArrayList<Integer> selectLessLessEqual(String[] tokens, Data data) throws SqlExceptions.InterpretingException {
        ArrayList<Integer> result = new ArrayList<Integer>();
        float value1 = 0;
        float value2 = 0;
        //First, try to remove the '' for the string
        String word2 = processString(tokens[2]);
        for( int i = 0; i < data.selectedRecords.size(); i++ ) {
            if(isNumber(data.selectedRecords.get(i).get(tokens[0])) && isNumber(word2)) {
                try {
                    value1 = Float.parseFloat(data.selectedRecords.get(i).get(tokens[0]));
                    value2 = Float.parseFloat(word2);
                } catch (NumberFormatException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
                if(value1 <= value2) result.add(i);
            }
        }

        return result;
    }

    private ArrayList<Integer> selectEqual(String[] tokens, Data data) throws SqlExceptions.InterpretingException {
        ArrayList<Integer> result = new ArrayList<Integer>();
        float value1 = 0;
        float value2 = 0;
        //First, try to remove the '' for the string
        String word2 = processString(tokens[2]);
        if (word2.equalsIgnoreCase("null")) word2 = " ";
        for( int i = 0; i < data.selectedRecords.size(); i++ ) {
            if(word2.equalsIgnoreCase("True")) {
                if(data.selectedRecords.get(i).get(tokens[0]).equalsIgnoreCase("True")) result.add(i);
            }else if(word2.equalsIgnoreCase("FALSE")) {
                if(data.selectedRecords.get(i).get(tokens[0]).equalsIgnoreCase("FALSE")) result.add(i);
            } else if(isNumber(data.selectedRecords.get(i).get(tokens[0])) && isNumber(word2)) {
                try {
                    value1 = Float.parseFloat(data.selectedRecords.get(i).get(tokens[0]));
                    value2 = Float.parseFloat(word2);
                } catch (NumberFormatException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
                if(value1 == value2) result.add(i);
            } else if (data.selectedRecords.get(i).get(tokens[0]).equalsIgnoreCase(word2)){
                  result.add(i);
            }
        }

        return result;
    }

    private ArrayList<Integer> selectNotEqual(String[] tokens, Data data) throws SqlExceptions.InterpretingException {
        ArrayList<Integer> result = new ArrayList<Integer>();
        float value1 = 0;
        float value2 = 0;
        //First, try to remove the '' for the string
        String word2 = processString(tokens[2]);
        if (word2.equalsIgnoreCase("null")) word2 = " ";
        for( int i = 0; i < data.selectedRecords.size(); i++ ) {
            if(word2.equalsIgnoreCase("True")) {
                if(!data.selectedRecords.get(i).get(tokens[0]).equalsIgnoreCase("True")) result.add(i);
            }else if (word2.equalsIgnoreCase("FALSE")) {
                if(!data.selectedRecords.get(i).get(tokens[0]).equalsIgnoreCase("FALSE")) result.add(i);
            }else if(isNumber(data.selectedRecords.get(i).get(tokens[0])) && isNumber(word2)) {
                try {
                    value1 = Float.parseFloat(data.selectedRecords.get(i).get(tokens[0]));
                    value2 = Float.parseFloat(word2);
                } catch (NumberFormatException e) {throw new SqlExceptions.InterpretingException(e.getMessage());}
                if(value1 != value2) result.add(i);
            } else if (!data.selectedRecords.get(i).get(tokens[0]).equals(word2)){
                result.add(i);
            }
        }

        return result;
    }

    private boolean isNumber(String token) {
        if (token.startsWith("+") || token.startsWith("-")) token = token.substring(1);
        if (token.isEmpty() || token.startsWith(".") || token.endsWith(".")) return false;

        // Check each character to ensure it's either a digit or at most one dot
        boolean isDot = false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '.') {
                if (isDot) return false;
                isDot = true;
            } else if (!Character.isDigit(c)) return false;
        }

        return true;
    }
    public String constructSelectedOutput(ArrayList<Integer> result) {
        StringBuilder stringBuilder = new StringBuilder();
        for(String string : selectAttributes) {
            stringBuilder.append(string);
            stringBuilder.append("\t");
        }
        stringBuilder.append("\n");
        if(result.isEmpty()) return stringBuilder.toString();
        for(int i = 0; i < selectedRecords.size(); i++ ) {
            if(result.contains(i)) {
                for(String attribute : attributes) {
                    stringBuilder.append(selectedRecords.get(i).get(attribute.toLowerCase()));
                    stringBuilder.append(" ");
                }
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public String constructSelectedOutput(ArrayList<Integer> result, ArrayList<String> attributesListToAdd) {
        StringBuilder stringBuilder = new StringBuilder();
        for(String string : attributesListToAdd) {
            stringBuilder.append(string);
            stringBuilder.append("\t");
        }
        stringBuilder.append("\n");
        if(result.isEmpty()) return stringBuilder.toString();
        for(int i = 0; i < selectedRecords.size(); i++ ) {
            if(result.contains(i)) {
                for(String attribute : attributesListToAdd) {
                    stringBuilder.append(selectedRecords.get(i).get(attribute.toLowerCase()));
                    stringBuilder.append(" ");
                }
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private ArrayList<Integer> selectLike(String[] tokens, Data data) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if(data.selectedRecords.isEmpty()) return result;
        //Prepare the data
        String string = processString(tokens[2]);
        for(int i = 0; i < data.selectedRecords.size(); i++) {
            System.out.println(data.selectedRecords.get(i).get(tokens[0]));
            if(string.equalsIgnoreCase("null")) {
                if ((data.selectedRecords.get(i).get(tokens[0]).equals(" "))) result.add(i);
            }else if(string.equalsIgnoreCase("True")) {
                if ((data.selectedRecords.get(i).get(tokens[0]).equalsIgnoreCase("True"))) result.add(i);
            }else if(string.equalsIgnoreCase("false")) {
                if ((data.selectedRecords.get(i).get(tokens[0]).equalsIgnoreCase("false"))) result.add(i);
            }else if (data.selectedRecords.get(i).get(tokens[0]).contains(string)) result.add(i);
        }
        return result;
    }

    private String processString(String string) {
        if(string.charAt(0) == '\'' && string.charAt(string.length() - 1) == '\'') {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    private String addRecords(ArrayList<String> attributeList) {
        StringBuilder result = new StringBuilder();
        for (Map<String, String> record : records) {
            for (String attributeToAdd : attributeList) {
                for (String attribute : attributes) {
                    if(attributeToAdd.equalsIgnoreCase(attribute)) {
                        result.append(String.format("%-10s", record.get(attribute.toLowerCase())));
                        result.append(" ");
                    }
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
            if(!dataOne.attributes.get(i).equalsIgnoreCase(attributeOne)) {
                stringBuilder.append(String.format("%-10s", dataOne.records.get(indexOne).get(dataOne.attributes.get(i).toLowerCase())));
                stringBuilder.append(" ");
            }
        }
        for(int i = 1; i < getAttributeNumber(); i++) {
            if(!attributes.get(i).equalsIgnoreCase(attributeTwo)){
                stringBuilder.append(String.format("%-10s", records.get(indexTwo).get(attributes.get(i).toLowerCase())));
                stringBuilder.append(" ");
            }
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
        if(buffer.size() > 1) {
            createMap(buffer);
        }
    }

    private void createMap(List<String> buffer) {
        for (int i = 1; i < buffer.size(); i++) {
            String[] values = buffer.get(i).split("\\t");
            Map<String, String> record = new HashMap<>();
            for (int j = 0; j < attributes.size(); j++) {
                record.put(attributes.get(j).toLowerCase(), values[j]);
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
                    result.append(record.get(attribute.toLowerCase()));
                    result.append('\t');
                }
                result.append('\n');
            }
        }

        return result.toString();
    }
}
