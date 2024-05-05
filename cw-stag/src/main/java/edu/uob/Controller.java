package edu.uob;

import edu.uob.entities.*;
import edu.uob.utilities.StagExceptions;

import java.util.*;

public class Controller {
    private final String[] builtInCommands = {"inventory", "inv", "get", "drop", "goto", "look", "health"};
    private final Map<String, GameEntity> entities;
    private final HashMap<String, HashSet<GameAction>> actions;
    private String command;
    private final ArrayList<String> keywords = new ArrayList<>();
    private final ArrayList<String> keywordsFromCommand = new ArrayList<>();
    private final ArrayList<String> subjects = new ArrayList<>();
    private final ArrayList<String> triggers = new ArrayList<>();
    private final Map<String, Player> players;
    private Player currentPlayer;
    private String firstLocation;
    private boolean isDead = false;

    public Controller(String command, Map<String, GameEntity> entities, HashMap<String,
                       HashSet<GameAction>> actions, Map<String, Player> players) throws StagExceptions {
        this.command = command;
        this.entities = entities;
        this.actions = actions;
        this.players = players;
        //Set the keywords for actions
        constructActionKeywords();
        //SetKeyWords
        constructKeywords();
        //Set the first Location
        setFirstLocation();
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
        checkActionCommand();
        HashSet<GameAction> gameActionSet = fetchAction();
        if(gameActionSet.size() > 1) throw new StagExceptions("The action is ambiguous");
        else if(gameActionSet.isEmpty()) throw new StagExceptions("You provide too many actions");
        else{
            List<GameAction> list = new ArrayList<>(gameActionSet);
            GameAction gameAction = list.get(0);
            checkSubject(gameAction);
            checkConsumeAndProduceSubjects(gameAction);
            produceSubjects(gameAction);
            consumeSubjects(gameAction);
            if(entities.get("storeroom") instanceof LocationEntity locationEntity) {
                System.out.println(locationEntity.getArtefacts().keySet());
                System.out.println(locationEntity.getProperties().keySet());
            }
            if(!isDead) return gameAction.getNarration();
            else return gameAction.getNarration() + "you died and lost all of your items, you must return to the start of the game";
        }
    }

