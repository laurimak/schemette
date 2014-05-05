package schemette;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import schemette.environment.Environment;
import schemette.expressions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Evaluator {

    private static final Set<SymbolExpression> SPECIAL_FORMS = ImmutableSet.of("quote", "set!", "define", "if", "lambda", "begin", "let").stream()
            .map(SymbolExpression::symbol)
            .collect(Collectors.toSet());

    public static Expression evaluate(Expression e, Environment environment) {
        if (isSelfEvaluating(e)) {
            return e;
        } else if (isVariableReference(e)) {
            return environment.lookup((SymbolExpression) e);
        } else if (isSpecialForm(e)) {
            return evaluateSpecialForm(e, environment);
        } else if (isFunctionCall(e)) {
            return evaluateFunctionCall((ListExpression) e, environment);
        }

        throw new IllegalArgumentException(String.format("Unable to evaluate expression '%s'", e));
    }

    private static Expression evaluateSpecialForm(Expression e, Environment environment) {
        List<Expression> exps = ((ListExpression) e).value;

        switch (((SymbolExpression) exps.get(0)).value) {
            case "quote":
                return exps.get(1);
            case "set!":
                return evaluateSet(exps, environment);
            case "define":
                return evaluateDefine(exps, environment);
            case "if":
                return evaluateIf(exps, environment);
            case "lambda":
                return evaluateLambda((ListExpression) exps.get(1), exps.get(2), environment);
            case "begin":
                return evaluateBegin(exps, environment);
            case "let":
                return evaluateLet(exps, environment);
        }

        throw new IllegalArgumentException(String.format("Invalid special form expression '%s'", e));
    }

    private static Expression evaluateFunctionCall(ListExpression e, Environment environment) {
        List<Expression> args = e.value.stream()
                .skip(1)
                .map((p) -> evaluate(p, environment))
                .collect(Collectors.toList());
        return apply((ProcedureExpression) evaluate(e.value.get(0), environment), args);
    }

    private static Expression apply(ProcedureExpression procedure, List<Expression> args) {
        return procedure.lambda.apply(args);
    }

    private static Expression evaluateLet(List<Expression> exps, Environment environment) {
        List<Expression> bindings = ((ListExpression) exps.get(1)).value;
        Map<SymbolExpression, Expression> nameToValue = bindings.stream()
                .map(a -> (ListExpression) a)
                .collect(Collectors.toMap(a -> (SymbolExpression) a.value.get(0), a -> evaluate(a.value.get(1), environment)));

        return lambda(exps.get(2), environment, ImmutableList.copyOf(nameToValue.keySet())).apply(ImmutableList.copyOf(nameToValue.values()));
    }

    private static Expression evaluateBegin(List<Expression> exps, Environment environment) {
        return exps.stream()
                .reduce((a, b) -> evaluate(b, environment))
                .orElse(Expression.none());
    }

    private static Expression evaluateIf(List<Expression> exps, Environment environment) {
        if (isTruthy(evaluate(exps.get(1), environment))) {
            return evaluate(exps.get(2), environment);
        } else if (exps.size() > 3) {
            return evaluate(exps.get(3), environment);
        } else {
            return Expression.none();
        }
    }

    private static Expression evaluateDefine(List<Expression> exps, Environment environment) {
        if (exps.get(1) instanceof SymbolExpression) {
            environment.define((SymbolExpression) exps.get(1), evaluate(exps.get(2), environment));
        } else {
            List<Expression> nameAndParams = ((ListExpression) exps.get(1)).value;
            SymbolExpression name = (SymbolExpression) nameAndParams.get(0);
            List<Expression> paramNames = nameAndParams.subList(1, nameAndParams.size());

            environment.define(name, ProcedureExpression.procedure(lambda(exps.get(2), environment, paramNames)));
        }

        return Expression.none();
    }

    private static Expression evaluateSet(List<Expression> exps, Environment environment) {
        environment.set((SymbolExpression) exps.get(1), evaluate(exps.get(2), environment));
        return Expression.none();
    }

    private static ProcedureExpression evaluateLambda(ListExpression paramNames, Expression body, Environment environment) {
        List<Expression> names = paramNames.value;
        return ProcedureExpression.procedure(lambda(body, environment, names));
    }

    private static Function<List<Expression>, Expression> lambda(Expression body, Environment environment, List<Expression> names) {
        return args ->
                evaluate(body, new Environment(IntStream.range(0, names.size())
                        .mapToObj(Integer::new)
                        .map(i -> (Integer) i)
                        .collect(Collectors.toMap(i -> (SymbolExpression) names.get(i),
                                args::get)), environment));
    }

    private static boolean isSpecialForm(Expression e) {
        return e instanceof ListExpression && SPECIAL_FORMS.contains(((ListExpression) e).value.get(0));
    }

    private static boolean isFunctionCall(Expression e) {
        return e instanceof ListExpression && ((ListExpression) e).value.size() > 0;
    }

    private static boolean isVariableReference(Expression e) {
        return e instanceof SymbolExpression;
    }

    private static boolean isSelfEvaluating(Expression e) {
        return e instanceof NumberExpression || e instanceof BooleanExpression || e == Expression.none();
    }

    private static boolean isTruthy(Expression evaluate) {
        return !BooleanExpression.bool(false).equals(evaluate);
    }
}
