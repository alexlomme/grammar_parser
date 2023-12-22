package grammar.variables;

import grammar.variables.Expression;
import grammar.variables.Statement;
import grammar.variables.StatementList;

public class WhileBlock extends Statement {
    private Expression expression;
    private StatementList statementList;

    public WhileBlock(Expression expression, StatementList statementList) {
        this.expression = expression;
        this.statementList = statementList;
    }

    public Expression getExpression() {
        return expression;
    }

    public StatementList getStatementList() {
        return statementList;
    }
    @Override
    public String toString() {
        return "while " + this.expression.toString() + " " + this.statementList.toString() + " end";
    }


}
