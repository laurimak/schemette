package schemette.environment;

import schemette.VariableNotDefinedException;
import schemette.expressions.Expression;
import schemette.expressions.SymbolExpression;

import java.util.Map;

public class Environment {
    public final Map<SymbolExpression, Expression> bindings;
    public final Environment enclosingEnvironment;

    public Environment(Map<SymbolExpression, Expression> bindings, Environment enclosingEnvironment) {
        this.bindings = bindings;
        this.enclosingEnvironment = enclosingEnvironment;
    }

    public Environment(Map<SymbolExpression, Expression> bindings) {
        this(bindings, null);
    }


    public Expression lookup(SymbolExpression symbol) {
        if (bindings.containsKey(symbol)) {
            return bindings.get(symbol);
        } else if (enclosingEnvironment != null) {
            return enclosingEnvironment.lookup(symbol);
        }

        throw new VariableNotDefinedException(symbol.toString());
    }

    public void set(SymbolExpression symbol, Expression value) {
        if (bindings.containsKey(symbol)) {
            bindings.put(symbol, value);
        } else if (enclosingEnvironment != null) {
            enclosingEnvironment.set(symbol, value);
        } else {
            throw new VariableNotDefinedException(symbol.toString());
        }
    }

    public void define(SymbolExpression symbol, Expression value) {
        bindings.put(symbol, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Environment that = (Environment) o;

        if (bindings != null ? !bindings.equals(that.bindings) : that.bindings != null) {
            return false;
        }
        if (enclosingEnvironment != null ? !enclosingEnvironment.equals(that.enclosingEnvironment) : that.enclosingEnvironment != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = bindings != null ? bindings.hashCode() : 0;
        result = 31 * result + (enclosingEnvironment != null ? enclosingEnvironment.hashCode() : 0);
        return result;
    }
}
