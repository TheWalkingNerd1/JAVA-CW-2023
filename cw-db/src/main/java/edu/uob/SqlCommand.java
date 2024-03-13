package edu.uob;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.Objects;

public class SqlCommand {
    protected ArrayList<String> tokens = new ArrayList<String>();
    protected int currentWord;

    public  SqlCommand(ArrayList<String> tokens) {
        this.tokens = tokens;
        currentWord = 1;
    }

    public static SqlCommand parseCommandType (ArrayList<String> tokens) {
        return switch (tokens.get(0)) {
            case "SELECT" -> new CommandSelect(tokens);
            case "CREATE" -> new CommandCreate(tokens);
            default -> null;
        };
    }

    protected boolean isPlainText(String token) {
        return true;
    }

    protected void parsingAttributeList() throws ParsingException {
        if(Objects.equals(tokens.get(currentWord), "FROM")) return;
        if(currentWord == tokens.size() - 1)
            throw new ParsingException("Invalid command");
        currentWord ++;
        System.out.println(tokens.get(currentWord));
        if(!Objects.equals(tokens.get(currentWord), ","))
            throw new ParsingException(("please use , to separate attributes"));
        currentWord ++;
        System.out.println(tokens.get(currentWord));
        parsingAttributeList();
    }
}
