package schemette.expressions;

public class BooleanExpression implements Expression {
    public final boolean value;

    public BooleanExpression(boolean value) {
        this.value = value;
    }

    public static BooleanExpression bool(boolean value) {
        return new BooleanExpression(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return value == ((BooleanExpression) o).value;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.format("bool(%s)", value);
    }
}
