package edu.uob.entities;

import edu.uob.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationEntity extends GameEntity {
    private final ArrayList<String> connectTo = new ArrayList<>();
    private final Map<String, Player> players = new HashMap<>();
    private final Map<String, ArtefactsEntity> artefacts = new HashMap<>();
    private final Map<String, StationaryEntity> properties = new HashMap<>();
    
    public boolean isFirstLocation = false;

    public LocationEntity (String name, String description) { super(name, description); }

    public void addConnectTo(String locationName) { connectTo.add(locationName); }

    public void setFirstLocation() { isFirstLocation = true; }

    public void addPlayers(String key, Player player) { players.put(key, player); }

    public void addArtefact(String key, ArtefactsEntity artefactsEntity) { artefacts.put(key, artefactsEntity); }

    public void addProperty(String key, StationaryEntity furnitureEntity) { properties.put(key, furnitureEntity); }

    public Map<String, ArtefactsEntity> getArtefacts() { return artefacts; }

    public Map<String, StationaryEntity> getProperties() { return properties; }
   
    public Map<String, Player> getPlayers() { return players; }

    public ArrayList<String> getConnectTo() { return connectTo; }

    public void removeArtefact(String key) { artefacts.remove(key); }

    public void removeProperty(String key) { properties.remove(key); }

    public void removePlayer(String key) { players.remove(key); }
}
