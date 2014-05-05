package schemette;

import com.google.common.collect.ImmutableSet;
import schemette.environment.Environment;
import schemette.expressions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static schemette.expressions.SymbolExpression.symbol;

public class Evaluator {

    private static final Set<SymbolExpression> SPECIAL_FORMS = ImmutableSet.of("quote", "set!", "define", "if", "lambda", "begin").stream()
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
            return evaluateFunctionCall(e, environment);
        }

        return null;
    }

    private static Expression evaluateFunctionCall(Expression e, Environment environment) {
        ListExpression l = (ListExpression) e;
        Expression operator = evaluate(l.value.get(0), environment);
        List<Expression> paramVals = l.value.stream()
                .skip(1)
                .map((p) -> evaluate(p, environment))
                .collect(Collectors.toList());
        return apply(operator, paramVals);
    }

    private static boolean isSpecialForm(Expression e) {
        return e instanceof ListExpression && SPECIAL_FORMS.contains(((ListExpression) e).value.get(0));
    }

    private static Expression evaluateSpecialForm(Expression e, Environment environment) {
        ListExpression l = (ListExpression) e;
        if (isFirstSymbol(l, "quote")) {
            return l.value.get(1);
        } else if (isFirstSymbol(l, "set!")) {
            environment.set((SymbolExpression) l.value.get(1), evaluate(l.value.get(2), environment));
            return null;
        } else if (isFirstSymbol(l, "define")) {
            environment.define((SymbolExpression) l.value.get(1), evaluate(l.value.get(2), environment));
            return null;
        } else if (isFirstSymbol(l, "if")) {
            if (truthy(evaluate(l.value.get(1), environment))) {
                return evaluate(l.value.get(2), environment);
            } else {
                return evaluate(l.value.get(3), environment);
            }
        } else if (isFirstSymbol(l, "lambda")) {
            return evaluateLambda((ListExpression) l.value.get(1), l.value.get(2), environment);
        } else if (isFirstSymbol(l, "begin")) {
            return l.value.stream()
                    .reduce((a, b) -> evaluate(b, environment))
                    .orElse(null);
        }

        return null;
    }

    private static ProcedureExpression evaluateLambda(ListExpression paramNames, Expression body, Environment environment) {
        return ProcedureExpression.procedure((args) -> {
            Environment lambdaEnvironment = new Environment(new HashMap<>(), environment);
            IntStream.range(0, paramNames.value.size())
                    .forEach((i) -> lambdaEnvironment.define((SymbolExpression) paramNames.value.get(i), args.get(i)));
            return evaluate(body, lambdaEnvironment);
        });
    }

    private static boolean isFirstSymbol(ListExpression l, String string) {
        return l.value.get(0).equals(symbol(string));
    }

    private static boolean isFunctionCall(Expression e) {
        return e instanceof ListExpression && ((ListExpression) e).value.size() > 0;
    }

    private static boolean isVariableReference(Expression e) {
        return e instanceof SymbolExpression;
    }

    private static boolean isSelfEvaluating(Expression e) {
        return e instanceof NumberExpression || e instanceof BooleanExpression;
    }

    private static Expression apply(Expression procedure, List<Expression> paramVals) {
        ProcedureExpression primitiveProcedure = (ProcedureExpression) procedure;
        return primitiveProcedure.lambda.apply(paramVals);
    }

    private static boolean truthy(Object evaluate) {
        return !BooleanExpression.bool(false).equals(evaluate);
    }
}
