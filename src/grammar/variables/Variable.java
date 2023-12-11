package grammar.variables;

import grammar.variables.Expression;

public class Variable extends Expression {
    private char name;

    public Variable(char name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
