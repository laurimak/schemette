package schemette.environment;

import schemette.exception.VariableNotDefinedException;
import schemette.expressions.Expression;
import schemette.expressions.SymbolExpression;

import java.util.Map;
import java.util.Optional;

public class Environment {
    public final Map<SymbolExpression, Expression> bindings;
    public final Optional<Environment> enclosingEnvironment;

    private Environment(Map<SymbolExpression, Expression> bindings, Optional<Environment> enclosingEnvironment) {
        this.bindings = bindings;
        this.enclosingEnvironment = enclosingEnvironment;
    }

    public Environment(Map<SymbolExpression, Expression> bindings) {
        this(bindings, Optional.empty());
    }

    public Environment extend(Map<SymbolExpression, Expression> bindings) {
        return new Environment(bindings, Optional.of(this));
    }

    public Expression lookup(SymbolExpression symbol) {
        return Optional.ofNullable(bindings.get(symbol))
                .orElseGet(() -> enclosingEnvironment
                        .map(e -> e.lookup(symbol))
                        .orElseThrow(() -> new VariableNotDefinedException(symbol.value)));
    }

    public void set(SymbolExpression symbol, Expression value) {
        if (bindings.containsKey(symbol)) {
            bindings.put(symbol, value);
        } else {
            enclosingEnvironment
                    .orElseThrow(() -> new VariableNotDefinedException(symbol.value))
                    .set(symbol, value);
        }
    }

    public void define(SymbolExpression symbol, Expression value) {
        bindings.put(symbol, value);
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }

        Environment that = (Environment) o;

        return !(!bindings.equals(that.bindings) || !enclosingEnvironment.equals(that.enclosingEnvironment));
    }

    @Override
    public int hashCode() {
        return 31 * bindings.hashCode() + enclosingEnvironment.hashCode();
    }
}
