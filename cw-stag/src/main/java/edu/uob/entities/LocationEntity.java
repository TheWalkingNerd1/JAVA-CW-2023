package edu.uob.entities;

import com.sun.source.tree.ReturnTree;
import edu.uob.*;

import java.util.ArrayList;

public class LocationEntity extends GameEntity {
    private final ArrayList<String> connectTo = new ArrayList<>();
    private final ArrayList<String> connectFrom = new ArrayList<>();
    private final ArrayList<Player> players = new ArrayList<>();
    private final ArrayList<ArtefactsEntity> artefacts = new ArrayList<>();
    private final ArrayList<FurnitureEntity> furniture = new ArrayList<>();
    private final ArrayList<CharactersEntity> characters = new ArrayList<>();

    public boolean isFirstLocation = false;

    public LocationEntity (String name, String description) { super(name, description); }

    public void addConnectTo(String locationName) { connectTo.add(locationName); }

    public void addConnectFrom(String locationName) { connectFrom.add(locationName); }

    public void setFirstLocation() { isFirstLocation = true; }

    public void addPlayers(Player player) { players.add(player); }

    public void addArtefact(ArtefactsEntity artefactsEntity) { artefacts.add(artefactsEntity); }

    public void addFurniture(FurnitureEntity furnitureEntity) { furniture.add(furnitureEntity); }

    public void addCharacter(CharactersEntity charactersEntity) { characters.add(charactersEntity); }

    public ArrayList<ArtefactsEntity> getArtefacts() { return artefacts; }

    public ArrayList<CharactersEntity> getCharacters() { return characters; }

    public ArrayList<FurnitureEntity> getFurniture() { return furniture; }
    public ArrayList<Player> getPlayers() { return players; }

    public ArrayList<String> getConnectTo() { return connectTo; }
}
