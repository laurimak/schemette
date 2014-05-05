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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ListExpression that = (ListExpression) o;

        if (!value.equals(that.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public String toString() {
        return String.format("list(%s)", value.stream()
                .map((a) -> a.toString())
                .reduce((a, b) -> a + ", " + b)
                .orElse(""));
    }
}
