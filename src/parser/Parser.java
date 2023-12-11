package parser;

import grammar.dictionary.*;
import grammar.variables.*;

import java.util.Map;
import java.util.Stack;

public class Parser {
    public static Program parse(String input) {
        String[] tokens = tokenize(input);

        Stack<GrammarVariable> parseStack = new Stack<>();
        Stack<StackAlphabet> stateStack = new Stack<>();
        ParseState state = ParseState.INITIAL;

        for (String token : tokens) {
            state = parseToken(state, token, parseStack, stateStack);
        }

        GrammarVariable expectedList = parseStack.pop();
        StackAlphabet expectedState = stateStack.pop();

        if (expectedList instanceof StatementList list && parseStack.empty() && expectedState == StackAlphabet.LIST && stateStack.empty()) {
            return list;
        } else {
            throw new RuntimeException("Incorrect stack content\n");
        }
    }

    private static ParseState parseToken(ParseState currentState, String token, Stack<GrammarVariable> parseStack, Stack<StackAlphabet> stateStack) {
        Map<String, Words> dictionary = LanguageDictionary.dictionary;

        String fToken = formatToken(token);
        ParseState newState = currentState;

        GrammarVariable newTokenOnStack = null;
        StackAlphabet newVarOnStack = null;

        switch(currentState) {
            case INITIAL: {
                switch(dictionary.get(fToken)) {
                    case VAR: {
                        newTokenOnStack = new Variable(token.charAt(0));
                        newVarOnStack = StackAlphabet.ASSIGNMENT;
                        newState = ParseState.ASSIGNMENT;
                        break;
                    }
                    case WHILE: {
                        newVarOnStack = StackAlphabet.WHILE_BLOCK;
                        newState = ParseState.EXPRESSION_HOLDER;
                        break;
                    }
                    case IF: {
                        newVarOnStack = StackAlphabet.BLOCK;
                        newState = ParseState.EXPRESSION_HOLDER;
                        break;
                    }
                    case END: {
                        newState = ParseState.END_LIST;
                        break;
                    }
                    case PROGRAM_END: {
                        newState = ParseState.PROGRAM_END;
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". grammar.variables.Statement expected\n");
                    }
                }
                break;
            }
            case ASSIGNMENT: {
                if (dictionary.get(fToken) == Words.EQ) {
                    newState = ParseState.EXPRESSION_HOLDER;
                } else {
                    throw new RuntimeException("Incorrect token " + fToken + ". = expected\n");
                }
                break;
            }
            case EXPRESSION_HOLDER: {
                switch(dictionary.get(fToken)) {
                    case NUM: {
                        newTokenOnStack = new Constant(Integer.parseInt(token));
                        newVarOnStack = StackAlphabet.EXPRESSION;
                        newState = ParseState.EXPRESSION;
                        break;
                    }
                    case VAR: {
                        newTokenOnStack = new Variable(token.charAt(0));
                        newVarOnStack = StackAlphabet.EXPRESSION;
                        newState = ParseState.EXPRESSION;
                        break;
                    }
                    case LB: {
                        stateStack.push(StackAlphabet.BRACKET);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". grammar.variables.Expression expected\n");
                    }
                }
                break;
            }
            case EXPRESSION: {
                switch(dictionary.get(fToken)) {
                    case OP: {
                        newTokenOnStack = new Operator(Operators.convert(token.charAt(0)));
                        newState = ParseState.OPERATOR;
                        break;
                    }
                    case VAR: {
                        newTokenOnStack = new Variable(token.charAt(0));
                        newVarOnStack = StackAlphabet.ASSIGNMENT;
                        newState = ParseState.NEW_ASSIGNMENT;
                        break;
                    }
                    case RB: {
                        newState = ParseState.RIGHT_BRACKET;
                        break;
                    }
                    case IF: {
                        newVarOnStack = StackAlphabet.BLOCK;
                        newState = ParseState.NEW_BLOCK;
                        break;
                    }
                    case WHILE: {
                        newVarOnStack = StackAlphabet.WHILE_BLOCK;
                        newState = ParseState.NEW_BLOCK;
                        break;
                    }
                    case END: {
                        newState = ParseState.END;
                        break;
                    }
                    case PROGRAM_END: {
                        newState = ParseState.PROGRAM_END;
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". grammar.variables.Operator, new statement or end expected\n");
                    }
                }
                break;
            }
            case OPERATOR: {
                switch(dictionary.get(fToken)) {
                    case NUM: {
                        parseStack.push(new Constant(Integer.parseInt(token)));
                        newState = ParseState.OPERATOR_EXPRESSION;
                        break;
                    }
                    case VAR: {
                        parseStack.push(new Variable(token.charAt(0)));
                        newState = ParseState.OPERATOR_EXPRESSION;
                        break;
                    }
                    case LB: {
                        newVarOnStack = StackAlphabet.BRACKET;
                        newState = ParseState.EXPRESSION_HOLDER;
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". grammar.variables.Expression after operator expected\n");
                    }
                }
                break;
            }
            default: {
                throw new RuntimeException("Internal error. False state reached: " + currentState);
            }
        }
        newState = reduce(newState, parseStack, stateStack);

        if (newTokenOnStack != null) {
            parseStack.push(newTokenOnStack);
        }
        if (newVarOnStack != null) {
            stateStack.push(newVarOnStack);
        }

        return newState;
    }

    private static ParseState reduce(ParseState state, Stack<GrammarVariable> parseStack, Stack<StackAlphabet> stateStack) {
        if (!reducible(state)) {
            return state;
        }
        ParseState newState = state;

        switch(state) {
            case NEW_ASSIGNMENT: {
                newState = ParseState.ASSIGNMENT;

                if (stateStack.size() < 2) {
                    throw new RuntimeException("Reduce error: stack smaller than expected\n");
                }

                stateStack.pop();
                StackAlphabet lastAction = stateStack.pop();
                switch (lastAction) {
                    case ASSIGNMENT: {

                        if (parseStack.size() < 2) {
                            throw new RuntimeException("Parse error: stack smaller than expected\n");
                        }

                        GrammarVariable expectedExpression = parseStack.pop();
                        GrammarVariable expectedVar = parseStack.pop();
                        if (expectedExpression instanceof Expression expression && expectedVar instanceof Variable var) {
                            Assignment assignment = new Assignment(var, expression);
                            mergeWithPreviousStatement(assignment, parseStack, stateStack);
                        } else {
                            throw new RuntimeException("Internal error: Incorrect stack trace for assignment. " + expectedExpression.toString() + " " + expectedVar.toString() + "\n");
                        }
                        break;
                    }
                    case WHILE_BLOCK: {
                        stateStack.push(StackAlphabet.WHILE_BLOCK);
                        break;
                    }
                    case BLOCK: {
                        stateStack.push(StackAlphabet.BLOCK);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect state stack trace. grammar.variables.Statement expected, got: " + lastAction + "\n");
                    }
                }
                break;
            }
            case NEW_BLOCK: {
                newState = ParseState.EXPRESSION_HOLDER;

                if (stateStack.size() < 2) {
                    throw new RuntimeException("Internal error: reduce stack smaller than expected\n");
                }

                stateStack.pop();
                StackAlphabet lastAction = stateStack.pop();
                switch (lastAction) {
                    case ASSIGNMENT: {

                        if (parseStack.size() < 2) {
                            throw new RuntimeException("Internal error: parse stack smaller than expected\n");
                        }

                        GrammarVariable expectedExpression = parseStack.pop();
                        GrammarVariable expectedVar = parseStack.pop();
                        if (expectedExpression instanceof Expression expression && expectedVar instanceof Variable var) {
                            Assignment assignment = new Assignment(var, expression);
                            mergeWithPreviousStatement(assignment, parseStack, stateStack);
                        } else {
                            throw new RuntimeException("Incorrect stack trace for assignment. " + expectedExpression.toString() + " " + expectedVar.toString() + "\n");
                        }
                        break;
                    }
                    case WHILE_BLOCK: {
                        stateStack.push(StackAlphabet.WHILE_BLOCK);
                        break;
                    }
                    case BLOCK: {
                        stateStack.push(StackAlphabet.BLOCK);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Parse failed" + "\n");
                    }
                }
                break;
            }
            case END: {
                newState = ParseState.INITIAL;

                if (stateStack.size() < 2 || parseStack.size() < 2) {
                    throw new RuntimeException("Internal error: stack smaller than expected\n");
                }

                stateStack.pop();
                StackAlphabet lastAction = stateStack.pop();
                GrammarVariable expectedExpression = parseStack.pop();
                GrammarVariable expectedVar = parseStack.pop();

                if (expectedExpression instanceof Expression expression && expectedVar instanceof Variable var && lastAction == StackAlphabet.ASSIGNMENT) {
                    Assignment assignment = new Assignment(var, expression);
                    mergeWithPreviousStatement(assignment, parseStack, stateStack);
                } else {
                    throw new RuntimeException("Incorrect stack trace for assignment while reading end." + "\n");
                }
            }
            case END_LIST: {
                if (parseStack.isEmpty() || stateStack.size() < 2) {
                    throw new RuntimeException("Internal error: stack smaller than expected\n");
                }

                GrammarVariable listInBlock = parseStack.pop();
                StackAlphabet listState = stateStack.pop();
                if (listInBlock instanceof StatementList list && listState == StackAlphabet.LIST) {

                    StackAlphabet blockInit = stateStack.pop();
                    if (blockInit == StackAlphabet.BLOCK || blockInit == StackAlphabet.WHILE_BLOCK) {

                        if (parseStack.isEmpty()) {
                            throw new RuntimeException("Internal error: parse stack smaller than expected\n");
                        }

                        GrammarVariable expectedExpression2 = parseStack.pop();
                        if (expectedExpression2 instanceof Expression expression2) {
                            Statement block;
                            if (blockInit == StackAlphabet.BLOCK) {
                                block = new IfBlock(expression2, list);
                            } else {
                                block = new WhileBlock(expression2, list);
                            }
                            mergeWithPreviousStatement(block, parseStack, stateStack);
                        } else {
                            throw new RuntimeException("Incorrect stack trace. Expected expression " + "\n");
                        }
                    } else {
                        throw new RuntimeException("Expected block statement. " + "\n");
                    }
                } else {
                    throw new RuntimeException("Internal error: incorrect stack content\n");
                }
                break;
            }
            case RIGHT_BRACKET: {
                if (parseStack.isEmpty() || stateStack.size() < 2) {
                    throw new RuntimeException("Internal error: stack smaller than expected");
                }

                GrammarVariable expectedExpression = parseStack.pop();
                stateStack.pop();
                StackAlphabet bracket = stateStack.pop();

                if (bracket == StackAlphabet.BRACKET && expectedExpression instanceof Expression expression) {
                    BracketExpression bracketExpression = new BracketExpression(expression);

                    if (stateStack.isEmpty()) {
                        throw new RuntimeException("Internal error: reduce stack smaller than expected\n");
                    }

                    StackAlphabet actionBeforeBrackets = stateStack.pop();
                    if (actionBeforeBrackets == StackAlphabet.EXPRESSION) {
                        newState = ParseState.OPERATOR_EXPRESSION;
                    } else {
                        newState = ParseState.EXPRESSION;
                        stateStack.push(actionBeforeBrackets);
                    }
                    stateStack.push(StackAlphabet.EXPRESSION);
                    parseStack.push(bracketExpression);
                } else {
                    throw new RuntimeException("Incorrect stack trace. Expected left bracket" + "\n");
                }
                if (newState != ParseState.OPERATOR_EXPRESSION) {
                    break;
                }
            }
            case OPERATOR_EXPRESSION: {
                newState = ParseState.EXPRESSION;

                if (parseStack.size() < 3 && stateStack.isEmpty()) {
                    throw new RuntimeException("Internal error: parse stack smaller than expected\n");
                }

                GrammarVariable expectedExpressionSecond = parseStack.pop();
                GrammarVariable expectedOperator = parseStack.pop();
                GrammarVariable expectedExpressionFirst = parseStack.pop();
                StackAlphabet expression = stateStack.pop();

                if (expectedExpressionFirst  instanceof Expression expression1 && expectedExpressionSecond instanceof Expression expression2 && expectedOperator instanceof Operator operator && expression == StackAlphabet.EXPRESSION) {
                    ExpressionProduct product = new ExpressionProduct(expression1, expression2, operator);
                    parseStack.push(product);
                    stateStack.push(StackAlphabet.EXPRESSION);
                } else {
                    throw new RuntimeException("incorrect stack trace for expression product" + "\n");
                }
                break;
            }
            case PROGRAM_END: {

                if (parseStack.isEmpty() && stateStack.isEmpty()) {
                    throw new RuntimeException("Internal error: stack smaller than expected\n");
                }

                if (parseStack.size() == 1 && stateStack.size() == 1) {
                    return newState;
                }

                StackAlphabet lastAction = stateStack.pop();
                StackAlphabet prevLastAction = stateStack.pop();
                GrammarVariable expectedExpression = parseStack.pop();
                GrammarVariable expectedVar = parseStack.pop();
                if (expectedExpression instanceof Expression expression && expectedVar instanceof Variable var && lastAction == StackAlphabet.EXPRESSION && prevLastAction == StackAlphabet.ASSIGNMENT) {
                    Assignment assignment = new Assignment(var, expression);
                    mergeWithPreviousStatement(assignment, parseStack, stateStack);
                } else {
                    throw new RuntimeException("incorrect tack trace\n");
                }
                break;
            }
            default: {
                throw new RuntimeException("Irreducible state " + state + "\n");
            }
        }
        return newState;
    }

    private static boolean reducible(ParseState state) {
        return switch (state) {
            case NEW_ASSIGNMENT, NEW_BLOCK, END, END_LIST, OPERATOR_EXPRESSION, RIGHT_BRACKET, PROGRAM_END -> true;
            default -> false;
        };
    }

    private static String formatToken(String token) {
        if (token.length() == 1) {
            char c = token.charAt(0);
            if (c >= 97 && c <= 122) {
                return "var";
            }
            if (Operators.isOperator(c)) {
                return "op";
            }
        }

        try {
            int num = Integer.parseInt(token);
            return "num";
        } catch (NumberFormatException e) {
            return token;
        }
    }

    private static String[] tokenize(String input) {
        String formattedInput = input.toLowerCase()
                .replaceAll("\\n", "")
                .replaceAll("\\(", " ( ")
                .replaceAll("\\)", " ) ")
                .replaceAll("=", " = ")
                .replaceAll("\\+", " + ")
                .replaceAll("-", " - ")
                .replaceAll("\\*", " * ")
                .replaceAll("/", " / ")
                .replaceAll(">", " > ")
                .replaceAll("<", " < ")
                .replaceAll("\\s+", " ")
                ;

        formattedInput = formattedInput + " .";

        return formattedInput.split("\\s+");
    }

    private static void mergeWithPreviousStatement(Statement statement, Stack<GrammarVariable> parseStack, Stack<StackAlphabet> stateStack) {
        if (!stateStack.empty() && !parseStack.empty()) {
            StackAlphabet actionBeforeStatement = stateStack.pop();
            GrammarVariable expectedList = parseStack.pop();
            if (actionBeforeStatement == StackAlphabet.LIST && expectedList instanceof StatementList list) {
                StatementListStatement newList = new StatementListStatement(list, statement);
                parseStack.push(newList);
            } else {
                parseStack.push(expectedList);
                parseStack.push(statement);
                stateStack.push(actionBeforeStatement);
            }
            stateStack.push(StackAlphabet.LIST);
        } else {
            parseStack.push(statement);
            stateStack.push(StackAlphabet.LIST);
        }
    }
}

enum StackAlphabet {
    BRACKET,
    EXPRESSION,
    BLOCK,
    WHILE_BLOCK,
    ASSIGNMENT,
    LIST

}

enum ParseState {
    INITIAL,
    ASSIGNMENT,
    EXPRESSION_HOLDER,
    EXPRESSION,
    OPERATOR,
    OPERATOR_EXPRESSION,
    NEW_ASSIGNMENT,
    NEW_BLOCK,
    END,
    END_LIST,
    RIGHT_BRACKET,
    PROGRAM_END
}
