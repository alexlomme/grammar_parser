package grammar.variables;

import grammar.variables.Expression;

public class ExpressionProduct extends Expression {
    private Expression expression1;
    Operator operator;
    private Expression expression2;

    public ExpressionProduct(Expression expression1, Expression expression2, Operator operator) {
        this.expression1 = expression1;
        this.expression2 = expression2;
        this.operator = operator;
    }

    @Override
    public String toString() {
        return this.expression1.toString() + " " + operator.toString() + " " + expression2.toString();
    }
}
