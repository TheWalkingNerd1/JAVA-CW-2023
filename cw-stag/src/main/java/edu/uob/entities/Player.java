package edu.uob.entities;

public class Player {
    final private String name;

    private String location;

    public Player(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setLocation(String location) { this.location = location; }
}
