package grammar.dictionary;

public enum Operators {
    ADDITION('+'),
    SUBTRACTION('-'),
    PRODUCT('*'),
    DIVISION('/'),
    COMPARISON_GREATER('>'),
    COMPARISON_SMALLER('<');

    public final char label;

    private Operators(char label) {
        this.label = label;
    }

    public static boolean isOperator(char token) {
        return switch (token) {
            case '+', '-', '*', '/', '>', '<' -> true;
            default -> false;
        };
    }

    public static Operators convert(char token) {
        return switch (token) {
            case '+' -> Operators.ADDITION;
            case '-' -> Operators.SUBTRACTION;
            case '*' -> Operators.PRODUCT;
            case '/' -> Operators.DIVISION;
            case '>' -> Operators.COMPARISON_GREATER;
            case '<' -> Operators.COMPARISON_SMALLER;
            default -> null;
        };
    }
}
