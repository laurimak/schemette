package schemette.expressions;

import schemette.exception.UnexpectedExpression;

public interface Expression {
    static final Expression NONE = new Expression() {
        public String toString() {
            return "none()";
        }
    };

    static Expression none() {
        return NONE;
    }

    default <T extends Expression> T assertExpressionOfType(Class<T> type) {
        if (!type.isAssignableFrom(this.getClass())) {
            throw new UnexpectedExpression(String.format("Expected expression of type '%s', got '%s'", type.getSimpleName(), this));
        }

        return type.cast(this);
    }

    default ListExpression list() {
        return assertExpressionOfType(ListExpression.class);
    }

    default NumberExpression number() {
        return assertExpressionOfType(NumberExpression.class);
    }

    default ProcedureExpression procedure() {
        return assertExpressionOfType(ProcedureExpression.class);
    }

    default SymbolExpression symbol() {
        return assertExpressionOfType(SymbolExpression.class);
    }

    default BooleanExpression bool() {
        return assertExpressionOfType(BooleanExpression.class);
    }
}
