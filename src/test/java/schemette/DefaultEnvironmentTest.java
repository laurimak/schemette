package schemette;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import schemette.environment.DefaultEnvironment;
import schemette.expressions.BooleanExpression;
import schemette.expressions.Expression;
import schemette.expressions.NumberExpression;
import schemette.expressions.ProcedureExpression;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static schemette.expressions.BooleanExpression.bool;
import static schemette.expressions.NumberExpression.number;
import static schemette.expressions.SymbolExpression.symbol;

public class DefaultEnvironmentTest {

    @Test
    public void addition() {
        Function<List<Expression>, Expression> function = lookupFunction("+");

        assertThat(number(6), is((NumberExpression) function.apply(ImmutableList.of(number(1), number(2), number(3)))));
    }

    @Test
    public void subtraction() {
        Function<List<Expression>, Expression> function = lookupFunction("-");

        assertThat(number(5), is((NumberExpression) function.apply(ImmutableList.of(number(10), number(2), number(3)))));
    }

    @Test
    public void multiplication() {
        Function<List<Expression>, Expression> function = lookupFunction("*");

        assertThat(number(60), is((NumberExpression) function.apply(ImmutableList.of(number(10), number(2), number(3)))));
    }

    @Test
    public void division() {
        Function<List<Expression>, Expression> function = lookupFunction("/");

        assertThat(number(2), is((NumberExpression) function.apply(ImmutableList.of(number(12), number(2), number(3)))));
    }

    @Test
    public void equality_equals() {
        Function<List<Expression>, Expression> function = lookupFunction("=");

        assertThat(bool(true), is((BooleanExpression) function.apply(ImmutableList.of(number(2), number(2), number(2)))));
    }

    @Test
    public void equality_not_equals() {
        Function<List<Expression>, Expression> function = lookupFunction("=");

        assertThat(bool(false), is((BooleanExpression) function.apply(ImmutableList.of(number(12), number(2), number(3)))));
    }

    private Function<List<Expression>, Expression> lookupFunction(String operator) {
        return ((ProcedureExpression) DefaultEnvironment.newInstance().lookup(symbol(operator))).lambda;
    }
}
