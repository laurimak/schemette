package schemette.environment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import schemette.expressions.Expression;
import schemette.expressions.ListExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static schemette.expressions.SymbolExpression.symbol;

public class PatternMatcher {
    public static Expression expandTemplate(LinkedListMultimap<String, Expression> bindings, Expression template) {
        if (template.isSymbol()) {
            if (bindings.containsKey(template.symbol().value)) {
                return bindings.get(template.symbol().value).get(0);
            }
        } else if (template.isList()) {
            List<Expression> list = template.list().value;
            if (list.contains(symbol("..."))) {
                List<List<Expression>> split = split(list, symbol("..."));
                return join()
            }
            return ListExpression.list(list.stream().map(e -> expandTemplate(bindings, e)).collect(Collectors.toList()));
        }
        return template;
    }

    public static Expression expandEllipsis(LinkedListMultimap<String, Expression> bindings, Expression subTemplate) {
        while (true) {

        }
    }

    public static boolean matches(List<String> literals, Multimap<String, Expression> bindings, Expression pattern, Expression candidate) {
        if (pattern.isSymbol()) {
            String patternSymbol = pattern.symbol().value;
            if (patternSymbol.equals("_")) {
                return true;
            }

            if (!literals.contains(patternSymbol)) {
                bindings.put(patternSymbol, candidate);
                return true;
            }

            if (candidate.equals(pattern)) {
                return true;
            }

        } else if (pattern.isList()) {
            if (candidate.isList()) {
                return matchesSequence(literals, bindings, pattern.list().value, candidate.list().value);
            }
        }
        return false;
    }

    private static boolean matchesSequence(List<String> literals, Multimap<String, Expression> bindings, List<Expression> pattern, List<Expression> candidate) {
        if (pattern.contains(symbol("..."))) {
            return matchSequenceWithEllipsis(literals, bindings, pattern, candidate);

        } else {
            if (pattern.size() != candidate.size()) {
                return false;
            }

            for (int i = 0; i < pattern.size(); i++) {
                if (!matches(literals, bindings, pattern.get(i), candidate.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean matchSequenceWithEllipsis(List<String> literals, Multimap<String, Expression> bindings, List<Expression> pattern, List<Expression> candidate) {
        List<List<Expression>> runs = split(pattern, symbol("..."));

        if (runs.stream()
                .map(e -> e.size())
                .reduce(0, (sum, e) -> sum + e)
                > candidate.size()) {
            return false;
        }

        List<Expression> run1 = runs.get(0);
        if (runs.size() == 1) {
            return matchesSequence(literals, bindings, run1, take(run1.size(), candidate))
                    && matchesSequence(literals, bindings, fillList(candidate.size() - run1.size(), last(run1)), takeLast(candidate.size() - run1.size(), candidate));
        } else {
            List<Expression> run2 = runs.get(1);
            int ellipsisLength = candidate.size() - run1.size() - run2.size();
            return matchesSequence(literals, bindings, run1, take(run1.size(), candidate))
                    && matchesSequence(literals, bindings, fillList(ellipsisLength, last(run1)), candidate.subList(run1.size(), run1.size() + ellipsisLength))
                    && matchesSequence(literals, bindings, run2, takeLast(run2.size(), candidate));
        }
    }

    private static <T> List<List<T>> split(List<T> list, T e) {
        List<List<T>> lists = new ArrayList<>();
        return splitRecur(lists, list, e);
    }

    private static <T> List<List<T>> splitRecur(List<List<T>> sum, List<T> list, T e) {
        if (!list.contains(e)) {
            sum.add(list);
            return sum;
        }

        sum.add(take(list.indexOf(e), list));
        return splitRecur(sum, list.subList(list.indexOf(e) + 1, list.size()), e);
    }

    private static <T> List<T> take(int n, List<T> list) {
        return list.subList(0, Math.min(n, list.size()));
    }

    private static <T> List<T> takeLast(int n, List<T> list) {
        return list.subList(Math.max(0, list.size() - n), list.size());
    }

    private static <T> List<T> fillList(int n, T element) {
        return IntStream.generate(() -> 0)
                .limit(n)
                .mapToObj(a -> element)
                .collect(Collectors.toList());
    }

    private static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }

    private static <T> List<T> join(List<T>... lists) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (List<T> list : lists) {
            builder.addAll(list);
        }

        return builder.build();
    }
}
