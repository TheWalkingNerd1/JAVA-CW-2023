package edu.uob;

import edu.uob.entities.*;
import edu.uob.utilities.StagExceptions;

import java.util.*;

public class Controller {
    private final Map<String, GameEntity> entities;
    private final HashMap<String, HashSet<GameAction>> actions;
    private String command;
    private final ArrayList<String> keywords = new ArrayList<>();
    private final ArrayList<String> keywordsFromCommand = new ArrayList<>();
    private final ArrayList<Player> players;
    private Player currentPlayer;


    public Controller(String command, Map<String, GameEntity> entities, HashMap<String,
                       HashSet<GameAction>> actions, ArrayList<Player> players) {
        this.command = command;
        this.entities = entities;
        this.actions = actions;
        this.players = players;
        //SetKeyWords
        constructKeywords(actions);
        //Set the current player
        setCurrentPlayer(command);
        //deduct the command
        setCommand(command);
    }

    public String result() throws StagExceptions {
        dismantleCommand();
        GameAction gameAction = fetchAction(keywordsFromCommand, actions);
        return gameAction.getNarration();
    }

    private GameAction fetchAction(ArrayList<String> keywordsFromCommand, HashMap<String, HashSet<GameAction>> actions) {
        List<GameAction> list = new ArrayList<>(actions.get("open"));
        return list.get(0);
    }

    private void dismantleCommand() {
        String[] parts = command.split(" ");
        for(String part : parts) {
            for(String keyword : keywords) {
                if(keyword.equalsIgnoreCase(part)) {
                    keywordsFromCommand.add(part.toLowerCase());
                    break;
                }
            }
        }
    }

    private void setCommand(String command) {
        int colonIndex = command.indexOf(':');
        if (colonIndex != -1)  this.command = command.substring(colonIndex + 1);
    }

    private void initialPlayerLocation(Map<String, GameEntity> entities, Player currentPlayer) {
        LocationEntity locationEntity = finaFirstLocation(entities);
        if(locationEntity != null) currentPlayer.setLocation(locationEntity.getName());
    }

    private LocationEntity finaFirstLocation(Map<String, GameEntity> entities) {
        for(GameEntity gameEntity : entities.values()) {
            if(gameEntity instanceof LocationEntity locationEntity)
                if (locationEntity.isFirstLocation) return locationEntity;
        }
        return null;
    }

    private void setCurrentPlayer(String command) {
        //Get the player's name
        String name = null;
        String[] parts = command.split(":");
        if(parts.length > 1) name = parts[0];
        if(!checkPlayerExistence(name)) {
            System.out.println("Enter Here");
            Player player = new Player(name);
            players.add(player);
            currentPlayer = player;
            //Set the player to firstLocation
            initialPlayerLocation(entities,currentPlayer);
        }
    }

    private void constructKeywords(HashMap<String, HashSet<GameAction>> actions) {
        for(Map.Entry<String, HashSet<GameAction>> entry : actions.entrySet()) { keywords.add(entry.getKey().toLowerCase()); }
    }

    private boolean checkPlayerExistence(String name) {
        for(Player player : players) {
            if(player.getName().equals(name)) {
                currentPlayer = player;
                return true;
            }
        }
        return false;
    }
}
