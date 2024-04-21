package edu.uob.entities;

import edu.uob.*;

import java.util.ArrayList;

public class LocationEntity extends GameEntity {
    public ArrayList<String> connections = new ArrayList<>();

    public LocationEntity (String name, String description) { super(name, description); }

    public void addConnection(String toName) { connections.add(toName);}
}
