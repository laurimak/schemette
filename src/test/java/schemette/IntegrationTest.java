package schemette;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import schemette.environment.DefaultEnvironment;
import schemette.environment.Environment;
import schemette.exception.UnexpectedExpression;
import schemette.expressions.Expression;
import schemette.expressions.SyntaxRulesExpression;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static schemette.expressions.NumberExpression.number;

public class IntegrationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

    @Test
    public void let() {
        String input = "(let ((x 10) (y 20)) (* x y))";
        Environment environment = DefaultEnvironment.newInstance();

        Expression result = Evaluator.evaluate(Reader.read(input), environment);

        assertThat(result, is(number(200)));
    }

    @Test
    public void eval() {
        String input = "(eval (quote (+ 1 2)))";

        Environment environment = DefaultEnvironment.newInstance();

        Expression result = Evaluator.evaluate(Reader.read(input), environment);

        assertThat(result, is(number(3)));
    }

    @Test
    public void define_function_sequence() {
        String input = "(define (foo) (define a 10) a)";

        Environment environment = DefaultEnvironment.newInstance();

        Evaluator.evaluate(Reader.read(input), environment);
        Expression result = Evaluator.evaluate(Reader.read("(foo)"), environment);

        assertThat(result, is(number(10)));

    }

    @Test
    public void lambda_sequence() {
        String input = "((lambda () (define a 10) a))";

        Environment environment = DefaultEnvironment.newInstance();

        Expression result = Evaluator.evaluate(Reader.read(input), environment);

        assertThat(result, is(number(10)));

    }

    @Test
    public void syntax_rules_expression() {
        String input = "(syntax-rules () ((sqr a) (* a a)))";

        Environment environment = DefaultEnvironment.newInstance();

        SyntaxRulesExpression result = Evaluator.evaluate(Reader.read(input), environment).syntaxRules();

        assertThat(result.keywords, is(emptyList()));
        assertThat(result.patternToTemplate.get(Reader.read("(sqr a)")), is(Reader.read("(* a a)")));
    }

    @Test
    public void syntax_rules_expression2() {
        String input = "((syntax-rules () ((sqr a) (* a a))) 2)";

        Environment environment = DefaultEnvironment.newInstance();

        Expression result = Evaluator.evaluate(Reader.read(input), environment);

        assertThat(result, is(number(4)));
    }



    @Test
    public void unexpected_expression() {
        thrown.expect(UnexpectedExpression.class);
        thrown.expectMessage("Expected expression of type 'ListExpression', got 'symbol(a)'");

        String input = "(lambda a b)";

        Environment environment = DefaultEnvironment.newInstance();

        Evaluator.evaluate(Reader.read(input), environment);
    }
}
