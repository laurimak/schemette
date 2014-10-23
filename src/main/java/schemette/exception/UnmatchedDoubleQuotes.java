package schemette.exception;

public class UnmatchedDoubleQuotes extends RuntimeException {
    public UnmatchedDoubleQuotes(String message) {
        super(message);
    }
}
