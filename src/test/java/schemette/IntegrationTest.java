package schemette;

import org.junit.Test;
import schemette.environment.DefaultEnvironment;
import schemette.environment.Environment;
import schemette.expressions.NumberExpression;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static schemette.expressions.NumberExpression.number;

public class IntegrationTest {

    @Test
    public void factorial() {
        String input = "(define factorial (lambda (n) (if (= n 1) 1 (* n (factorial (- n 1))))))";
        Environment environment = DefaultEnvironment.newInstance();

        Evaluator.evaluate(Parser.parse(input), environment);
        Object result = Evaluator.evaluate(Parser.parse("(factorial 3)"), environment);

        assertThat((NumberExpression) result, is(number(6)));
    }
}
