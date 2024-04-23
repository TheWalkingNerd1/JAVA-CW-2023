package edu.uob.utilities;

import edu.uob.GameAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ActionGenerator {
    private NodeList actionsList;
    private ArrayList<String> triggers;
    private ArrayList<String> subjects;
    private ArrayList<String> consumed;
    private ArrayList<String> produced;
    private String narration = "default";

    public ActionGenerator(File actionsFile) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(actionsFile);
            Element root = document.getDocumentElement();
            actionsList = root.getChildNodes();
        } catch(ParserConfigurationException pce) {
            System.out.println("ParserConfigurationException was thrown when attempting to read basic actions file");
        } catch(SAXException saxe) {
            System.out.println("SAXException was thrown when attempting to read basic actions file");
        } catch(IOException ioe) {
            System.out.println("IOException was thrown when attempting to read basic actions file");
        }
    }

    public void generateActions(HashMap<String, HashSet<GameAction>> actions) {
        for(int i = 0; i < actionsList.getLength(); i++) {
            if (i % 2 == 1) {
                dismantleActions((Element)actionsList.item(i), actions);
                GameAction gameAction =new GameAction();
                gameAction.setTriggers(triggers);
                gameAction.setSubjects(subjects);
                gameAction.setConsumed(consumed);
                gameAction.setProduced(produced);
                gameAction.setNarration(narration);
                constructActionMap(actions, gameAction);
            }
        }
    }

    private void constructActionMap(HashMap<String, HashSet<GameAction>> actions, GameAction gameAction) {
        for(String trigger : triggers){
            if(!actions.containsKey(trigger.toLowerCase())) {
                HashSet<GameAction> gameActionSet = new HashSet<GameAction>();
                actions.put(trigger.toLowerCase(), gameActionSet);
            }
            actions.get(trigger.toLowerCase()).add(gameAction);
        }
        for(String subject : subjects){
            if(!actions.containsKey(subject.toLowerCase())) {
                HashSet<GameAction> gameActionSet = new HashSet<GameAction>();
                actions.put(subject.toLowerCase(), gameActionSet);
            }
            actions.get(subject).add(gameAction);
        }
    }

    private void dismantleActions(Element action, HashMap<String, HashSet<GameAction>> actions) {
        narration = action.getElementsByTagName("narration").item(0).getTextContent();
        constructArrayLists(action);
    }

    private void constructArrayLists(Element action) {
        triggers = new ArrayList<>();
        subjects = new ArrayList<>();
        consumed = new ArrayList<>();
        produced = new ArrayList<>();

        Element triggerList = (Element)action.getElementsByTagName("triggers").item(0);
        NodeList keyPhrase = triggerList.getElementsByTagName("keyphrase");
        for(int i = 0; i < keyPhrase.getLength(); i++) {
            if(keyPhrase.item(i) instanceof Element keyPhraseNode) triggers.add(keyPhraseNode.getTextContent());
        }
        Element subjectsList = (Element)action.getElementsByTagName("subjects").item(0);
        NodeList subjectEntities = subjectsList.getElementsByTagName("entity");
        for(int i = 0; i < subjectEntities.getLength(); i++) {
            if(subjectEntities.item(i) instanceof Element subject) subjects.add(subject.getTextContent());
            //System.out.println(subjectEntities.item(i).getTextContent());
        }
        Element consumedList = (Element)action.getElementsByTagName("consumed").item(0);
        NodeList consumedEntities = consumedList.getElementsByTagName("entity");
        for(int i = 0; i < consumedEntities.getLength(); i++) {
            if(consumedEntities.item(i) instanceof Element consumedNode) consumed.add(consumedNode.getTextContent());
        } 
        Element producedList = (Element)action.getElementsByTagName("produced").item(0);
        NodeList producedEntities = producedList.getElementsByTagName("entity");
        for(int i = 0; i < producedEntities.getLength(); i++) {
            if(producedEntities.item(i) instanceof Element producedNode) produced.add(producedNode.getTextContent());
        } 
    }
  
}
