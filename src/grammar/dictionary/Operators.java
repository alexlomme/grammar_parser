package grammar.dictionary;

public enum Operators {
    ADDITION('+'),
    SUBTRACTION('-'),
    PRODUCT('*'),
    DIVISION('/'),
    COMPARISON_GREATER('>'),
    COMPARISON_SMALLER('<');

    public final char label;

    public static final int MAX_PRIORITY = 3;

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

    public static int priority(Operators operator) {
        return switch(operator) {
            case PRODUCT, DIVISION -> 3;
            case ADDITION, SUBTRACTION -> 2;
            default -> 1;
        };
    }
}
