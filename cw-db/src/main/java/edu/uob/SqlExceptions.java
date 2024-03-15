package edu.uob;

public class SqlExceptions {
    public static class ParsingException extends Exception {
        private static final long serialVersionUID = 1L;
        public ParsingException(String message) {
            super(message);
        }
    }

    public static class InterpretingException extends Exception {
        private static final long serialVersionUID = 2L;
        public InterpretingException(String message) {
            super(message);
        }
    }
}
