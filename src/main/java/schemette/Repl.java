package schemette;

import schemette.environment.DefaultEnvironment;
import schemette.environment.Environment;
import schemette.expressions.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Repl {
    public static void main(String[] args) {
        repl(System.in, System.out);
    }

    private static void repl(InputStream in, PrintStream out) {
        Scanner scanner = new Scanner(in);
        Environment environment = DefaultEnvironment.newInstance();

        String input = "";
        prompt(out, 0);

        while (true) {
            try {
                input += " " + scanner.nextLine();
                if (completeExpression(input)) {
                    Expression result = Evaluator.evaluate(Reader.read(input), environment);
                    out.print(print(result));

                    input = "";
                }

            } catch (Throwable t) {
                out.println("Error: " + t.getMessage());
                input = "";
            }

            prompt(out, Reader.countOpenParens(input));
        }
    }

    private static boolean completeExpression(String input) {
        return Reader.countOpenParens(input) == 0;
    }

    private static void prompt(PrintStream out, int openParenCount) {
        if (openParenCount == 0) {
            out.print(" > ");
        } else {
            indent(out, openParenCount);
        }
    }

    private static void indent(PrintStream out, int openParenCount) {
        out.print("... ");
        IntStream.range(0, openParenCount).forEach(i -> out.print("  "));
    }

    private static String print(Expression expression) {
        String exp = expressionToString(expression);
        if (exp.isEmpty()) {
            return "";
        }
        return String.format("-> %s\n", exp);
    }

    private static String expressionToString(Expression expression) {
        if (expression instanceof BooleanExpression) {
            return expression.bool().value ? "#t" : "#f";
        }

        if (expression instanceof ListExpression) {
            return expression.list().value.stream()
                    .map(Repl::expressionToString)
                    .reduce((a, b) -> a + " " + b)
                    .map(a -> "(" + a + ")")
                    .orElse("()");
        }

        if (expression instanceof NumberExpression) {
            return Long.toString(expression.number().value);
        }

        if (expression instanceof ProcedureExpression) {
            return "#<Procedure>";
        }

        if (expression instanceof SymbolExpression) {
            return expression.symbol().value;
        }

        return "";
    }
}
