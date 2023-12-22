import grammar.variables.Assignment;
import grammar.variables.Program;
import parser.Analyser;
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

        Program program = null;
        try {
            program = Parser.parse(input.toString());
            System.out.println("Parsed successfully\n");
            List<Assignment> unused = Analyser.analyseAssignments(program);
            System.out.println("Analysed\n");
        } catch (RuntimeException e) {
            System.out.println(e.toString());
        }
    }
}