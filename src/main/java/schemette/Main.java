package schemette;

import schemette.environment.DefaultEnvironment;
import schemette.environment.Environment;
import schemette.expressions.Expression;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Environment environment = DefaultEnvironment.newInstance();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(": ");
            String line = scanner.nextLine();
            Expression parse = Parser.parse(line);
            System.out.println(Evaluator.evaluate(parse, environment));
        }
    }
}
