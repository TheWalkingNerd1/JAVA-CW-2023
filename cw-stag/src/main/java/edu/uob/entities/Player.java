package edu.uob.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Player {
    final private String name;
    private String location;
    public int health = 3;
    private final Map<String, ArtefactsEntity> artefacts = new HashMap<>();

    public Player(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setLocation(String location) { this.location = location; }

    public String getLocation() { return location; }

    public Map<String, ArtefactsEntity> getArtefacts() { return artefacts; }

    public void addArtefact(String key, ArtefactsEntity artefactsEntity) { artefacts.put(key, artefactsEntity); }

    public void removeArtefact(String key) { artefacts.remove(key); }
}
