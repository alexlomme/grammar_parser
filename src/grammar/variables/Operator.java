package grammar.variables;

import grammar.dictionary.Operators;

public class Operator implements GrammarVariable {
    private final Operators operator;

    public Operator(Operators operator) {
        this.operator = operator;
    }

    public int priority() {
        return Operators.priority(this.operator);
    }

    public String toString() {
        return "" + operator.label;
    }
}
