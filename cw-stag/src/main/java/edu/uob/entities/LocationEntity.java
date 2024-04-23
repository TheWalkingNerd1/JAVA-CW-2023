package edu.uob.entities;

import edu.uob.*;

import java.util.ArrayList;

public class LocationEntity extends GameEntity {
    public ArrayList<String> connectTo = new ArrayList<>();
    public ArrayList<String> connectFrom = new ArrayList<>();

    public LocationEntity (String name, String description) { super(name, description); }

    public void addConnectTo(String locationName) { connectTo.add(locationName); }

    public void addConnectFrom(String locationName) { connectFrom.add(locationName); }

}
