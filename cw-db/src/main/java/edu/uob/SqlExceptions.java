package edu.uob;

public class SqlExceptions {
    public static class ParsingException extends Exception {
        public ParsingException(String message) {
            super(message);
        }
    }

    public static class InterpretingException extends Exception {
        public InterpretingException(String message) {
            super(message);
        }
    }
}
