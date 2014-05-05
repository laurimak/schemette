package schemette.expressions;

import java.util.List;

public class ListExpression implements Expression {
    public final List<Expression> value;

    public ListExpression(List<Expression> value) {
        this.value = value;
    }

    public static ListExpression valueOf(List<Expression> list) {
        return new ListExpression(list);
    }

    @Override
    public boolean equals(Object o) {
        return getClass() == o.getClass() && value.equals(((ListExpression) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public String toString() {
        return String.format("list(%s)", value.stream()
                .map(Expression::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse(""));
    }
}
