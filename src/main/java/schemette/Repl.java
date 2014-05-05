package schemette;

import schemette.environment.DefaultEnvironment;
import schemette.environment.Environment;
import schemette.expressions.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static schemette.expressions.BooleanExpression.bool;

public class Repl {
    public static void main(String[] args) {
        repl(System.in, System.out);
    }

    private static void repl(InputStream in, PrintStream out) {
        Scanner scanner = new Scanner(in);
        Environment environment = DefaultEnvironment.newInstance();
        while (true) {
            out.print(" > ");

            try {
                Expression parse = Reader.read(scanner.nextLine());
                Expression result = Evaluator.evaluate(parse, environment);
                String output = print(result);

                if (!output.isEmpty()) {
                    out.println("->" + output);
                }
            } catch (Throwable t) {
                out.println("Error: " + t.getMessage());
            }
        }
    }

    private static String print(Expression expression) {
        if (expression instanceof BooleanExpression) {
            return bool(true).equals(expression) ? "#t" : "#f";
        }

        if (expression instanceof ListExpression) {
            return ((ListExpression) expression).value.stream()
                    .map(Repl::print)
                    .reduce((a, b) -> a + " " + b)
                    .map(a -> "(" + a + ")")
                    .orElse("()");
        }

        if (expression instanceof NumberExpression) {
            return Long.toString(((NumberExpression) expression).value);
        }

        if (expression instanceof ProcedureExpression) {
            return "#<Procedure>";
        }

        if (expression instanceof SymbolExpression) {
            return ((SymbolExpression) expression).value;
        }

        return "";
    }
}
