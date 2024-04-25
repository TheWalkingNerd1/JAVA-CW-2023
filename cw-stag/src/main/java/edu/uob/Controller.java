package edu.uob;

import edu.uob.entities.*;
import edu.uob.utilities.StagExceptions;

import java.util.*;

public class Controller {
    private final String[] builtInCommands = {"inventory", "inv", "get", "drop", "goto", "look"};
    private final Map<String, GameEntity> entities;
    private final HashMap<String, HashSet<GameAction>> actions;
    private String command;
    private final ArrayList<String> keywords = new ArrayList<>();
    private final ArrayList<String> keywordsFromCommand = new ArrayList<>();
    private final ArrayList<Player> players;
    private Player currentPlayer;
    private boolean isBuiltInCommand = false;


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
        //Ignore empty command
        if (command == null || command.trim().isEmpty()) return ""; 
        //extract keyWords
        dismantleCommand();
        checkCommandValidation();
        /*HashSet<GameAction> gameActionSet = fetchAction(keywordsFromCommand, actions);
        if(gameActionSet.size() == 1) {
            List<GameAction> list = new ArrayList<>(gameActionSet);
            return list.get(0).getNarration();
        }*/
        return "";
        //throw new StagExceptions("Warning! Unexpect state is met");
    }

    void setIsBuiltinCommand() { isBuiltInCommand = true; }

    private void checkCommandValidation() throws StagExceptions {
        int builtInCommandNum = 0;
        for(String builtInCommand : builtInCommands) {
            if(command.contains(builtInCommand)) builtInCommandNum++;     
        }
        if(!keywordsFromCommand.isEmpty()) {
            for(String builtInCommand : builtInCommands) {
                if(command.contains(builtInCommand)) throw new StagExceptions(
                    "You are mixing built-in actions with sandard actions");      
            }  
        }
        else setIsBuiltinCommand();    
        if(builtInCommandNum != 1) throw new StagExceptions("Please do one action for each command");
    }

    private HashSet<GameAction> fetchAction(ArrayList<String> keywordsFromCommand, HashMap<String, HashSet<GameAction>> actions) {
        HashSet<GameAction> initalAction = new HashSet<>(actions.get(keywordsFromCommand.get(0).toLowerCase()));
        for(String keyword : keywordsFromCommand) {
            initalAction.retainAll(actions.get(keyword.toLowerCase()));
        }
        return initalAction;
    }

    private void dismantleCommand() {
        for(String keyword : keywords) {
            if(command.contains(keyword)) keywordsFromCommand.add(keyword);
        }
    }

    private void setCommand(String command) {
        int colonIndex = command.indexOf(':');
        if (colonIndex != -1)  this.command = command.substring(colonIndex + 1);
        command = command.toLowerCase();
    }

    private void initialPlayerLocation(Map<String, GameEntity> entities, Player currentPlayer) {
        LocationEntity locationEntity = finaFirstLocation(entities);
        if(locationEntity != null) {
            currentPlayer.setLocation(locationEntity.getName());
            locationEntity.addPlayers(currentPlayer);
        }
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
