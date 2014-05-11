package schemette;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import schemette.environment.Environment;
import schemette.expressions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Evaluator {

    private static final Set<SymbolExpression> SPECIAL_FORMS = ImmutableSet.of("quote", "set!", "define", "if", "lambda", "begin", "let", "eval").stream()
            .map(SymbolExpression::symbol)
            .collect(Collectors.toSet());

    public static Expression evaluate(Expression exp, Environment env) {
        return analyze(exp).apply(env);
    }

    public static Function<Environment, Expression> analyze(Expression exp) {
        if (isSelfEvaluating(exp)) {
            return e -> exp;
        } else if (isVariableReference(exp)) {
            return env -> env.lookup(exp.symbol());
        } else if (isSpecialForm(exp)) {
            return analyzeSpecialForm(exp.list());
        } else if (isFunctionCall(exp)) {
            return analyzeFunctionCall(exp.list());
        }

        throw new IllegalArgumentException(String.format("Unable to evaluate expression '%s'", exp));
    }

    private static Function<Environment, Expression> analyzeSpecialForm(ListExpression exp) {
        List<Expression> exps = exp.value;
        switch (exps.get(0).symbol().value) {
            case "quote":
                return env -> exps.get(1);
            case "set!": {
                SymbolExpression symbol = exps.get(1).symbol();
                Function<Environment, Expression> valueProc = analyze(exps.get(2));

                return env -> {
                    env.set(symbol, valueProc.apply(env));
                    return Expression.none();
                };
            }
            case "define": {
                if (exps.get(1) instanceof SymbolExpression) {
                    SymbolExpression symbol = exps.get(1).symbol();
                    Function<Environment, Expression> valueProc = analyze(exps.get(2));
                    return env -> {
                        env.define(symbol, valueProc.apply(env));
                        return Expression.none();
                    };
                } else {
                    SymbolExpression name = exps.get(1).list().value.get(0).symbol();
                    List<SymbolExpression> paramNames = exps.get(1).list().value.stream()
                            .skip(1)
                            .map(Expression::symbol)
                            .collect(Collectors.toList());
                    Function<Environment, Expression> body = analyzeSequence(exps.subList(2, exps.size()));
                    Function<Environment, Expression> lambda = analyzeLambda(paramNames, body);
                    return env -> {
                        env.define(name, lambda.apply(env));
                        return Expression.none();
                    };
                }
            }
            case "if":
                return analyzeIf(exps);
            case "lambda":
                List<SymbolExpression> paramNames = exps.get(1).list().value.stream()
                        .map(Expression::symbol)
                        .collect(Collectors.toList());
                Function<Environment, Expression> body = analyzeSequence(exps.subList(2, exps.size()));
                return analyzeLambda(paramNames, body);
            case "begin":
                return analyzeBegin(exps);
            case "let":
                return analyzeLet(exps);
            case "eval":
                return analyzeEval(exps);
        }

        throw new IllegalArgumentException(String.format("Invalid special form expression '%s'", exp));

    }

    private static Function<Environment, Expression> analyzeFunctionCall(ListExpression exp) {
        List<Function<Environment, Expression>> map = exp.value.stream()
                .map(e -> analyze(e))
                .collect(Collectors.toList());

        return env -> {
            List<Expression> list = map.stream()
                    .map(e -> e.apply(env))
                    .collect(Collectors.toList());
            return list.get(0).procedure().lambda.apply(rest(list));
        };
    }

    private static <T> List<T> rest(List<T> list) {
        return list.subList(1, list.size());
    }

    private static Function<Environment, Expression> analyzeLet(List<Expression> exps) {
        Map<SymbolExpression, Function<Environment, Expression>> letBindings = letBindings(exps);
        Function<Environment, Expression> letBody = analyzeLambda(ImmutableList.copyOf(letBindings.keySet()), analyze(exps.get(2)));

        return env -> {
            List<Expression> letParams = letBindings.values().stream()
                    .map(a -> a.apply(env))
                    .collect(Collectors.toList());
            return letBody.apply(env).procedure().lambda.apply(letParams);
        };
    }

    private static Map<SymbolExpression, Function<Environment, Expression>> letBindings(List<Expression> exps) {
        return exps.get(1).list().value.stream()
                .map(Expression::list)
                .collect(Collectors.toMap(a -> a.value.get(0).symbol(),
                        a -> analyze(a.value.get(1))));
    }

    private static Function<Environment, Expression> analyzeBegin(List<Expression> exps) {
        return analyzeSequence(rest(exps));
    }

    private static Function<Environment, Expression> analyzeSequence(List<Expression> exps) {
        List<Function<Environment, Expression>> seq = exps.stream()
                .map(Evaluator::analyze)
                .collect(Collectors.toList());

        return env ->
                seq.stream()
                        .collect(Collectors.reducing(Expression.none(), a -> a.apply(env), (a, b) -> b));
    }

    private static Function<Environment, Expression> analyzeIf(List<Expression> exps) {
        Function<Environment, Expression> condition = analyze(exps.get(1));
        Function<Environment, Expression> consequent = analyze(exps.get(2));
        Optional<Function<Environment, Expression>> alternative = exps.size() > 3 ? Optional.of(analyze(exps.get(3))) : Optional.empty();
        return env -> {
            if (isTruthy(condition.apply(env))) {
                return consequent.apply(env);
            } else {
                return alternative.map(a -> a.apply(env)).orElse(Expression.none());
            }
        };
    }


    private static Function<Environment, Expression> analyzeEval(List<Expression> exps) {
        Function<Environment, Expression> code = analyze(exps.get(1));
        return env -> analyze(code.apply(env)).apply(env);
    }

    private static Function<Environment, Expression> analyzeLambda(List<SymbolExpression> names, Function<Environment, Expression> body) {
        return env ->
                ProcedureExpression.procedure(args ->
                        body.apply(env.extend(makeMap(names, args))));
    }

    private static Map<SymbolExpression, Expression> makeMap(List<SymbolExpression> names, List<Expression> args) {
        return IntStream.range(0, names.size())
                .mapToObj(Integer::new)
                .map(i -> (Integer) i)
                .collect(Collectors.toMap(i -> names.get(i).symbol(),
                        args::get));
    }

    private static boolean isSpecialForm(Expression exp) {
        return exp instanceof ListExpression && SPECIAL_FORMS.contains(exp.list().value.get(0));
    }

    private static boolean isFunctionCall(Expression exp) {
        return exp instanceof ListExpression && exp.list().value.size() > 0;
    }

    private static boolean isVariableReference(Expression exp) {
        return exp instanceof SymbolExpression;
    }

    private static boolean isSelfEvaluating(Expression exp) {
        return exp instanceof NumberExpression || exp instanceof BooleanExpression || exp == Expression.none() || (exp instanceof ListExpression && exp.list().value.size() == 0);
    }

    private static boolean isTruthy(Expression exp) {
        return !BooleanExpression.bool(false).equals(exp);
    }
}
