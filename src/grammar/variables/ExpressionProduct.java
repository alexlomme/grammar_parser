package grammar.variables;

import grammar.variables.Expression;

import java.util.Set;

public class ExpressionProduct extends Expression {
    private Expression expression1;
    Operator operator;
    private Expression expression2;

    public ExpressionProduct(Expression expression1, Expression expression2, Operator operator) {
        this.expression1 = expression1;
        this.expression2 = expression2;
        this.operator = operator;
    }

    public Set<Character> getVariables() {
        Set<Character> set1 = expression1.getVariables();
        Set<Character> set2 = expression2.getVariables();
        set1.addAll(set2);
        return set1;
    }

    public Expression getExpression1() {
        return expression1;
    }

    public Expression getExpression2() {
        return expression2;
    }

    @Override
    public String toString() {
        return this.expression1.toString() + " " + operator.toString() + " " + expression2.toString();
    }
}
