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

    public static Expression evaluate(Expression exp, Environment env) {
        if (isSelfEvaluating(exp)) {
            return exp;
        } else if (isVariableReference(exp)) {
            return env.lookup(exp.symbol());
        } else if (isSpecialForm(exp)) {
            return evaluateSpecialForm(exp.list(), env);
        } else if (isFunctionCall(exp)) {
            return evaluateFunctionCall(exp.list(), env);
        }

        throw new IllegalArgumentException(String.format("Unable to evaluate expression '%s'", exp));
    }

    private static Expression evaluateSpecialForm(ListExpression exp, Environment env) {
        List<Expression> exps = exp.value;
        switch (exps.get(0).symbol().value) {
            case "quote":
                return exps.get(1);
            case "set!":
                return evaluateSet(exps, env);
            case "define":
                return evaluateDefine(exps, env);
            case "if":
                return evaluateIf(exps, env);
            case "lambda":
                return evaluateLambda(exps.get(1).list(), exps.get(2), env);
            case "begin":
                return evaluateBegin(exps, env);
            case "let":
                return evaluateLet(exps, env);
        }

        throw new IllegalArgumentException(String.format("Invalid special form expression '%s'", exp));
    }

    private static Expression evaluateFunctionCall(ListExpression exp, Environment env) {
        List<Expression> args = exp.value.stream()
                .skip(1)
                .map((p) -> evaluate(p, env))
                .collect(Collectors.toList());
        return apply((ProcedureExpression) evaluate(exp.value.get(0), env), args);
    }

    private static Expression apply(ProcedureExpression procedure, List<Expression> args) {
        return procedure.lambda.apply(args);
    }

    private static Expression evaluateLet(List<Expression> exps, Environment env) {
        Map<SymbolExpression, Expression> bindings = exps.get(1).list().value.stream()
                .map(Expression::list)
                .collect(Collectors.toMap(a -> a.value.get(0).symbol(),
                        a -> evaluate(a.value.get(1), env)));

        return lambda(ImmutableList.copyOf(bindings.keySet()), exps.get(2), env)
                .apply(ImmutableList.copyOf(bindings.values()));
    }

    private static Expression evaluateBegin(List<Expression> exps, Environment env) {
        return exps.stream()
                .reduce((a, b) -> evaluate(b, env))
                .orElse(Expression.none());
    }

    private static Expression evaluateIf(List<Expression> exps, Environment env) {
        if (isTruthy(evaluate(exps.get(1), env))) {
            return evaluate(exps.get(2), env);
        } else if (exps.size() > 3) {
            return evaluate(exps.get(3), env);
        } else {
            return Expression.none();
        }
    }

    private static Expression evaluateDefine(List<Expression> exps, Environment env) {
        if (exps.get(1) instanceof SymbolExpression) {
            env.define(exps.get(1).symbol(), evaluate(exps.get(2), env));
        } else {
            SymbolExpression name = exps.get(1).list().value.get(0).symbol();
            List<Expression> nameAndParams = exps.get(1).list().value;
            List<Expression> paramNames = nameAndParams.subList(1, nameAndParams.size());

            env.define(name, ProcedureExpression.procedure(lambda(paramNames, exps.get(2), env)));
        }

        return Expression.none();
    }

    private static Expression evaluateSet(List<Expression> exps, Environment env) {
        env.set(exps.get(1).symbol(), evaluate(exps.get(2), env));
        return Expression.none();
    }

    private static ProcedureExpression evaluateLambda(ListExpression paramNames, Expression body, Environment env) {
        return ProcedureExpression.procedure(lambda(paramNames.value, body, env));
    }

    private static Function<List<Expression>, Expression> lambda(List<Expression> names, Expression body, Environment env) {
        return args -> evaluate(body, new Environment(makeMap(names, args), env));
    }

    private static Map<SymbolExpression, Expression> makeMap(List<Expression> names, List<Expression> args) {
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
        return exp instanceof NumberExpression || exp instanceof BooleanExpression || exp == Expression.none();
    }

    private static boolean isTruthy(Expression exp) {
        return !BooleanExpression.bool(false).equals(exp);
    }
}
