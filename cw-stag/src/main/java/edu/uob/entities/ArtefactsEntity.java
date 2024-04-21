package edu.uob.entities;

import edu.uob.*;

public class ArtefactsEntity extends GameEntity {
    private String location;

    public ArtefactsEntity(String name, String description, String location) {
        super(name, description);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }
}