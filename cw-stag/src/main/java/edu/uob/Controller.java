package edu.uob;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Controller {
    private Map<String, GameEntity> entities;
    private HashMap<String, HashSet<GameAction>> actions;
    final private String command;
    ArrayList<String> keywords = new ArrayList<>();

    public Controller(String command, Map<String, GameEntity> entities, HashMap<String, HashSet<GameAction>> actions) {
        this.command = command;
        this.entities = entities;
        this.actions = actions;
        constructKeywords(actions);
        System.out.println(keywords.size());
        for(String key: keywords) {System.out.println(key);}
    }

    private void constructKeywords(HashMap<String, HashSet<GameAction>> actions) {
        for(Map.Entry<String, HashSet<GameAction>> entry : actions.entrySet()) { keywords.add(entry.getKey().toLowerCase()); }
    }
}
