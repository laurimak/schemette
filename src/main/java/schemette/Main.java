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
            Expression parse = Reader.read(scanner.nextLine());
            Expression result = Evaluator.evaluate(parse, environment);
            System.out.println(result);
        }
    }
}
