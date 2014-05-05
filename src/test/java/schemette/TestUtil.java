package schemette;

import com.google.common.collect.ImmutableList;
import schemette.expressions.Expression;
import schemette.expressions.ListExpression;
import schemette.expressions.SymbolExpression;

public class TestUtil {
    static ListExpression list(Expression... exps) {
        return ListExpression.valueOf(ImmutableList.copyOf(exps));
    }

    static SymbolExpression symbol(String symbol) {
        return SymbolExpression.symbol(symbol);
    }
}
