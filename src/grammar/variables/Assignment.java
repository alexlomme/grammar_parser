package grammar.variables;

public class Assignment extends Statement {
    private Variable variable;
    private Expression expression;

    public Assignment(Variable variable, Expression expression) {
        this.variable = variable;
        this.expression = expression;
    }

    public Variable getVariable() {
        return variable;
    }

    public Expression getExpression() {
        return expression;
    }
    @Override
    public String toString() {
        return this.variable.toString() + " = " + this.expression.toString();
    }
}
