package schemette;

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
        } else if (exp.isSymbol()) {
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
                return analyzeQuote(exps);
            case "set!":
                return analyzeSet(exps);

            case "define":
                if (isVarDefinition(exps)) {
                    return analyzeVarDefinition(exps);
                } else {
                    return analyzeFunctionDefinition(exps);
                }
            case "if":
                return analyzeIf(exps);
            case "lambda":
                return analyzeLambda(exps);
            case "begin":
                return analyzeBegin(exps);
            case "let":
                return analyzeLet(exps);
            case "eval":
                return analyzeEval(exps);
        }

        throw new IllegalArgumentException(String.format("Invalid special form expression '%s'", exp));

    }

    private static Function<Environment, Expression> analyzeQuote(List<Expression> exps) {
        return env -> exps.get(1);
    }

    private static Function<Environment, Expression> analyzeLambda(List<Expression> exps) {
        List<SymbolExpression> paramNames = exps.get(1).list().value.stream()
                .map(Expression::symbol)
                .collect(Collectors.toList());
        return analyzeProcedure(paramNames, exps);
    }

    private static Function<Environment, Expression> analyzeSet(List<Expression> exps) {
        SymbolExpression symbol = exps.get(1).symbol();
        Function<Environment, Expression> valueProc = analyze(exps.get(2));

        return env -> {
            env.set(symbol, valueProc.apply(env));
            return Expression.none();
        };
    }

    private static Function<Environment, Expression> analyzeFunctionDefinition(List<Expression> exps) {
        SymbolExpression name = exps.get(1).list().value.get(0).symbol();
        List<SymbolExpression> paramNames = rest(exps.get(1).list().value).stream()
                .map(Expression::symbol)
                .collect(Collectors.toList());
        Function<Environment, Expression> lambda = analyzeProcedure(paramNames, exps);
        return env -> {
            env.define(name, lambda.apply(env));
            return Expression.none();
        };
    }

    private static Function<Environment, Expression> analyzeVarDefinition(List<Expression> exps) {
        SymbolExpression symbol = exps.get(1).symbol();
        Function<Environment, Expression> valueProc = analyze(exps.get(2));
        return env -> {
            env.define(symbol, valueProc.apply(env));
            return Expression.none();
        };
    }

    private static boolean isVarDefinition(List<Expression> exps) {
        return exps.get(1).isSymbol();
    }

    private static Function<Environment, Expression> analyzeFunctionCall(ListExpression exp) {
        List<Function<Environment, Expression>> map = exp.value.stream()
                .map(Evaluator::analyze)
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
        List<Function<Environment, Expression>> letBindingValues = letBindingValues(exps);
        Function<Environment, Expression> letBody = analyzeProcedure(letBindingSymbols(exps), exps);

        return env -> {
            List<Expression> letParams = letBindingValues.stream()
                    .map(a -> a.apply(env))
                    .collect(Collectors.toList());
            return letBody.apply(env).procedure().lambda.apply(letParams);
        };
    }

    private static List<SymbolExpression> letBindingSymbols(List<Expression> exps) {
        return exps.get(1).list().value.stream()
                .map(e -> e.list().value.get(0).symbol())
                .collect(Collectors.toList());
    }

    private static List<Function<Environment, Expression>> letBindingValues(List<Expression> exps) {
        return exps.get(1).list().value.stream()
                .map(e -> e.list().value.get(1))
                .map(Evaluator::analyze)
                .collect(Collectors.toList());
    }

    private static Function<Environment, Expression> analyzeBegin(List<Expression> exps) {
        return analyzeSequence(rest(exps));
    }

    private static Function<Environment, Expression> analyzeSequence(List<Expression> exps) {
        List<Function<Environment, Expression>> seq = exps.stream()
                .map(Evaluator::analyze)
                .collect(Collectors.toList());

        return env -> seq.stream()
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

    private static Function<Environment, Expression> analyzeProcedure(List<SymbolExpression> names, List<Expression> exps) {
        Function<Environment, Expression> body = analyzeSequence(rest(rest(exps)));
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
        return exp.isList() && SPECIAL_FORMS.contains(exp.list().value.get(0).symbol());
    }

    private static boolean isFunctionCall(Expression exp) {
        return exp.isList() && exp.list().value.size() > 0;
    }

    private static boolean isSelfEvaluating(Expression exp) {
        return exp.isNumber() || exp.isBoolean() || exp == Expression.none() || (exp.isList() && exp.list().value.size() == 0);
    }

    private static boolean isTruthy(Expression exp) {
        return !BooleanExpression.bool(false).equals(exp);
    }
}
