package edu.uob.entities;

import edu.uob.*;

public class FurnitureEntity extends GameEntity {
    private String location;

    public FurnitureEntity(String name, String description, String location) {
        super(name, description);
        this.location = location;
    }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }
}
