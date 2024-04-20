package edu.uob.utilities;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class EntityGenerator {
    public EntityGenerator(File entitiesFile) {
        try {
            Parser parser = new Parser();
            FileReader reader = new FileReader("config" + File.separator + "basic-entities.dot");
            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            ArrayList<Graph> sections = wholeDocument.getSubgraphs();
            ArrayList<Graph> locations = sections.get(0).getSubgraphs();
            Graph firstLocation = locations.get(0);
            Node locationDetails = firstLocation.getNodes(false).get(0);
            Graph art = firstLocation.getSubgraphs().get(0);
            Node Axe = art.getNodes(false).get(0);
            String description = Axe.getAttribute("description");
            System.out.println(description);
        } catch (FileNotFoundException fnfe) {
            System.out.println("FileNotFoundException was thrown when attempting to read basic entities file");
        } catch (ParseException pe) {
            System.out.println("ParseException was thrown when attempting to read basic entities file");
        }
    }
}
