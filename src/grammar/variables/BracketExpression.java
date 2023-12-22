package grammar.variables;

import java.util.Set;

public class BracketExpression extends Expression {
    private Expression expression;

    public BracketExpression(Expression expression) {
        this.expression = expression;
    }

    public Set<Character> getVariables() {
        return expression.getVariables();
    }

    @Override
    public String toString() {
        return "( " + this.expression.toString() + " )";
    }
}
