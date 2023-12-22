package grammar.variables;

import grammar.variables.Statement;
import grammar.variables.StatementList;

public class StatementListStatement extends StatementList {
    private StatementList statementList;
    private Statement statement;

    public StatementListStatement(StatementList statementList, Statement statement) {
        this.statementList = statementList;
        this.statement = statement;
    }

    public StatementList getStatementList() {
        return statementList;
    }

    public Statement getStatement() {
        return statement;
    }

    @Override
    public String toString() {
        return this.statementList.toString() + " " + this.statement.toString();
    }
}
