package schemette;

import org.junit.Test;
import schemette.environment.DefaultEnvironment;
import schemette.expressions.Expression;
import schemette.expressions.NumberExpression;
import schemette.expressions.ProcedureExpression;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static schemette.expressions.BooleanExpression.bool;
import static schemette.expressions.NumberExpression.number;
import static schemette.expressions.SymbolExpression.symbol;

public class DefaultEnvironmentTest {

    @Test
    public void addition() {
        Function<List<Expression>, Expression> function = lookupFunction("+");

        assertThat(number(6), is(function.apply(numberList(1, 2, 3))));
    }

    @Test
    public void subtraction() {
        Function<List<Expression>, Expression> function = lookupFunction("-");

        assertThat(number(5), is(function.apply(numberList(10, 2, 3))));
    }

    @Test
    public void multiplication() {
        Function<List<Expression>, Expression> function = lookupFunction("*");

        assertThat(number(60), is(function.apply(numberList(10, 2, 3))));
    }

    @Test
    public void division() {
        Function<List<Expression>, Expression> function = lookupFunction("/");

        assertThat(number(2), is(function.apply(numberList(12, 2, 3))));
    }

    @Test
    public void equality_equals() {
        Function<List<Expression>, Expression> function = lookupFunction("=");

        assertThat(bool(true), is(function.apply(numberList(2, 2, 2))));
    }

    @Test
    public void equality_not_equals() {
        Function<List<Expression>, Expression> function = lookupFunction("=");

        assertThat(bool(false), is(function.apply(numberList(12, 2, 3))));
    }

    @Test
    public void greater_than_true() {
        Function<List<Expression>, Expression> function = lookupFunction(">");

        assertThat(bool(true), is(function.apply(numberList(3, 2, 1))));
    }

    @Test
    public void greater_than_false() {
        Function<List<Expression>, Expression> function = lookupFunction(">");

        assertThat(bool(false), is(function.apply(numberList(1, 2, 1))));
    }

    @Test
    public void less_than_true() {
        Function<List<Expression>, Expression> function = lookupFunction("<");

        assertThat(bool(true), is(function.apply(numberList(1, 2, 3))));
    }

    @Test
    public void less_than_false() {
        Function<List<Expression>, Expression> function = lookupFunction("<");

        assertThat(bool(false), is(function.apply(numberList(3, 2, 3))));
    }

    @Test
    public void greater_than_or_equal_true() {
        Function<List<Expression>, Expression> function = lookupFunction(">=");

        assertThat(bool(true), is(function.apply(numberList(3, 2, 2))));
    }

    @Test
    public void greater_than_or_equal_false() {
        Function<List<Expression>, Expression> function = lookupFunction(">=");

        assertThat(bool(false), is(function.apply(numberList(3, 2, 3))));
    }

    @Test
    public void less_than_or_equal_true() {
        Function<List<Expression>, Expression> function = lookupFunction("<=");

        assertThat(bool(true), is(function.apply(numberList(2, 2, 3))));
    }

    @Test
    public void less_than_or_equal_false() {
        Function<List<Expression>, Expression> function = lookupFunction("<=");

        assertThat(bool(false), is(function.apply(numberList(2, 3, 2))));
    }

    private static List<Expression> numberList(Integer... numbers) {
        return Arrays.stream(numbers)
                .map(NumberExpression::number)
                .collect(Collectors.toList());
    }

    private Function<List<Expression>, Expression> lookupFunction(String operator) {
        return ((ProcedureExpression) DefaultEnvironment.newInstance().lookup(symbol(operator))).lambda;
    }
}
