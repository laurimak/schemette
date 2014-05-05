package schemette;

import schemette.expressions.Expression;
import schemette.expressions.ListExpression;
import schemette.expressions.NumberExpression;
import schemette.expressions.SymbolExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {
    public static List<String> tokenize(String input) {
        return Arrays.stream(input
                .replace("(", " ( ")
                .replace(")", " ) ")
                .split(" "))
                .map(String::trim)
                .filter((s) -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static Expression parse(String input) {
        List<String> tokens = tokenize(input);
        List<Expression> exps = parseSequence(tokens.iterator());
        if (exps.size() == 1) {
            return exps.get(0);
        }
        return ListExpression.valueOf(exps);
    }

    private static List<Expression> parseSequence(Iterator<String> i) {
        List<Expression> result = new ArrayList<>();
        while (i.hasNext()) {
            String token = i.next();
            if ("(".equals(token)) {
                result.add(ListExpression.valueOf(parseSequence(i)));
            } else if (")".equals(token)) {
                return result;
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
