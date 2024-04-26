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
    private final ArrayList<String> artefactNames = new ArrayList<>();
    private final ArrayList<String> locationNames = new ArrayList<>();
    private final Map<String, Player> players;
    private Player currentPlayer;

    public Controller(String command, Map<String, GameEntity> entities, HashMap<String,
                       HashSet<GameAction>> actions, Map<String, Player> players) {
        this.command = command;
        this.entities = entities;
        this.actions = actions;
        this.players = players;
        //Set artefacts for command check
        constructArtefactNames();
        //Set locations for command check
        constructLocationNames();
        //SetKeyWords
        constructKeywords();
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
        String builtInCommandResult = handleBuiltInCommand();
        if(builtInCommandResult != null) return builtInCommandResult;
        /*HashSet<GameAction> gameActionSet = fetchAction(keywordsFromCommand, actions);
        if(gameActionSet.size() == 1) {
            List<GameAction> list = new ArrayList<>(gameActionSet);
            return list.get(0).getNarration();
        }*/
        return "";
        //throw new StagExceptions("Warning! Unexpect state is met");
    }

    private void constructArtefactNames() {
        for(GameEntity gameEntity : entities.values()) {
            if(gameEntity instanceof ArtefactsEntity artefactsEntity) artefactNames.add(artefactsEntity.getName().toLowerCase());
        }
    }

    private void constructLocationNames() {
        for(GameEntity gameEntity : entities.values()) {
            if(gameEntity instanceof LocationEntity locationEntity) locationNames.add(locationEntity.getName().toLowerCase());
        }
    }

    private String handleBuiltInCommand() throws StagExceptions {
        if(command.contains("look")) return handleLookCommand();
        if(command.contains("inv")) return handleInventoryCommand();
        if(command.contains("get")) return handleGetCommand();
        if(command.contains("drop")) return handleDropCommand();
        if(command.contains("goto")) return handleGotoCommand();
        return null;
    }

    private String handleGotoCommand() throws StagExceptions {
        String location = checkLocationBuiltinValidation();
        if(!isPathAvailable(location)) throw new StagExceptions("You have no access to this place");
        if(entities.get(location) instanceof LocationEntity locationEntity) locationEntity.addPlayers(currentPlayer.getName(), currentPlayer);
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) locationEntity.removePlayer(currentPlayer.getName());
        currentPlayer.setLocation(location);
        return "You have moved to " + location;
    }

    private boolean isPathAvailable(String location) {
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity)
            return locationEntity.getConnectTo().contains(location.toLowerCase());
        return false;
    }

    private String handleDropCommand() throws StagExceptions {
        String artefact = checkArtefactBuiltinValidation();
        if(!playerHasArtefact(artefact)) throw new StagExceptions("Player doesn't have this artefact");
        ArtefactsEntity artefactsEntity = currentPlayer.getArtefacts().get(artefact.toLowerCase());
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) locationEntity.addArtefact(artefact.toLowerCase(), artefactsEntity);
        currentPlayer.removeArtefact(artefact.toLowerCase());
        return "You have successfully dropped the " + artefact;
    }

    private String handleGetCommand() throws StagExceptions {
        String artefact = checkArtefactBuiltinValidation();
        if(!locationHasArtefact(artefact)) throw new StagExceptions("This location doesn't contain the artefact");
        if(entities.get(artefact) instanceof ArtefactsEntity artefactsEntity) currentPlayer.addArtefact(artefact, artefactsEntity);
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) locationEntity.removeArtefact(artefact.toLowerCase());
        return "You have successfully got the " + artefact;
    }

    private boolean playerHasArtefact(String artefact) throws StagExceptions {
        if(currentPlayer.getArtefacts().containsKey(artefact.toLowerCase())) return true;
        return false;
    }

    private boolean locationHasArtefact(String artefact) {
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) 
            if(locationEntity.getArtefacts().containsKey(artefact.toLowerCase())) return true;
        return false;
    }

    private String handleInventoryCommand() throws StagExceptions {
        checkSingleBuiltinValidation();
        if(currentPlayer.getArtefacts().isEmpty()) return "You are not currently carrying any artefacts!";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("You are holding the artefacts : ");
        for(ArtefactsEntity artefactsEntity : currentPlayer.getArtefacts().values()) {
            stringBuilder.append(artefactsEntity.getName()).append(" ");
        }
        return stringBuilder.toString();
    }

    private String handleLookCommand() throws StagExceptions {
        checkSingleBuiltinValidation();
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
        for(Player player : locationEntity.getPlayers().values()) {
            stringBuilder.append(player.getName()).append(" ");
        }
        stringBuilder.append("\n");
    }

    private void lookFurniture(StringBuilder stringBuilder, LocationEntity locationEntity) {
        if(!locationEntity.getFurniture().isEmpty()) {
            stringBuilder.append("The room has following furniture: \n");
            for(FurnitureEntity furnitureEntity : locationEntity.getFurniture().values()) {
                stringBuilder.append(furnitureEntity.getName()).append(" : ").append(furnitureEntity.getDescription()).append("\n");
            }
            stringBuilder.append("\n");
        }
    }

    private void lookCharacters(StringBuilder stringBuilder, LocationEntity locationEntity) {
        if(!locationEntity.getCharacters().isEmpty()) {
            stringBuilder.append("The room has following characters: \n");
            for(CharactersEntity charactersEntity : locationEntity.getCharacters().values()) {
                stringBuilder.append(charactersEntity.getName()).append(" : ").append(charactersEntity.getDescription()).append("\n");
            }
            stringBuilder.append("\n");
        }
    }

    private void lookArtefacts(StringBuilder stringBuilder, LocationEntity locationEntity) {
        if(!locationEntity.getArtefacts().isEmpty()) {
            stringBuilder.append("The room has following artefacts: \n");
            for(ArtefactsEntity artefactsEntity : locationEntity.getArtefacts().values()) {
                stringBuilder.append(artefactsEntity.getName()).append(" : ").append(artefactsEntity.getDescription()).append("\n");
            }
            stringBuilder.append("\n");
        }
    }

    private String checkLocationBuiltinValidation() throws StagExceptions {
        if(keywordsFromCommand.size() > 1) throw new StagExceptions("You are mixing built-in actions with standard actions");
        String location = null;
        //Find out the numbers of the builtIn command
        int builtInCommandNum = 0;
        for(String builtInCommand : builtInCommands) {
            if (command.contains(builtInCommand)) builtInCommandNum++;
        }
        //Only One command should be processed at each time
        if(builtInCommandNum > 1) throw new StagExceptions("Please do one action for each command");
        //Find the artefact number
        int locationNum = 0;
        for(String locationName : locationNames) {
            if(command.contains(locationName)) {
                locationNum++;
                location = locationName;
            }
        }
        if(locationNum != 1) throw new StagExceptions("Please specify one and only one location for goto command");
        return location;
    }

    private String checkArtefactBuiltinValidation() throws StagExceptions {
        if(keywordsFromCommand.size() > 1) throw new StagExceptions("You are mixing built-in actions with standard actions");
        String artefact = null;
        //Find out the numbers of the builtIn command
        int builtInCommandNum = 0;
        for(String builtInCommand : builtInCommands) {
            if (command.contains(builtInCommand)) builtInCommandNum++;
        }
        //Only One command should be processed at each time
        if(builtInCommandNum > 1) throw new StagExceptions("Please do one action for each command");
        //Find the artefact number
        int artefactNum = 0;
        for(String artefactName : artefactNames) {
            if(command.contains(artefactName)) {
                artefactNum++;
                artefact = artefactName;
            }
        }
        if(artefactNum != 1) throw new StagExceptions("Please specify one and only one artefact for get and pick command");
        return artefact;
    }
    private void checkSingleBuiltinValidation() throws StagExceptions {
        if(!keywordsFromCommand.isEmpty()) throw new StagExceptions("You are mixing built-in actions with standard actions");
        //Find out the numbers of the builtIn command
        int builtInCommandNum = 0;
        for(String builtInCommand : builtInCommands) {
            if (command.contains(builtInCommand)) builtInCommandNum++;
        }
        //Only One command should be processed at each time
        if(builtInCommandNum > 1) throw new StagExceptions("Please do one action for each command");
    }

    private HashSet<GameAction> fetchAction() {
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
            locationEntity.addPlayers(currentPlayer.getName(), currentPlayer);
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
            players.put(name, player);
            currentPlayer = player;
            //Set the player to firstLocation
            initialPlayerLocation(entities,currentPlayer);
        }
    }

    private void constructKeywords() {
        for(Map.Entry<String, HashSet<GameAction>> entry : actions.entrySet()) { keywords.add(entry.getKey().toLowerCase()); }
    }

    private boolean checkPlayerExistence(String name) {
        if(players.containsKey(name)) {
            currentPlayer = players.get(name);
            return true;
        }
        return false;
    }
}
