package edu.uob;

import edu.uob.*;
import java.util.ArrayList;
import java.util.Objects;

public class CommandSelect extends SqlCommand implements DatabaseOperations {
    public CommandSelect(ArrayList<String> tokens) {
        super(tokens);
    }

    public void parsingResult() throws ParsingException {
        parsingWildAttributeList();
    }

    void parsingWildAttributeList() throws ParsingException {
        if (Objects.equals(tokens.get(currentWord), "*")) return;
        super.parsingAttributeList();
    }
}
