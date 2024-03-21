package edu.uob.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    String[] specialCharacters = {"(",")",",",";"};
    private final Pattern comparePattern = Pattern.compile("<=?|>=?|==?|!=");
    ArrayList<String> tokens = new ArrayList<String>();

    public Tokenizer(String query){
        // Remove any whitespace at the beginning and end of the query
        query = query.trim();
        // Split the query on single quotes (to separate out query characters from string literals)
        String[] fragments = query.split("'");
        for (int i=0; i<fragments.length; i++) {
            // Every odd fragment is a string literal, so just append it without any alterations
            if (i%2 != 0) tokens.add("'" + fragments[i] + "'");
            // If it's not a string literal, it must be query characters (which need further processing)
            else {
            String processedFragment = processComparisonOperators(fragments[i]);
            String[] nextBatchOfTokens = tokenise(processedFragment);
            // Tokenise the fragments into an array of strings
            // Then add these to the "result" array list (needs a bit of conversion)
            tokens.addAll(Arrays.asList(nextBatchOfTokens));
            }
        }
    }
    
    public ArrayList<String> getTokens() {
        return tokens;
    }

    String processComparisonOperators(String input) {
        Matcher matcher = comparePattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // Ensure the matched operator is padded with spaces
            matcher.appendReplacement(sb, " " + matcher.group() + " ");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    String[] tokenise(String input){
        // Add in some extra padding spaces around the "special characters"
        // so we can be sure that they are separated by AT LEAST one space (possibly more)
        for (String specialCharacter : specialCharacters) {
            input = input.replace(specialCharacter, " " + specialCharacter + " ");
        }
        // Remove all double spaces (the previous replacements may had added some)
        // This is "blind" replacement - replacing if they exist, doing nothing if they don't
        while (input.contains("  ")) input = input.replaceAll("  ", " ");
        // Again, remove any whitespace from the beginning and end that might have been introduced
        input = input.trim();
        // Finally split on the space char (since there will now ALWAYS be a space between tokens)
        return input.split(" ");
    }
}
