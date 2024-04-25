package edu.uob.entities;

import java.util.ArrayList;

public class Player {
    final private String name;
    private String location;
    private final ArrayList<String> artefacts = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setLocation(String location) { this.location = location; }

    public String getLocation() { return location; }

    public ArrayList<String> getArtefacts() { return artefacts; }
}
