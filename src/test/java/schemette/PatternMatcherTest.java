package schemette;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedListMultimap;
import org.junit.Test;
import schemette.expressions.Expression;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static schemette.Reader.read;
import static schemette.environment.PatternMatcher.expandTemplate;
import static schemette.environment.PatternMatcher.matches;
import static schemette.expressions.NumberExpression.number;
import static schemette.expressions.SymbolExpression.symbol;

public class PatternMatcherTest {
    @Test
    public void wildcard() {
        assertThat(matches(ImmutableList.of(), LinkedListMultimap.create(), read("_"), read("foo")), is(true));
    }

    @Test
    public void pattern_variable() {
        LinkedListMultimap<String, Expression> bindings = LinkedListMultimap.create();
        assertThat(matches(ImmutableList.of(), bindings, read("a"), read("foo")), is(true));
        assertThat(bindings.get("a").get(0), is(symbol("foo")));
    }

    @Test
    public void literal_identifier() {
        assertThat(matches(ImmutableList.of("else"), LinkedListMultimap.create(), read("else"), read("else")), is(true));
    }

    @Test
    public void empty_list() {
        assertThat(matches(ImmutableList.of(), LinkedListMultimap.create(), read("()"), read("()")), is(true));
    }

    @Test
    public void one_element_list_with_wildcard() {
        assertThat(matches(ImmutableList.of(), LinkedListMultimap.create(), read("(_)"), read("(a)")), is(true));
    }

    @Test
    public void two_element_list_with_wildcard() {
        LinkedListMultimap<String, Expression> bindings = LinkedListMultimap.create();
        assertThat(matches(ImmutableList.of(), bindings, read("(_ a)"), read("(foo 1")), is(true));
        assertThat(bindings.get("a").get(0), is(number(1)));
    }

    @Test
    public void nested_list() {
        LinkedListMultimap<String, Expression> bindings = LinkedListMultimap.create();
        assertThat(matches(ImmutableList.of(), bindings, read("(_ ((a b) (c d))"), read("(foo ((1 2) (3 4))")), is(true));
        assertThat(bindings.get("a").get(0), is(number(1)));
        assertThat(bindings.get("b").get(0), is(number(2)));
        assertThat(bindings.get("c").get(0), is(number(3)));
        assertThat(bindings.get("d").get(0), is(number(4)));
    }

    @Test
    public void ellipsis() {
        assertThat(matches(ImmutableList.of(), LinkedListMultimap.create(), read("(_ ...)"), read("(foo 1 2 3 4)")), is(true));
    }

    @Test
    public void ellipsis_with_tail() {
        LinkedListMultimap<String, Expression> bindings = LinkedListMultimap.create();
        assertThat(matches(ImmutableList.of(), bindings, read("(_ a b ... c d)"), read("(foo 1 2 3 4)")), is(true));
        assertThat(bindings.get("a").get(0), is(number(1)));
        assertThat(bindings.get("b").get(0), is(number(2)));
        assertThat(bindings.get("c").get(0), is(number(3)));
        assertThat(bindings.get("d").get(0), is(number(4)));
    }

    @Test
    public void ellipsis_with_multiple_bindings() {
        LinkedListMultimap<String, Expression> bindings = LinkedListMultimap.create();
        assertThat(matches(ImmutableList.of(), bindings, read("(_ a ...)"), read("(foo 1 2)")), is(true));
        assertThat(bindings.get("a").get(0), is(number(1)));
        assertThat(bindings.get("a").get(1), is(number(2)));
    }

    @Test
    public void ellipsis_with_multiple_bindings_and_tail() {
        LinkedListMultimap<String, Expression> bindings = LinkedListMultimap.create();
        assertThat(matches(ImmutableList.of(), bindings, read("(_ a ... b)"), read("(foo 1 2 3)")), is(true));
        assertThat(bindings.get("a").get(0), is(number(1)));
        assertThat(bindings.get("a").get(1), is(number(2)));
        assertThat(bindings.get("b").get(0), is(number(3)));
    }

    @Test
    public void list_ellipsis() {
        LinkedListMultimap<String, Expression> bindings = LinkedListMultimap.create();
        assertThat(matches(ImmutableList.of(), bindings, read("((a b) ...)"), read("((1 2) (3 4))")), is(true));
        assertThat(bindings.get("a").get(0), is(number(1)));
        assertThat(bindings.get("a").get(1), is(number(3)));
        assertThat(bindings.get("b").get(0), is(number(2)));
        assertThat(bindings.get("b").get(1), is(number(4)));
    }

    @Test
    public void simple_template() {
        assertThat(expandTemplate(LinkedListMultimap.create(), read("(+ 1 2)")), is(read("(+ 1 2)")));
    }

    @Test
    public void template_with_binding() {
        assertThat(expandTemplate(LinkedListMultimap.create(ImmutableMultimap.of("a", symbol("foo"))), read("(a)")), is(read("(foo)")));
    }

}
