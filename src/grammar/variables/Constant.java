package grammar.variables;

import java.util.HashSet;
import java.util.Set;

public class Constant extends Expression {
    private int value;

    public Constant(int value) {
        this.value = value;
    }

    public Set<Character> getVariables() {
        return new HashSet<>();
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}
