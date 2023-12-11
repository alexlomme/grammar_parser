package grammar.variables;

public class Constant extends Expression {
    private int value;

    public Constant(int value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}
