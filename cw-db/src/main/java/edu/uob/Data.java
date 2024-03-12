package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class Data {
    private Map<Integer, Map<String, Object>> records = new HashMap<>();
    private int id;
    private String[] headers;

    public Data(String buffer) {
        id = 0;
        createDataCollection(buffer);
    }

    public String constructResult() {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < headers.length; i++) {
            result.append(headers[i]);
            result.append(' ');
        }
        result.append('\n');

        for (int i = 1; i <= id; i++) {
            for (int j = 0; j < headers.length; j++) {
                result.append(records.get(i).get(headers[j]));
                result.append(' ');
            }
            result.append('\n');
        }

        return result.toString();
    }

    private void createDataCollection(String buffer) {
        String[] lines = buffer.split("\\r?\\n");
        headers = lines[0].split("\\t");

        for (int i = 1; i < lines.length; i++) {
            Object[] values = lines[i].split("\\t");
            Map<String, Object> record = new HashMap<>();
            for (int j = 0; j < headers.length; j++) {
                record.put(headers[j], values[j]);
            }
            id++;
            this.records.put(id, record);
        }
    }

}
