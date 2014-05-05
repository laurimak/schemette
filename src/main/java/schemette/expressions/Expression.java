package schemette.expressions;

public interface Expression {
    static final Expression NONE = new Expression() {};

    public static Expression none() {
        return NONE;
    }

    default ListExpression list() {
        return (ListExpression) this;
    }

    default NumberExpression number() {
        return (NumberExpression) this;
    }

    default ProcedureExpression procedure() {
        return (ProcedureExpression) this;
    }

    default SymbolExpression symbol() {
        return (SymbolExpression) this;
    }
}
