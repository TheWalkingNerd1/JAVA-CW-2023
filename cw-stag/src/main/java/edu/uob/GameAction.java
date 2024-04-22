package edu.uob;

import java.util.ArrayList;

public class GameAction {
    private ArrayList<String> triggers;
    private ArrayList<String> subjects;
    private ArrayList<String> consumed;
    private ArrayList<String> produced;

    private String narration = "default";

    public void setTriggers(ArrayList<String> triggers) {
        this.triggers = triggers;
    }

    public void setSubjects(ArrayList<String> subjects) {
        this.subjects = subjects;
    }

    public void setConsumed(ArrayList<String> consumed) {
        this.consumed = consumed;
    }

    public void setProduced(ArrayList<String> produced) {
        this.produced = produced;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getNarration() {
        return this.narration;
    }
}

