package grammar.variables;

import java.util.Set;

public abstract class Expression implements GrammarVariable {
    public abstract Set<Character> getVariables();
}
