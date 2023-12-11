package grammar.variables;

import grammar.variables.Expression;

public class IfBlock extends Statement {
    private Expression expression;
    private StatementList statementList;

    public IfBlock(Expression expression, StatementList statementList) {
        this.expression = expression;
        this.statementList = statementList;
    }

    @Override
    public String toString() {
        return "if " + this.expression.toString() + " " + this.statementList.toString() + " end";
    }
}
