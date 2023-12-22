package grammar.variables;

import grammar.variables.Expression;

import java.util.HashSet;
import java.util.Set;

public class Variable extends Expression {
    private char name;

    public Variable(char name) {
        this.name = name;
    }

    public Set<Character> getVariables() {
        HashSet<Character> set = new HashSet<>();
        set.add(name);
        return set;
    }

    public char getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
