package schemette.expressions;

public class SymbolExpression implements Expression {
    public final String value;

    public SymbolExpression(String value) {
        this.value = value;
    }

    public static SymbolExpression symbol(String s) {
        return new SymbolExpression(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return value.equals(((SymbolExpression) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public String toString() {
        return String.format("symbol(%s)", value);
    }
}
