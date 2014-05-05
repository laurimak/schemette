package schemette.environment;

import com.google.common.collect.ImmutableMap;
import schemette.expressions.Expression;
import schemette.expressions.NumberExpression;
import schemette.expressions.SymbolExpression;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import static schemette.expressions.BooleanExpression.bool;
import static schemette.expressions.ProcedureExpression.procedure;
import static schemette.expressions.SymbolExpression.symbol;

public class DefaultEnvironment {
    private static final ImmutableMap<SymbolExpression, Expression> PRIMITIVES = ImmutableMap.<SymbolExpression, Expression>builder()
            .put(symbol("+"),
                    procedure(args -> longFunction(args, (a, b) -> a + b)))
            .put(symbol("-"),
                    procedure(args -> longFunction(args, (a, b) -> a - b)))
            .put(symbol("/"),
                    procedure(args -> longFunction(args, (a, b) -> a / b)))
            .put(symbol("*"),
                    procedure(args -> longFunction(args, (a, b) -> a * b)))
            .put(symbol("="),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a.equals(b)))))
            .put(symbol(">"),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a > b))))
            .put(symbol("<"),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a < b))))
            .put(symbol(">="),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a >= b))))
            .put(symbol("<="),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a <= b))))
            .put(symbol("#t"),
                    bool(true))
            .put(symbol("#f"),
                    bool(false))
                            .build();

    private static boolean satisfiesTransitivePredicate(List<Expression> args, BiPredicate<Long, Long> predicate) {
        return IntStream.range(1, args.size())
                .allMatch(i -> predicate.test(((NumberExpression) args.get(i - 1)).value, ((NumberExpression) args.get(i)).value));
    }

    public static Environment newInstance() {
        return new Environment(new HashMap<>(PRIMITIVES));
    }

    private static NumberExpression longFunction(List<Expression> args, BinaryOperator<Long> accumulator) {
        return args.stream()
                .map(a -> ((NumberExpression) a).value)
                .reduce(accumulator)
                .map(NumberExpression::number)
                .get();
    }
}