    private void checkConsumeAndProduceSubjects(GameAction gameAction) throws StagExceptions {
        for(String string : gameAction.getProduced()) {
            if(entities.get(string) instanceof ArtefactsEntity artefactsEntity) {
                if (artefactsEntity.getLocation().isEmpty() && !currentPlayer.getArtefacts().containsValue(artefactsEntity))
                    throw new StagExceptions("An artefact to be produced is currently hold by another player");
            }
        }
        for(String string : gameAction.getConsumed()) {
            if(entities.get(string) instanceof ArtefactsEntity artefactsEntity) {
                if (artefactsEntity.getLocation().isEmpty() && !currentPlayer.getArtefacts().containsValue(artefactsEntity))
                    throw new StagExceptions("An artefact to be consumed is currently hold by another player");
            }
            if(entities.get(string) instanceof LocationEntity) {
                if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) {
                    if(!locationEntity.getConnectTo().contains(string))
                        throw new StagExceptions("You can't consume unreached location");
                }
            }
        }
    }

    private void setFirstLocation() {
        for(GameEntity entity : entities.values()) {
            if(entity instanceof LocationEntity locationEntity) {
                if(locationEntity.isFirstLocation) {
                    firstLocation = locationEntity.getName();
                    return;
                }
            }
        }
    }

    private void produceSubjects(GameAction gameAction) {
        for(String string : gameAction.getProduced()) {
            if(entities.get(string) instanceof ArtefactsEntity artefactsEntity) {
                produceArtefact(artefactsEntity);
            }
            if(entities.get(string) instanceof StationaryEntity stationaryEntity) {
                String location = stationaryEntity.getLocation();
                if(entities.get(location) instanceof LocationEntity locationEntity)
                    locationEntity.removeProperty(stationaryEntity.getName());
                if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity)
                    locationEntity.addProperty(stationaryEntity.getName(), stationaryEntity);
                stationaryEntity.setLocation(currentPlayer.getLocation());
            }
            if(entities.get(string) instanceof LocationEntity) {
                if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity location)
                    if(!location.getConnectTo().contains(string) && !currentPlayer.getLocation().equals(string)) location.getConnectTo().add(string);
            }
            if(string.equalsIgnoreCase("health")) increasePlayerHealth();
        }
    }

    private void increasePlayerHealth() {
        if(currentPlayer.health < 3) currentPlayer.health++;
    }

    private void reducePlayerHealth() {
        if(currentPlayer.health > 0) currentPlayer.health--;
        if(currentPlayer.health == 0) resetPlayer();
    }

    private void resetPlayer() {
        for(ArtefactsEntity artefactsEntity : currentPlayer.getArtefacts().values()) {
            if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) {
                locationEntity.addArtefact(artefactsEntity.getName(), artefactsEntity);
            }
            artefactsEntity.setLocation(currentPlayer.getLocation());
        }
        currentPlayer.getArtefacts().clear();
        if(entities.get(firstLocation) instanceof LocationEntity locationEntity) locationEntity.addPlayers(currentPlayer.getName(), currentPlayer);
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) locationEntity.removePlayer(currentPlayer.getName());
        currentPlayer.setLocation(firstLocation);
        currentPlayer.health = 3;
        isDead = true;
    }

    private void produceArtefact(ArtefactsEntity artefactsEntity) {
        if(artefactsEntity.getLocation().isEmpty()) currentPlayer.removeArtefact(artefactsEntity.getName());
        else {
            String location = artefactsEntity.getLocation();
            if(entities.get(location) instanceof LocationEntity locationEntity)
                locationEntity.removeArtefact(artefactsEntity.getName());
        }
        //Add the item to the current Room
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) {
            locationEntity.addArtefact(artefactsEntity.getName(), artefactsEntity);
            artefactsEntity.setLocation(currentPlayer.getLocation());
        }
    }

    private void consumeSubjects(GameAction gameAction) {
        for(String string : gameAction.getConsumed()) {
            if(entities.get(string) instanceof ArtefactsEntity artefactsEntity) {
                consumeArtefact(artefactsEntity);
            }
            if(entities.get(string) instanceof StationaryEntity stationaryEntity) {
                String location = stationaryEntity.getLocation();
                if(entities.get(location) instanceof LocationEntity locationEntity) 
                    locationEntity.removeProperty(stationaryEntity.getName());
                if(entities.get("storeroom") instanceof LocationEntity locationEntity)
                    locationEntity.addProperty(stationaryEntity.getName(), stationaryEntity);
                stationaryEntity.setLocation("storeroom");
            }
            if(entities.get(string) instanceof LocationEntity) {
                if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity location) {
                    location.getConnectTo().remove(string);
                }
            }
            if(string.equalsIgnoreCase("health")) reducePlayerHealth();
        }
    }

    private void consumeArtefact(ArtefactsEntity artefactsEntity) {
        if(artefactsEntity.getLocation().isEmpty()) currentPlayer.removeArtefact(artefactsEntity.getName());
        else {
            String location = artefactsEntity.getLocation();
            if(entities.get(location) instanceof LocationEntity locationEntity)
                locationEntity.removeArtefact(artefactsEntity.getName());
        }
        //Add the item to storeroom
        if(entities.get("storeroom") instanceof LocationEntity locationEntity) {
            locationEntity.addArtefact(artefactsEntity.getName(), artefactsEntity);
            artefactsEntity.setLocation("storeroom");
        }
    }

    private void checkSubject(GameAction gameAction) throws StagExceptions {
        for(String string : gameAction.getSubjects()) {
            if(!checkPlayerInventory(string) && !checkLocationSubjects(string))
                throw new StagExceptions("There's no enough subjects for the action!");
        }
    }

    private boolean checkLocationSubjects(String string) {
        if(string.equalsIgnoreCase(currentPlayer.getLocation())) return true;
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity)
        {
            if(locationEntity.getConnectTo().contains(string)) return true;
            if(locationEntity.getArtefacts().containsKey(string)) return true;
            return locationEntity.getProperties().containsKey(string);
        }
        return false;
    }

    private boolean checkPlayerInventory(String string) {
        return currentPlayer.getArtefacts().containsKey(string);
    }

    private void checkActionCommand() throws StagExceptions {
        int triggerNum = 0, subjectNum = 0;
        for(String string : triggers) {
            if (containsKeywords(string)) {
                int lastIndex = 0;
                int count = 0;

                while (lastIndex != -1) {
                    lastIndex = command.indexOf(string, lastIndex);
                    if (lastIndex != -1) {
                        count++;
                        lastIndex += string.length();
                    }
                }
                if(count > 1) throw new StagExceptions("This is a invalid command");
                triggerNum++;
            }
        }
        for(String string : subjects) {
            if (containsKeywords(string)) {
                int lastIndex = 0;
                int count = 0;

                while (lastIndex != -1) {
                    lastIndex = command.indexOf(string, lastIndex);
                    if (lastIndex != -1) {
                        count++;
                        lastIndex += string.length();
                    }
                }
                if(count > 1) throw new StagExceptions("This is a invalid command");
                subjectNum++;
            }
        }
        if(triggerNum < 1 || subjectNum < 1) throw new StagExceptions("Please specify at least one trigger and one subject");
    }

    private void constructActionKeywords() {
        for(String string : actions.keySet()) {
            if(entities.containsKey(string)) subjects.add(string);
            else triggers.add(string);
        }
    }

    private boolean containsKeywords(String string) {
        if (command.equalsIgnoreCase(string)) return true;
        if (command.startsWith(string + " ")) return true;
        if (command.endsWith(" " + string)) return true;
        return command.contains(" " + string + " ");
    }

    private String handleBuiltInCommand() throws StagExceptions {
        if(containsKeywords("health")) return handleHealthCommand();
        if(containsKeywords("look")) return handleLookCommand();
        if(containsKeywords("inv")) return handleInventoryCommand();
        if(containsKeywords("get")) return handleGetCommand();
        if(containsKeywords("drop")) return handleDropCommand();
        if(containsKeywords("goto")) return handleGotoCommand();
        return null;
    }

    private String handleHealthCommand() throws StagExceptions {
        checkSingleBuiltinValidation();
        return "Health : " + String.valueOf(currentPlayer.health);
    }

    private String handleGotoCommand() throws StagExceptions {
        String location = checkComplexBuiltinValidation();
        if(!isPathAvailable(location)) throw new StagExceptions("You have no access to this place");
        if(entities.get(location) instanceof LocationEntity locationEntity) locationEntity.addPlayers(currentPlayer.getName(), currentPlayer);
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) locationEntity.removePlayer(currentPlayer.getName());
        currentPlayer.setLocation(location);
        return "You have moved to " + location;
    }

    private boolean isPathAvailable(String location) {
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity)
            return locationEntity.getConnectTo().contains(location);
        return false;
    }

    private String handleDropCommand() throws StagExceptions {
        String artefact = checkComplexBuiltinValidation();
        if(!playerHasArtefact(artefact)) throw new StagExceptions("Player doesn't have this artefact");
        ArtefactsEntity artefactsEntity = currentPlayer.getArtefacts().get(artefact);
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) {
            locationEntity.addArtefact(artefact, artefactsEntity);
            artefactsEntity.setLocation(currentPlayer.getLocation());
        }
        currentPlayer.removeArtefact(artefact);
        return "You have successfully dropped the " + artefact;
    }

    private String handleGetCommand() throws StagExceptions {
        String artefact = checkComplexBuiltinValidation();
        if(!locationHasArtefact(artefact)) throw new StagExceptions("This location doesn't contain the artefact");
        if(entities.get(artefact) instanceof ArtefactsEntity artefactsEntity) {
            artefactsEntity.setLocation("");
            currentPlayer.addArtefact(artefact, artefactsEntity);
        }
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity) locationEntity.removeArtefact(artefact);
        return "You have successfully got the " + artefact;
    }

    private boolean playerHasArtefact(String artefact) {
        return currentPlayer.getArtefacts().containsKey(artefact);
    }

    private boolean locationHasArtefact(String artefact) {
        if(entities.get(currentPlayer.getLocation()) instanceof LocationEntity locationEntity)
            return locationEntity.getArtefacts().containsKey(artefact);
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
        String location = currentPlayer.getLocation();
        if(entities.get(location) instanceof LocationEntity locationEntity) return constructLookResult(locationEntity);
        throw new StagExceptions("Something wrong for look command");
    }

    private String constructLookResult(LocationEntity locationEntity) throws StagExceptions  {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("You are currently in ").append(locationEntity.getName()).append(" : ").append(locationEntity.getDescription()).append("\n");
        lookPlaceToGo(stringBuilder, locationEntity);
        lookArtefacts(stringBuilder, locationEntity);
        lookEntities(stringBuilder, locationEntity);
        lookPlayer(stringBuilder, locationEntity);
        return stringBuilder.toString();
    }

    private void lookPlaceToGo(StringBuilder stringBuilder, LocationEntity locationEntity) {
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
    }

    private void lookEntities(StringBuilder stringBuilder, LocationEntity locationEntity) {
        if(!locationEntity.getProperties().isEmpty()) {
            stringBuilder.append("The room has following furniture and characters: \n");
            for(StationaryEntity stationaryEntity : locationEntity.getProperties().values()) {
                stringBuilder.append(stationaryEntity.getName()).append(" : ").append(stationaryEntity.getDescription()).append("\n");
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

    private String checkComplexBuiltinValidation() throws StagExceptions {
        if(keywordsFromCommand.size() > 1) throw new StagExceptions("You are mixing built-in actions with standard actions");
        String target = null;
        //Find out the numbers of the builtIn command
        int builtInCommandNum = 0;
        for(String builtInCommand : builtInCommands) {
            if (containsKeywords(builtInCommand)) builtInCommandNum++;
        }
        //Only One command should be processed at each time
        if(builtInCommandNum > 1) throw new StagExceptions("Please do one action for each command");
        //Find the artefact number
        int targetNum = 0;
        for(String key : entities.keySet()) {
            if(containsKeywords(key)) {
                targetNum++;
                target = key;
            }
        }
        if(targetNum != 1) throw new StagExceptions("Please specify one and only one entity for built-in action");
        return target;
    }

    private void checkSingleBuiltinValidation() throws StagExceptions {
        if(!keywordsFromCommand.isEmpty()) throw new StagExceptions("You are mixing built-in actions with standard actions");
        //Find out the numbers of the builtIn command
        int builtInCommandNum = 0;
        for(String builtInCommand : builtInCommands) {
            if (containsKeywords(builtInCommand)) builtInCommandNum++;
        }
        //Only One command should be processed at each time
        if(builtInCommandNum > 1) throw new StagExceptions("Please do one action for each command");
    }

    private HashSet<GameAction> fetchAction() {
        HashSet<GameAction> initialAction = new HashSet<>(actions.get(keywordsFromCommand.get(0)));
        for(String keyword : keywordsFromCommand) {
            initialAction.retainAll(actions.get(keyword));
        }
        return initialAction;
    }

    private void dismantleCommand() {
        for(String keyword : keywords) {
            if(containsKeywords(keyword)) keywordsFromCommand.add(keyword);
        }
    }

    private void setCommand() {
        int colonIndex = command.indexOf(':');
        if (colonIndex != -1)  this.command = command.substring(colonIndex + 1);
        command = command.toLowerCase();
    }

    private void initialPlayerLocation(Map<String, GameEntity> entities, Player currentPlayer) {
        LocationEntity locationEntity = findFirstLocation(entities);
        if(locationEntity != null) {
            currentPlayer.setLocation(locationEntity.getName());
            locationEntity.addPlayers(currentPlayer.getName(), currentPlayer);
        }
    }

    private LocationEntity findFirstLocation(Map<String, GameEntity> entities) {
        for(GameEntity gameEntity : entities.values()) {
            if(gameEntity instanceof LocationEntity locationEntity)
                if (locationEntity.isFirstLocation) return locationEntity;
        }
        return null;
    }

    private void setCurrentPlayer() throws StagExceptions {
        //Get the player's name
        String[] parts = command.split(":");
        if(parts.length <= 1) throw new StagExceptions("Please provide a valid command");
        String name = parts[0];
        checkName(name);
        if(!checkPlayerExistence(name)) {
            Player player = new Player(name);
            players.put(name, player);
            currentPlayer = player;
            //Set the player to firstLocation
            initialPlayerLocation(entities,currentPlayer);
        }
    }

    private void checkName(String name) throws StagExceptions {
        String regex = "^[A-Za-z\\s'-]+$";
        if(!name.matches(regex)) throw new StagExceptions("Please use a valid user name");
    }

    private void constructKeywords() {
        keywords.addAll(actions.keySet());
    }

    private boolean checkPlayerExistence(String name) {
        if(players.containsKey(name)) {
            currentPlayer = players.get(name);
            return true;
        }
        return false;
    }
}
