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
    private Parser parser = new Parser();

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
            String locationName = locationDetails.getId().getId();
            String description = locationDetails.getAttribute("description");
            LocationEntity entity = new LocationEntity(locationName, description);
            entities.put(locationName.toLowerCase(), entity);
            generateSubEntities(graph, entities);
        }
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
            if(entities.get(toName) instanceof LocationEntity locationEntity) locationEntity.addConnectFrom(fromName);
        }
    }

    private void generateSubEntities(Graph graph, Map<String, GameEntity> entities) {
        ArrayList<Graph> subGraphs = graph.getSubgraphs();
        String locationName = graph.getNodes(false).get(0).getId().getId();
        for(Graph subGraph : subGraphs) {
            String entityType = subGraph.getId().getId();
            if(entityType.equals("artefacts")) generateArtefacts(subGraph.getNodes(false), entities, locationName);
            if(entityType.equals("furniture")) generateFurniture(subGraph.getNodes(false), entities);
            if(entityType.equals("characters")) generateCharacters(subGraph.getNodes(false), entities);
        }
    }

    private void generateArtefacts(ArrayList<Node> nodes, Map<String, GameEntity> entities, String locationName) {
        for(Node node : nodes) {
            String artefactsName = node.getId().getId();
            String description = node.getAttribute("description");
            ArtefactsEntity entity = new ArtefactsEntity(artefactsName, description, locationName);
            entities.put(artefactsName.toLowerCase(), entity);
        }
    }

    private void generateFurniture(ArrayList<Node> nodes, Map<String, GameEntity> entities) {
        for(Node node : nodes) {
            String furnitureName = node.getId().getId();
            String description = node.getAttribute("description");
            FurnitureEntity entity = new FurnitureEntity(furnitureName, description);
            entities.put(furnitureName.toLowerCase(), entity);
        }
    }

    private void generateCharacters(ArrayList<Node> nodes, Map<String, GameEntity> entities) {
        for(Node node : nodes) {
            String characterName = node.getId().getId();
            String description = node.getAttribute("description");
            CharactersEntity entity = new CharactersEntity(characterName, description);
            entities.put(characterName.toLowerCase(), entity);
        }
    }
}
