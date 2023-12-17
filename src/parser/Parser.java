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
                        stateStack.push(StackAlphabet.EXPRESSION_END_LIST);
                        break;
                    }
                    case PROGRAM_END: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_PROGRAM);
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
                    case OP_1:
                    case OP_2:
                    case OP_3: {
                        newTokenOnStack = new Operator(Operators.convert(token.charAt(0)));
                        newState = ParseState.OPERATOR;
                        break;
                    }
                    case VAR: {
                        newTokenOnStack = new Variable(token.charAt(0));
                        newVarOnStack = StackAlphabet.ASSIGNMENT;
                        stateStack.push(StackAlphabet.EXPRESSION_END_ASG);
                        break;
                    }
                    case RB: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_RB);
                        break;
                    }
                    case IF: {
                        newVarOnStack = StackAlphabet.BLOCK;
                        stateStack.push(StackAlphabet.EXPRESSION_END_BLOCK);
                        break;
                    }
                    case WHILE: {
                        newVarOnStack = StackAlphabet.WHILE_BLOCK;
                        stateStack.push(StackAlphabet.EXPRESSION_END_BLOCK);
                        break;
                    }
                    case END: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_END);
                        break;
                    }
                    case PROGRAM_END: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_PROGRAM);
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
                        stateStack.push(StackAlphabet.EXPRESSION);
                        newState = ParseState.EXPRESSION_2;
                        break;
                    }
                    case VAR: {
                        parseStack.push(new Variable(token.charAt(0)));
                        stateStack.push(StackAlphabet.EXPRESSION);
                        newState = ParseState.EXPRESSION_2;
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
            case EXPRESSION_2: {
                switch(dictionary.get(fToken)) {
                    case OP_1:
                    case OP_2:
                    case OP_3: {
                        newTokenOnStack = new Operator(Operators.convert(token.charAt(0)));
                        newState = ParseState.OPERATOR_2;
                        break;
                    }
                    case VAR: {
                        newTokenOnStack = new Variable(token.charAt(0));
                        newVarOnStack = StackAlphabet.ASSIGNMENT;
                        stateStack.push(StackAlphabet.EXPRESSION_END_ASG);
                        break;
                    }
                    case RB: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_RB);
                        break;
                    }
                    case IF: {
                        newVarOnStack = StackAlphabet.BLOCK;
                        stateStack.push(StackAlphabet.EXPRESSION_END_BLOCK);
                        break;
                    }
                    case WHILE: {
                        newVarOnStack = StackAlphabet.WHILE_BLOCK;
                        stateStack.push(StackAlphabet.EXPRESSION_END_BLOCK);
                        break;
                    }
                    case END: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_END);
                        break;
                    }
                    case PROGRAM_END: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_PROGRAM);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". grammar.variables.Operator, new statement or end expected\n");
                    }
                }
                break;
            }
            case OPERATOR_2: {
                switch(dictionary.get(fToken)) {
                    case NUM: {
                        parseStack.push(new Constant(Integer.parseInt(token)));
                        stateStack.push(StackAlphabet.EXPRESSION);
                        newState = ParseState.EXPRESSION_3;
                        break;
                    }
                    case VAR: {
                        parseStack.push(new Variable(token.charAt(0)));
                        stateStack.push(StackAlphabet.EXPRESSION);
                        newState = ParseState.EXPRESSION_3;
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
            case EXPRESSION_3: {
                switch(dictionary.get(fToken)) {
                    case OP_1:
                    case OP_2:
                    case OP_3: {
                        newTokenOnStack = new Operator(Operators.convert(token.charAt(0)));
                        newState = ParseState.OPERATOR_3;
                        break;
                    }
                    case VAR: {
                        newTokenOnStack = new Variable(token.charAt(0));
                        newVarOnStack = StackAlphabet.ASSIGNMENT;
                        stateStack.push(StackAlphabet.EXPRESSION_END_ASG);
                        break;
                    }
                    case RB: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_RB);
                        break;
                    }
                    case IF: {
                        newVarOnStack = StackAlphabet.BLOCK;
                        stateStack.push(StackAlphabet.EXPRESSION_END_BLOCK);
                        break;
                    }
                    case WHILE: {
                        newVarOnStack = StackAlphabet.WHILE_BLOCK;
                        stateStack.push(StackAlphabet.EXPRESSION_END_BLOCK);
                        break;
                    }
                    case END: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_END);
                        break;
                    }
                    case PROGRAM_END: {
                        stateStack.push(StackAlphabet.EXPRESSION_END_PROGRAM);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". grammar.variables.Operator, new statement or end expected\n");
                    }
                }
                break;
            }
            case OPERATOR_3: {
                switch(dictionary.get(fToken)) {
                    case NUM: {
                        parseStack.push(new Constant(Integer.parseInt(token)));
                        stateStack.push(StackAlphabet.EXPRESSION);
                        newState = ParseState.EXPRESSION_4;
                        break;
                    }
                    case VAR: {
                        parseStack.push(new Variable(token.charAt(0)));
                        stateStack.push(StackAlphabet.EXPRESSION);
                        newState = ParseState.EXPRESSION_4;
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
        ParseState newState = state;

        switch(state) {
            case EXPRESSION_4: {
                if (stateStack.size() < 6 && parseStack.size() < 7) {
                    throw new RuntimeException("Internal error: stack size smaller than expected\n");
                }

                boolean red = false;

                GrammarVariable exp4 = parseStack.pop();
                GrammarVariable op3 = parseStack.pop();
                GrammarVariable exp3 = parseStack.pop();
                GrammarVariable op2 = parseStack.pop();
                GrammarVariable exp2 = parseStack.pop();
                StackAlphabet potentialEndExpression = stateStack.pop();
                if (isEndExpression(potentialEndExpression)) {
                    stateStack.pop();
                }

                if (exp4 instanceof Expression expression4 && exp3 instanceof Expression expression3 && exp2 instanceof Expression expression2 &&
                        op2 instanceof Operator operator2 && op3 instanceof Operator operator3) {
                    newState = ParseState.EXPRESSION_3;
                    ExpressionProduct product;
                    if (operator3.priority() > operator2.priority()) {
                        product = new ExpressionProduct(expression3, expression4, operator3);
                        parseStack.push(expression2);
                        parseStack.push(operator2);
                        parseStack.push(product);
                    } else {
                        product = new ExpressionProduct(expression2, expression3, operator2);
                        parseStack.push(product);
                        parseStack.push(operator3);
                        parseStack.push(expression4);
                        if (operator3.priority() < operator2.priority()) {
                            red = true;
                        }
                    }
                    if (isEndExpression(potentialEndExpression)) {
                        stateStack.push(potentialEndExpression);
                    }
                } else {
                    throw new RuntimeException("Internal error: false stack content\n");
                }
                if (!isEndExpression(potentialEndExpression) && !red) {
                    break;
                }
            }
            case EXPRESSION_3: {
                if (stateStack.size() < 4 && parseStack.size() < 5) {
                    throw new RuntimeException("Internal error: stack size smaller than expected\n");
                }

                StackAlphabet potentialEndExpression = stateStack.pop();

                GrammarVariable exp3 = parseStack.pop();
                GrammarVariable op2 = parseStack.pop();
                GrammarVariable exp2 = parseStack.pop();
                GrammarVariable op1 = parseStack.pop();
                GrammarVariable exp1 = parseStack.pop();
                if (isEndExpression(potentialEndExpression)) {
                    stateStack.pop();
                }
                stateStack.pop();

                if (exp1 instanceof Expression expression1 && exp2 instanceof Expression expression2 && exp3 instanceof Expression expression3
                        && op1 instanceof Operator operator1 && op2 instanceof Operator operator2) {
                    if (operator2.priority() == Operators.MAX_PRIORITY || (isEndExpression(potentialEndExpression) && operator2.priority() > operator1.priority())) {
                        newState = ParseState.EXPRESSION_2;
                        Expression product = new ExpressionProduct(expression2, expression3, operator2);
                        parseStack.push(exp1);
                        parseStack.push(op1);
                        parseStack.push(product);
                        stateStack.push(StackAlphabet.EXPRESSION);
                        if (isEndExpression(potentialEndExpression)) {
                            stateStack.push(potentialEndExpression);
                        }
                    } else if (operator1.priority() >= operator2.priority()) {
                        newState = ParseState.EXPRESSION_2;
                        ExpressionProduct product = new ExpressionProduct(expression1, expression2, operator1);
                        parseStack.push(product);
                        parseStack.push(operator2);
                        parseStack.push(expression3);
                        stateStack.push(StackAlphabet.EXPRESSION);
                    } else {
                        parseStack.push(expression1);
                        parseStack.push(operator1);
                        parseStack.push(expression2);
                        parseStack.push(operator2);
                        parseStack.push(expression3);
                        stateStack.push(StackAlphabet.EXPRESSION);
                        stateStack.push(StackAlphabet.EXPRESSION);
                    }
                } else {
                    throw new RuntimeException("Internal error: stack content not as expected\n");
                }
                if (!isEndExpression(potentialEndExpression)) {
                    break;
                } else {
                    stateStack.push(potentialEndExpression);
                }
            }
            case EXPRESSION_2: {
                if (parseStack.size() < 3 && stateStack.size() < 3) {
                    throw new RuntimeException("Internal error: parse stack smaller than expected\n");
                }

                StackAlphabet potentialEndExpression = stateStack.pop();

                GrammarVariable expectedExpressionSecond = parseStack.pop();
                GrammarVariable expectedOperator = parseStack.pop();
                GrammarVariable expectedExpressionFirst = parseStack.pop();

                if (expectedExpressionFirst  instanceof Expression expression1 && expectedExpressionSecond instanceof Expression expression2 && expectedOperator instanceof Operator operator) {
                    if (isEndExpression(potentialEndExpression) || operator.priority() == Operators.MAX_PRIORITY) {
                        newState = ParseState.EXPRESSION;
                        ExpressionProduct product = new ExpressionProduct(expression1, expression2, operator);
                        parseStack.push(product);
                        if (isEndExpression(potentialEndExpression)) {
                            stateStack.pop();
                        }
                    } else {
                        parseStack.push(expression1);
                        parseStack.push(operator);
                        parseStack.push(expression2);
                        stateStack.push(potentialEndExpression);
                    }
                } else {
                    throw new RuntimeException("incorrect stack trace for expression product" + "\n");
                }
                if (isEndExpression(potentialEndExpression)) {
                    stateStack.push(potentialEndExpression);
                } else {
                    break;
                }
            }
            case INITIAL:
            case EXPRESSION: {
                if (stateStack.isEmpty()) {
                    throw new RuntimeException("Internal error: stack smaller than expected\n");
                }
                StackAlphabet potentialEndExpression = stateStack.pop();
                if (isEndExpression(potentialEndExpression)) {
                    stateStack.push(potentialEndExpression);
                    newState = reduceStatement(newState, parseStack, stateStack);
                } else {
                    stateStack.push(potentialEndExpression);
                }
                break;
            }
        }
        return newState;
    }

    private static ParseState reduceStatement(ParseState state, Stack<GrammarVariable> parseStack, Stack<StackAlphabet> stateStack) {
        if (stateStack.isEmpty()) {
            throw new RuntimeException("");
        }

        ParseState newState = state;

        StackAlphabet token = stateStack.pop();
        switch(token) {
            case EXPRESSION_END_ASG: {
                if (stateStack.size() < 2) {
                    throw new RuntimeException("Reduce error: stack smaller than expected\n");
                }

                newState = ParseState.ASSIGNMENT;

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
            case EXPRESSION_END_BLOCK: {
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
            case EXPRESSION_END_END: {
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
            case EXPRESSION_END_LIST: {
                newState = ParseState.INITIAL;

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
            case EXPRESSION_END_RB: {
                if (stateStack.size() < 3 && parseStack.isEmpty()) {
                    throw new RuntimeException("Internal error: stack smaller than expected");
                }

                StackAlphabet e = stateStack.pop();
                StackAlphabet b = stateStack.pop();

                GrammarVariable expr = parseStack.pop();
                BracketExpression bracketExpression;
                if (expr instanceof Expression expression && e == StackAlphabet.EXPRESSION && b == StackAlphabet.BRACKET) {
                    bracketExpression = new BracketExpression(expression);
                    parseStack.push(bracketExpression);
                } else {
                    throw new RuntimeException("");
                }

                StackAlphabet potExpression3 = stateStack.pop();

                if (potExpression3 == StackAlphabet.EXPRESSION) {
                    if (stateStack.isEmpty()) {
                        throw new RuntimeException("Internal error: reduce stack smaller than expected\n");
                    }

                    StackAlphabet potExpression2 = stateStack.pop();
                    if (potExpression2 == StackAlphabet.EXPRESSION) {
                        if (stateStack.isEmpty()) {
                            throw new RuntimeException("Internal error: reduce stack smaller than expected\n");
                        }

                        StackAlphabet potExpression1 = stateStack.pop();

                        if (potExpression1 == StackAlphabet.EXPRESSION) {

                            StackAlphabet previousAction = stateStack.pop();
                            if (previousAction != StackAlphabet.EXPRESSION) {
                                stateStack.push(previousAction);
                                stateStack.push(StackAlphabet.EXPRESSION);
                                stateStack.push(StackAlphabet.EXPRESSION);
                                stateStack.push(StackAlphabet.EXPRESSION);
                                stateStack.push(StackAlphabet.EXPRESSION);
                                newState = reduce(ParseState.EXPRESSION_4, parseStack, stateStack);
                            } else {
                                throw new RuntimeException("Internal error: stack content not as expected\n");
                            }
                        } else {
                            stateStack.push(potExpression1);
                            stateStack.push(StackAlphabet.EXPRESSION);
                            stateStack.push(StackAlphabet.EXPRESSION);
                            stateStack.push(StackAlphabet.EXPRESSION);
                            newState = reduce(ParseState.EXPRESSION_3, parseStack, stateStack);
                        }
                    } else {
                        stateStack.push(potExpression2);
                        stateStack.push(StackAlphabet.EXPRESSION);
                        stateStack.push(StackAlphabet.EXPRESSION);
                        newState = reduce(ParseState.EXPRESSION_2, parseStack, stateStack);
                    }
                } else {
                    stateStack.push(potExpression3);
                    stateStack.push(StackAlphabet.EXPRESSION);
                    newState = ParseState.EXPRESSION;
                }
                break;
            }
            case EXPRESSION_END_PROGRAM: {

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
        }
        return newState;
    }

    private static String formatToken(String token) {
        if (token.length() == 1) {
            char c = token.charAt(0);
            if (c >= 97 && c <= 122) {
                return "var";
            }
            if (Operators.isOperator(c)) {
                Operators operator = Operators.convert(c);
                return "op" + Operators.priority(operator);
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
                .replaceAll("\\n", " ")
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

    private static boolean isEndExpression(StackAlphabet var) {
        return switch(var) {
            case EXPRESSION_END_ASG, EXPRESSION_END_BLOCK, EXPRESSION_END_RB, EXPRESSION_END_END, EXPRESSION_END_LIST, EXPRESSION_END_PROGRAM -> true;
            default -> false;
        };
    }
}

enum StackAlphabet {
    BRACKET,
    EXPRESSION,
    BLOCK,
    WHILE_BLOCK,
    ASSIGNMENT,
    LIST,
    EXPRESSION_END_ASG,
    EXPRESSION_END_END,
    EXPRESSION_END_LIST,
    EXPRESSION_END_RB,
    EXPRESSION_END_BLOCK,
    EXPRESSION_END_PROGRAM,

}

enum ParseState {
    INITIAL,
    ASSIGNMENT,
    EXPRESSION_HOLDER,
    EXPRESSION,
    OPERATOR,
    EXPRESSION_2,
    OPERATOR_2,
    EXPRESSION_3,
    OPERATOR_3,
    EXPRESSION_4,
}
