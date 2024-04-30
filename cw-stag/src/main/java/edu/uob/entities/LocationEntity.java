package edu.uob.entities;

import com.sun.source.tree.ReturnTree;
import edu.uob.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationEntity extends GameEntity {
    private final ArrayList<String> connectTo = new ArrayList<>();
    private final ArrayList<String> connectFrom = new ArrayList<>();
    private final Map<String, Player> players = new HashMap<>();
    private final Map<String, ArtefactsEntity> artefacts = new HashMap<>();
    private final Map<String, FurnitureEntity> furniture = new HashMap<>();
    private final Map<String, CharactersEntity> characters = new HashMap<>();
    
    public boolean isFirstLocation = false;

    public LocationEntity (String name, String description) { super(name, description); }

    public void addConnectTo(String locationName) { connectTo.add(locationName); }

    public void addConnectFrom(String locationName) { connectFrom.add(locationName); }

    public void setFirstLocation() { isFirstLocation = true; }

    public void addPlayers(String key, Player player) { players.put(key, player); }

    public void addArtefact(String key, ArtefactsEntity artefactsEntity) { artefacts.put(key, artefactsEntity); }

    public void addFurniture(String key, FurnitureEntity furnitureEntity) { furniture.put(key, furnitureEntity); }

    public void addCharacter(String key, CharactersEntity charactersEntity) { characters.put(key, charactersEntity); }

    public Map<String, ArtefactsEntity> getArtefacts() { return artefacts; }

    public Map<String, CharactersEntity> getCharacters() { return characters; }

    public Map<String, FurnitureEntity> getFurniture() { return furniture; }
   
    public Map<String, Player> getPlayers() { return players; }

    public ArrayList<String> getConnectTo() { return connectTo; }

    public void removeArtefact(String key) { artefacts.remove(key); }

    public void removeFurniture(String key) { furniture.remove(key); }

    public void removeCharacters(String key) { characters.remove(key); }

    public void removePlayer(String key) { players.remove(key); }
}
