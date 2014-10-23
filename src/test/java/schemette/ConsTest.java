package schemette;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import schemette.cons.Cons;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static schemette.cons.Cons.cons;
import static schemette.cons.Cons.empty;

public class ConsTest {
    @Test
    public void map() {
        Cons<Integer> list = cons(1, cons(2, empty()));
        list.stream().map(e -> e + 1).collect(Cons.collector());

        assertThat(list.stream().map(e -> e + 1).collect(Cons.collector()).toList(), is(ImmutableList.of(2, 3)));
    }

}