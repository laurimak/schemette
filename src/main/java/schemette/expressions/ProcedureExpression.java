package schemette.expressions;

import java.util.List;
import java.util.function.Function;

public class ProcedureExpression implements Expression {

    public final Function<List<Expression>, Expression> lambda;

    public ProcedureExpression(Function<List<Expression>, Expression> lambda) {
        this.lambda = lambda;
    }


    public static ProcedureExpression procedure(Function<List<Expression>, Expression> lambda) {
        return new ProcedureExpression(lambda);
    }
}
