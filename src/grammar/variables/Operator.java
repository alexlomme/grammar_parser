package grammar.variables;

import grammar.dictionary.Operators;

public class Operator implements GrammarVariable {
    private final Operators operator;

    public Operator(Operators operator) {
        this.operator = operator;
    }

    public String toString() {
        return "" + operator.label;
    }
}
