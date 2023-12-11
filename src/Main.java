import grammar.variables.Program;
import parser.Parser;

public class Main {
    public static void main(String[] args) {
        String input = "a = ((b + (4 + c) * 2)/8 + (k*v))";

        try {
            Program program = Parser.parse(input);
            System.out.println("Parsed successfully");
        } catch (RuntimeException e) {
            System.out.println(e.toString());
        }
    }
}