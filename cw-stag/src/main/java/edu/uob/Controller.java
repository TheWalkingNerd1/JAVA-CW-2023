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
        setCurrentPlayer();
        //deduct the command
        setCommand();
    }

    public String result() throws StagExceptions {
        //Ignore empty command
        if (command == null || command.trim().isEmpty()) return ""; 
        //extract keyWords
        dismantleCommand();
        checkCommandValidation();
        if(isBuiltInCommand)  return handleBuiltInCommand();
        /*HashSet<GameAction> gameActionSet = fetchAction(keywordsFromCommand, actions);
        if(gameActionSet.size() == 1) {
            List<GameAction> list = new ArrayList<>(gameActionSet);
            return list.get(0).getNarration();
        }*/
        return "";
        //throw new StagExceptions("Warning! Unexpect state is met");
    }

    private String handleBuiltInCommand() throws StagExceptions{
        if(command.contains("look")) return handleLookCommand();
        if(command.contains("inv")) return handleInventoryCommand();
        throw new StagExceptions("Unexpected state when processing built-in commands");
    }

    private String handleInventoryCommand() {
        if(currentPlayer.getArtefacts().isEmpty()) return "You are not currently carrying any artefacts!";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("You are holding the artefacts : ");
        for(String string : currentPlayer.getArtefacts()) {
            stringBuilder.append(string).append(" ");
        }
        return stringBuilder.toString();
    }

    private String handleLookCommand() throws StagExceptions {
        String location = currentPlayer.getLocation().toLowerCase();
        if(entities.get(location) instanceof LocationEntity locationEntity) return constructLookResult(locationEntity);
        throw new StagExceptions("Something wrong for look command");
    }

    private String constructLookResult(LocationEntity locationEntity) throws StagExceptions  {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("You are currently in ").append(locationEntity.getName()).append(" : ").append(locationEntity.getDescription()).append("\n");
        lookPlaceToGo(stringBuilder, locationEntity);
        lookArtefacts(stringBuilder, locationEntity);
        lookCharacters(stringBuilder, locationEntity);
        lookFurniture(stringBuilder, locationEntity);
        lookPlayer(stringBuilder, locationEntity);
        return stringBuilder.toString();
    }

    private void lookPlaceToGo(StringBuilder stringBuilder, LocationEntity locationEntity) throws StagExceptions {
        if(!locationEntity.getConnectTo().isEmpty()) {
            stringBuilder.append("You can goto the following places: ");
            for(String string : locationEntity.getConnectTo()) {
                stringBuilder.append(string).append(" ");
            }
            stringBuilder.append("\n");
        }
    }

    private void lookPlayer(StringBuilder stringBuilder, LocationEntity locationEntity) throws StagExceptions {
        if(locationEntity.getPlayers().isEmpty()) throw new StagExceptions("There should be at least one player in this room");
        stringBuilder.append("The room has following players: \n");
        for(Player player : locationEntity.getPlayers()) {
            stringBuilder.append(player.getName()).append(" ");
        }
        stringBuilder.append("\n");
    }

    private void lookFurniture(StringBuilder stringBuilder, LocationEntity locationEntity) {
        if(!locationEntity.getFurniture().isEmpty()) {
            stringBuilder.append("The room has following furniture: \n");
            for(FurnitureEntity furnitureEntity : locationEntity.getFurniture()) {
                stringBuilder.append(furnitureEntity.getName()).append(" : ").append(furnitureEntity.getDescription()).append("\n");
            }
            stringBuilder.append("\n");
        }
    }

    private void lookCharacters(StringBuilder stringBuilder, LocationEntity locationEntity) {
        if(!locationEntity.getCharacters().isEmpty()) {
            stringBuilder.append("The room has following characters: \n");
            for(CharactersEntity charactersEntity : locationEntity.getCharacters()) {
                stringBuilder.append(charactersEntity.getName()).append(" : ").append(charactersEntity.getDescription()).append("\n");
            }
            stringBuilder.append("\n");
        }
    }

    private void lookArtefacts(StringBuilder stringBuilder, LocationEntity locationEntity) {
        if(!locationEntity.getArtefacts().isEmpty()) {
            stringBuilder.append("The room has following artefacts: \n");
            for(ArtefactsEntity artefactsEntity : locationEntity.getArtefacts()) {
                stringBuilder.append(artefactsEntity.getName()).append(" : ").append(artefactsEntity.getDescription()).append("\n");
            }
            stringBuilder.append("\n");
        }
    }

    private void setIsBuiltinCommand() { isBuiltInCommand = true; }

    private void checkCommandValidation() throws StagExceptions {
        //Find out the numbers of the builtIn command
        int builtInCommandNum = 0;
        for(String builtInCommand : builtInCommands) {
            if(command.contains(builtInCommand)) builtInCommandNum++;     
        }
        if(!keywordsFromCommand.isEmpty() && builtInCommandNum > 0)
            throw new StagExceptions("You are mixing built-in actions with standard actions");
        //Only One command should be processed at each time
        if(builtInCommandNum > 1) throw new StagExceptions("Please do one action for each command");
        if(builtInCommandNum == 1) setIsBuiltinCommand();
    }

    private HashSet<GameAction> fetchAction(ArrayList<String> keywordsFromCommand, HashMap<String, HashSet<GameAction>> actions) {
        HashSet<GameAction> initialAction = new HashSet<>(actions.get(keywordsFromCommand.get(0).toLowerCase()));
        for(String keyword : keywordsFromCommand) {
            initialAction.retainAll(actions.get(keyword.toLowerCase()));
        }
        return initialAction;
    }

    private void dismantleCommand() {
        for(String keyword : keywords) {
            if(command.contains(keyword)) keywordsFromCommand.add(keyword);
        }
    }

    private void setCommand() {
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

    private void setCurrentPlayer() {
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
