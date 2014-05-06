package schemette;

import schemette.exception.UnmatchedParenthesisExpection;
import schemette.expressions.Expression;
import schemette.expressions.ListExpression;
import schemette.expressions.NumberExpression;
import schemette.expressions.SymbolExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Reader {
    public static List<String> tokenize(String input) {
        return Arrays.stream(input
                .replace("(", " ( ")
                .replace(")", " ) ")
                .split(" "))
                .map(String::trim)
                .filter((s) -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static Expression read(String input) {
        List<Expression> exps = parseSequence(tokenize(input).iterator());

        if (exps.isEmpty()) {
            return Expression.none();
        }

        if (exps.size() == 1) {
            return exps.get(0);
        }

        return ListExpression.list(exps);
    }

    public static int countOpenParens(String input) {
        return tokenize(input).stream()
                .filter(t -> t.equals("(") || t.equals(")"))
                .map(t -> t.equals("(") ? 1 : -1)
                .reduce(0, (a, b) -> {
                    if (a + b < 0) {
                        throw new UnmatchedParenthesisExpection("Too many closed parenthesis ')'");
                    }
                    return a + b;
                });
    }

    private static List<Expression> parseSequence(Iterator<String> i) {
        List<Expression> result = new ArrayList<>();
        while (i.hasNext()) {
            String token = i.next();
            if (")".equals(token)) {
                return result;
            }

            if ("(".equals(token)) {
                result.add(ListExpression.list(parseSequence(i)));
            } else {
                result.add(symbolOrNumber(token));
            }
        }

        return result;
    }

    private static Expression symbolOrNumber(String token) {
        try {
            return NumberExpression.number(Long.parseLong(token));
        } catch (NumberFormatException e) {
            return SymbolExpression.symbol(token);
        }
    }
}
