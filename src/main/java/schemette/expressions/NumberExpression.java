package schemette.expressions;

public class NumberExpression implements Expression {
    public final long value;

    public NumberExpression(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public static NumberExpression number(long n) {
        return new NumberExpression(n);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return value == ((NumberExpression) o).value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    public String toString() {
        return String.format("number(%s)", value);
    }
}
