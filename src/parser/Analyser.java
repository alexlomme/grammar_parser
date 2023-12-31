package parser;

import grammar.variables.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Analyser {
    private static final int OFFSET = 97;
    private static final int NUMBER_OF_SYMBOLS = 26;

    private Analyser() {}

    public static List<Assignment> analyseAssignments(Program program) {

        List<Assignment> unused = new ArrayList<>();
        Set<Character> defined = new HashSet<>();
        List<Assignment>[] symbolTable = initializeSymbolTable();

        analyseStatementList((StatementList) program, symbolTable, defined, unused);

        Arrays.stream(symbolTable).forEach(unused::addAll);

        return unused;
    }

    private static Set<Character> analyseStatementList(StatementList program, List<Assignment>[] symbolTable, Set<Character> defined, List<Assignment> unused) {
        if (program instanceof Statement statement) {
            return analyseStatement(statement, symbolTable, defined, unused);
        } else if (program instanceof StatementListStatement statementListStatement) {
            return analyseList(statementListStatement, symbolTable, defined, unused);
        }
        return new HashSet<>();
    }

    private static Set<Character> analyseStatement(Statement statement, List<Assignment>[] symbolTable, Set<Character> defined, List<Assignment> unused) {

        if (statement instanceof Assignment assignment) {
            return analyseAssignment(assignment, symbolTable, defined, unused);
        } else if (statement instanceof IfBlock ifBlock) {
            List<Assignment>[] localSymbolTable = initializeSymbolTable();
            Set<Character> localDefined = new HashSet<>();

            Set<Character> assignedBeforeIf = analyseIfBlock(ifBlock, localSymbolTable, localDefined, unused);

            assignedBeforeIf = assignedBeforeIf.stream().filter(var -> {
                if (defined.contains(var)) {
                    symbolTable[indexOfSymbol(var)].clear();
                    return true;
                }
                return false;
            }).collect(Collectors.toSet());

            IntStream.range(0, NUMBER_OF_SYMBOLS)
                    .forEach(index -> symbolTable[index].addAll(localSymbolTable[index]));


            return assignedBeforeIf;
        } else if (statement instanceof WhileBlock whileBlock) {
            List<Assignment>[] localSymbolTable = initializeSymbolTable();
            Set<Character> localDefined = new HashSet<>();

            Set<Character> assignedBeforeWhile = analyseWhileBlock(whileBlock, localSymbolTable, localDefined, unused);

            assignedBeforeWhile = assignedBeforeWhile.stream().filter(var -> {
                if (defined.contains(var)) {
                    symbolTable[indexOfSymbol(var)].clear();
                    return true;
                }
                return false;
            }).collect(Collectors.toSet());

            IntStream.range(0, NUMBER_OF_SYMBOLS)
                    .forEach(index -> symbolTable[index].addAll(localSymbolTable[index]));

            return assignedBeforeWhile;
        }
        return new HashSet<>();
    }

    private static Set<Character> analyseList(StatementListStatement list, List<Assignment>[] symbolTable, Set<Character> defined, List<Assignment> unused) {
        StatementList newList = list.getStatementList();
        Statement statement = list.getStatement();

        Set<Character> assignedBeforeList = analyseStatementList(newList, symbolTable, defined, unused);

        Set<Character> assignedBeforeStatement = analyseStatement(statement, symbolTable, defined, unused);

        assignedBeforeList.addAll(assignedBeforeStatement);

        return assignedBeforeList;
    }

    private static Set<Character> analyseAssignment(Assignment assignment, List<Assignment>[] symbolTable, Set<Character> defined, List<Assignment> unused) {
        Set<Character> variables = assignment.getExpression().getVariables();
        char name = assignment.getVariable().getName();

        variables = variables.stream().filter(var -> {
            if (defined.contains(var)) {
                symbolTable[indexOfSymbol(var)].clear();
                return true;
            }
            return false;
        }).collect(Collectors.toSet());

        int index = indexOfSymbol(name);
        unused.addAll(symbolTable[index]);
        symbolTable[index].clear();
        symbolTable[index].add(assignment);

        defined.add(name);

        return variables;
    }

    private static Set<Character> analyseIfBlock(IfBlock ifBlock, List<Assignment>[] symbolTable, Set<Character> defined, List<Assignment> unused) {
        Set<Character> variables = ifBlock.getExpression().getVariables();
        StatementList listInBlock = ifBlock.getStatementList();

        Set<Character> usedBeforeBlock = analyseStatementList(listInBlock, symbolTable, defined, unused);

        variables.addAll(usedBeforeBlock);

        return variables;
    }

    private static Set<Character> analyseWhileBlock(WhileBlock whileBlock, List<Assignment>[] symbolTable, Set<Character> defined, List<Assignment> unused) {
        StatementList listInBlock = whileBlock.getStatementList();
        Set<Character> variables = whileBlock.getExpression().getVariables();

        Set<Character> usedBeforeBlock = analyseStatementList(listInBlock, symbolTable, defined, unused);

        variables.forEach(var -> symbolTable[indexOfSymbol(var)].clear());

        variables.addAll(usedBeforeBlock);

        return variables;
    }

    private static int indexOfSymbol(Character c) {
        return c - OFFSET;
    }

    private static List<Assignment>[] initializeSymbolTable() {
        List<Assignment>[] symbolTable = new List[NUMBER_OF_SYMBOLS];
        for (int i = 0; i < NUMBER_OF_SYMBOLS; i++) {
            symbolTable[i] = new ArrayList<>();
        }
        return symbolTable;
    }

}
