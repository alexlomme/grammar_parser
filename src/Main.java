import grammar.variables.Program;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String inputFilePath = "src/input.txt";
        Path path = Paths.get(inputFilePath);

        StringBuilder input = new StringBuilder();

        try {
            List<String> lines = Files.readAllLines(path);

            for (String line : lines) {
                input.append(line + "\n");
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            return;
        }

        try {
            Program program = Parser.parse(input.toString());
            System.out.println("Parsed successfully");
        } catch (RuntimeException e) {
            System.out.println(e.toString());
        }
    }
}