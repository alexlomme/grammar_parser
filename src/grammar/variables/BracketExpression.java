package grammar.variables;

public class BracketExpression extends Expression {
    private Expression expression;

    public BracketExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "( " + this.expression.toString() + " )";
    }
}
