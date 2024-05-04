package edu.uob.entities;

import edu.uob.*;

public class StationaryEntity extends GameEntity {
    private String location;

    public StationaryEntity(String name, String description, String location) {
        super(name, description);
        this.location = location;
    }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }
}
