package schemette;

import org.junit.Test;
import schemette.environment.DefaultEnvironment;
import schemette.environment.Environment;
import schemette.expressions.Expression;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static schemette.expressions.NumberExpression.number;

public class IntegrationTest {

    @Test
    public void factorial() {
        String input = "(define factorial (lambda (n) (if (= n 1) 1 (* n (factorial (- n 1))))))";
        Environment environment = DefaultEnvironment.newInstance();

        Evaluator.evaluate(Reader.read(input), environment);
        Object result = Evaluator.evaluate(Reader.read("(factorial 3)"), environment);

        assertThat(result, is(number(6)));
    }

    @Test
    public void fibonacci() {
        String input = "(define (fib n) (if (< n 2) n (+ (fib (- n 1)) (fib (- n 2)))))";
        Environment environment = DefaultEnvironment.newInstance();

        Evaluator.evaluate(Reader.read(input), environment);
        Expression result = Evaluator.evaluate(Reader.read("(fib 10)"), environment);

        assertThat(result, is(number(55)));

    }
}
