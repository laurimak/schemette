package schemette.exception;

public class UnexpectedExpression extends RuntimeException {
    public UnexpectedExpression(String message) {
        super(message);
    }
}
