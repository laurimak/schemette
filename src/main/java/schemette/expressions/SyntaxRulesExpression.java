package schemette.expressions;

import java.util.List;
import java.util.Map;

public class SyntaxRulesExpression implements Expression {
    public final List<String> keywords;
    public final Map<Expression, Expression> patternToTemplate;

    public SyntaxRulesExpression(List<String> keywords, Map<Expression, Expression> patternToTemplate) {
        this.keywords = keywords;
        this.patternToTemplate = patternToTemplate;
    }

    public static Expression syntaxRules(List<String> literals, Map<Expression,Expression> patternToTemplate) {
        return new SyntaxRulesExpression(literals, patternToTemplate);
    }

    @Override public String toString() {
        return "SyntaxRulesExpression{" +
                "keywords=" + keywords +
                ", patternToTemplate=" + patternToTemplate +
                '}';
    }
}
