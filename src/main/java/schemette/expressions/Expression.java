package schemette.expressions;

public interface Expression {
    static final Expression NONE = new Expression() {};

    public static Expression none() {
        return NONE;
    }
}
