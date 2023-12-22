package parser;

import grammar.variables.*;

import java.util.*;

public class Analyser {

    public static List<Assignment> analyseAssignments(Program program) {

        List<Assignment> unused = new ArrayList<>();
        Set<Character> defined = new HashSet<>();
        List<Assignment>[] symbolTable = new List[26];

        for (int i = 0; i < 26; i++) {
            symbolTable[i] = new ArrayList<>();
        }

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
            List<Assignment>[] localSymbolTable = new List[26];
            for (int i = 0; i < 26; i++) {
                localSymbolTable[i] = new ArrayList<>();
            }
            Set<Character> localDefined = new HashSet<>();

            Set<Character> assignedBeforeIf = analyseIfBlock(ifBlock, localSymbolTable, localDefined, unused);

            Set<Character> toRemove = new HashSet<>();
            for (Character c : assignedBeforeIf) {
                if (defined.contains(c)) {
                    symbolTable[c - 97].clear();
                    toRemove.add(c);
                }
            }

            assignedBeforeIf.removeAll(toRemove);
            for (int i = 0; i < 26; i++) {
                symbolTable[i].addAll(localSymbolTable[i]);
            }


            return assignedBeforeIf;
        } else if (statement instanceof WhileBlock whileBlock) {
            List<Assignment>[] localSymbolTable = new List[26];
            for (int i = 0; i < 26; i++) {
                localSymbolTable[i] = new ArrayList<>();
            }
            Set<Character> localDefined = new HashSet<>();

            Set<Character> assignedBeforeWhile = analyseWhileBlock(whileBlock, localSymbolTable, localDefined, unused);

            Set<Character> toRemove = new HashSet<>();
            for (Character c : assignedBeforeWhile) {
                if (defined.contains(c)) {
                    symbolTable[c - 97].clear();
                    toRemove.add(c);
                }
            }

            assignedBeforeWhile.removeAll(toRemove);
            for (int i = 0; i < 26; i++) {
                symbolTable[i].addAll(localSymbolTable[i]);
            }


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

        Set<Character> toRemove = new HashSet<>();
        for (Character c : variables) {
            if (defined.contains(c)) {
                toRemove.add(c);
                symbolTable[c - 97].clear();
            }
        }

        unused.addAll(symbolTable[name - 97]);
        symbolTable[name - 97].clear();
        symbolTable[name - 97].add(assignment);

        variables.removeAll(toRemove);
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

        for (Character c : variables) {
            symbolTable[c - 97].clear();
        }

        variables.addAll(usedBeforeBlock);

        return variables;
    }

}
