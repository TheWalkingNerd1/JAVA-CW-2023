package edu.uob.entities;

import edu.uob.*;

public class CharactersEntity extends GameEntity {
    private String location;

    public CharactersEntity(String name, String description, String location) {
        super(name, description);
        this.location = location;
    }

    public String getLocation() { return location; }
}
