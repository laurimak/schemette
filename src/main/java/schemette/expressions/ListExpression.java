package schemette.expressions;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ListExpression implements Expression {
    public final List<Expression> value;

    public ListExpression(List<Expression> value) {
        this.value = value;
    }

    public static ListExpression list(List<Expression> list) {
        return new ListExpression(list);
    }

    public static ListExpression list(Expression... exps) {
        return ListExpression.list(ImmutableList.copyOf(exps));
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
                .map(Object::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse(""));
    }
}
