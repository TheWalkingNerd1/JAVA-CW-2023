package edu.uob.utilities;

import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Node;
import edu.uob.GameEntity;
import edu.uob.entities.*;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

public class EntityGenerator {
    private final Parser parser = new Parser();

    public EntityGenerator(File entitiesFile) {
        try {
            FileReader reader = new FileReader(entitiesFile);
            parser.parse(reader);
        } catch (FileNotFoundException fnfe) {
            System.out.println("FileNotFoundException was thrown when attempting to read basic entities file");
        } catch (ParseException pe) {
            System.out.println("ParseException was thrown when attempting to read basic entities file");
        }
    }

    public void generateEntities(Map<String, GameEntity> entities) {
        Graph wholeDocument = parser.getGraphs().get(0);
        ArrayList<Graph> sections = wholeDocument.getSubgraphs();
        // The locations will always be in the first subgraph
        ArrayList<Graph> locations = sections.get(0).getSubgraphs();
        for(Graph graph : locations) {
            Node locationDetails = graph.getNodes(false).get(0);
            String locationName = locationDetails.getId().getId().toLowerCase();
            String description = locationDetails.getAttribute("description");
            LocationEntity entity = new LocationEntity(locationName, description);
            entities.put(locationName, entity);
            generateSubEntities(graph, entities);
        }
        //Set first location
        String firstLocationName = locations.get(0).getNodes(false).get(0).getId().getId().toLowerCase();
        if(entities.get(firstLocationName) instanceof LocationEntity locationEntity) locationEntity.setFirstLocation();
        //Locate entities
        locateEntities(entities);
    }

    public void generatePath(Map<String, GameEntity> entities) {
        Graph wholeDocument = parser.getGraphs().get(0);
        ArrayList<Graph> sections = wholeDocument.getSubgraphs();
        // The paths will always be in the second subgraph
        ArrayList<Edge> paths = sections.get(1).getEdges();
        for(Edge path : paths) {
            String fromName = path.getSource().getNode().getId().getId().toLowerCase();
            String toName = path.getTarget().getNode().getId().getId().toLowerCase();
            if(entities.get(fromName) instanceof LocationEntity locationEntity) locationEntity.addConnectTo(toName);
        }
    }

    private void locateEntities(Map<String, GameEntity> entities) {
        for(GameEntity gameEntity : entities.values()) {
            if(gameEntity instanceof ArtefactsEntity artefactsEntity) {
                if(entities.get(artefactsEntity.getLocation()) instanceof LocationEntity locationEntity)
                    locationEntity.addArtefact(artefactsEntity.getName(), artefactsEntity);
            }
            if(gameEntity instanceof StationaryEntity stationaryEntity) {
                if(entities.get(stationaryEntity.getLocation()) instanceof LocationEntity locationEntity)
                    locationEntity.addProperty(stationaryEntity.getName(), stationaryEntity);
            }
        }
    }

    private void generateSubEntities(Graph graph, Map<String, GameEntity> entities) {
        ArrayList<Graph> subGraphs = graph.getSubgraphs();
        String locationName = graph.getNodes(false).get(0).getId().getId().toLowerCase();
        for(Graph subGraph : subGraphs) {
            String entityType = subGraph.getId().getId();
            if(entityType.equals("artefacts")) generateArtefacts(subGraph.getNodes(false), entities, locationName);
            if(entityType.equals("furniture")) generateFurnitureAndCharacters(subGraph.getNodes(false), entities,locationName);
            if(entityType.equals("characters")) generateFurnitureAndCharacters(subGraph.getNodes(false), entities,locationName);
        }
    }

    private void generateArtefacts(ArrayList<Node> nodes, Map<String, GameEntity> entities, String locationName) {
        for(Node node : nodes) {
            String artefactsName = node.getId().getId().toLowerCase();
            String description = node.getAttribute("description");
            ArtefactsEntity entity = new ArtefactsEntity(artefactsName, description, locationName);
            entities.put(artefactsName, entity);
        }
    }

    private void generateFurnitureAndCharacters(ArrayList<Node> nodes, Map<String, GameEntity> entities, String locationName) {
        for(Node node : nodes) {
            String entityName = node.getId().getId().toLowerCase();
            String description = node.getAttribute("description");
            StationaryEntity entity = new StationaryEntity(entityName, description, locationName);
            entities.put(entityName, entity);
        }
    }
}
