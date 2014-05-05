package schemette;

import com.google.common.collect.ImmutableSet;
import schemette.environment.Environment;
import schemette.expressions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        throw new IllegalArgumentException(String.format("Unable to evaluate expression '%s'", e));
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
        List<Expression> exps = ((ListExpression) e).value;
        String first = ((SymbolExpression) exps.get(0)).value;

        if (first.equals("quote")) {
            return exps.get(1);
        } else if (first.equals("set!")) {
            environment.set((SymbolExpression) exps.get(1), evaluate(exps.get(2), environment));
            return Expression.none();
        } else if (first.equals("define")) {
            if (exps.get(1) instanceof SymbolExpression) {
                environment.define((SymbolExpression) exps.get(1), evaluate(exps.get(2), environment));
            } else {
                List<Expression> nameAndParams = ((ListExpression) exps.get(1)).value;
                SymbolExpression name = (SymbolExpression) nameAndParams.get(0);
                Expression body = exps.get(2);
                List<Expression> paramNames = nameAndParams.subList(1, nameAndParams.size());

                environment.define(name, makeProcedure(body, environment, paramNames));
            }

            return Expression.none();
        } else if (first.equals("if")) {
            if (truthy(evaluate(exps.get(1), environment))) {
                return evaluate(exps.get(2), environment);
            } else {
                return evaluate(exps.get(3), environment);
            }
        } else if (first.equals("lambda")) {
            return evaluateLambda((ListExpression) exps.get(1), exps.get(2), environment);
        } else if (first.equals("begin")) {
            return exps.stream()
                    .reduce((a, b) -> evaluate(b, environment))
                    .orElse(Expression.none());
        }

        throw new IllegalArgumentException(String.format("Invalid special form expression '%s'", e));
    }

    private static ProcedureExpression evaluateLambda(ListExpression paramNames, Expression body, Environment environment) {
        List<Expression> names = paramNames.value;
        return makeProcedure(body, environment, names);
    }

    private static ProcedureExpression makeProcedure(Expression body, Environment environment, List<Expression> names) {
        return ProcedureExpression.procedure((args) -> {
            Environment lambdaEnvironment = new Environment(new HashMap<>(), environment);
            IntStream.range(0, names.size())
                    .forEach((i) -> lambdaEnvironment.define((SymbolExpression) names.get(i), args.get(i)));
            return evaluate(body, lambdaEnvironment);
        });
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

    private static boolean truthy(Expression evaluate) {
        return !BooleanExpression.bool(false).equals(evaluate);
    }
}
