package schemette;

import com.google.common.collect.ImmutableList;
import schemette.expressions.Expression;
import schemette.expressions.ListExpression;

public class TestUtil {
    static ListExpression list(Expression... exps) {
        return ListExpression.valueOf(ImmutableList.copyOf(exps));
    }
}
