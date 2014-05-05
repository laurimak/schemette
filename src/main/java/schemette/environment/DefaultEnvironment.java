package schemette.environment;

import com.google.common.collect.ImmutableMap;
import schemette.expressions.*;

import java.util.HashMap;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static schemette.expressions.BooleanExpression.*;
import static schemette.expressions.SymbolExpression.symbol;

public class DefaultEnvironment {
    private static final ImmutableMap<SymbolExpression, Expression> PRIMITIVES = ImmutableMap.<SymbolExpression, Expression>builder()
            .put(symbol("+"),
                    function((args) -> longFunction(args, (a, b) -> a + b)))
            .put(symbol("-"),
                    function((args) -> longFunction(args, (a, b) -> a - b)))
            .put(symbol("/"),
                    function((args) -> longFunction(args, (a, b) -> a / b)))
            .put(symbol("*"),
                    function((args) -> longFunction(args, (a, b) -> a * b)))
            .put(symbol("="),
                    function((args) -> bool(args.stream()
                            .allMatch((e) -> e.equals(args.get(0))))))
            .put(symbol("#t"),
                    bool(true))
            .put(symbol("#f"),
                    bool(false))

                            .build();

    public static Environment newInstance() {
        return new Environment(new HashMap<>(PRIMITIVES));
    }

    private static ProcedureExpression function(Function<List<Expression>, Expression> f) {
        return ProcedureExpression.procedure(f);
    }

    private static NumberExpression longFunction(List<Expression> args, BinaryOperator<Long> accumulator) {
        return args.stream()
                .map(a -> ((NumberExpression) a).value)
                .reduce(accumulator)
                .map(NumberExpression::number)
                .get();
    }
}
