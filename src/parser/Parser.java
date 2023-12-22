package parser;

import grammar.dictionary.*;
import grammar.variables.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Parser {
    public static Program parse(String input) {
        if (input == null || input.isEmpty()) {
            throw new RuntimeException("Empty input\n");
        }

        String[] tokens = tokenize(input);

        if (tokens.length == 0) {
            throw new RuntimeException("No elements after tokenizing");
        }

        Stack<GrammarVariable> parseStack = new Stack<>();
        ParseState state = ParseState.INITIAL;

        for (String token : tokens) {
            state = parseToken(state, token, parseStack);
        }

        GrammarVariable expectedList = parseStack.pop();

        if (expectedList instanceof StatementList list && parseStack.empty()) {
            return list;
        } else {
            throw new RuntimeException("Stack cannot be reduced to program\n");
        }
    }

    private static ParseState parseToken(ParseState currentState, String token, Stack<GrammarVariable> parseStack) {
        Map<String, Words> dictionary = LanguageDictionary.dictionary;

        String fToken = formatToken(token);
        ParseState newState = currentState;

        List<GrammarVariable> newTokensOnStack = new ArrayList<>();

        switch(currentState) {
            case INITIAL: {
                switch(dictionary.get(fToken)) {
                    case VAR: {
                        newTokensOnStack.add(new Variable(token.charAt(0)));
                        newTokensOnStack.add(SimpleLexeme.ASSIGNMENT);
                        newState = ParseState.ASSIGNMENT;
                        break;
                    }
                    case WHILE: {
                        newTokensOnStack.add(SimpleLexeme.WHILE_HOLDER);
                        newState = ParseState.EXPRESSION_HOLDER;
                        break;
                    }
                    case IF: {
                        newTokensOnStack.add(SimpleLexeme.IF_HOLDER);
                        newState = ParseState.EXPRESSION_HOLDER;
                        break;
                    }
                    case END: {
                        parseStack.push(ReduceLexeme.INITIAL_END);
                        break;
                    }
                    case PROGRAM_END: {
                        parseStack.push(ReduceLexeme.PROGRAM_END);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". grammar.variables.Statement expected (variable, if or while)\n");
                    }
                }
                break;
            }
            case ASSIGNMENT: {
                if (dictionary.get(fToken) == Words.EQ) {
                    newState = ParseState.EXPRESSION_HOLDER;
                } else {
                    throw new RuntimeException("Incorrect token " + fToken + ". \"=\" expected\n");
                }
                break;
            }
            case EXPRESSION_HOLDER: {
                switch(dictionary.get(fToken)) {
                    case NUM: {
                        newTokensOnStack.add(new Constant(Integer.parseInt(token)));
                        newState = ParseState.EXPRESSION;
                        break;
                    }
                    case VAR: {
                        newTokensOnStack.add(new Variable(token.charAt(0)));
                        newState = ParseState.EXPRESSION;
                        break;
                    }
                    case LB: {
                        parseStack.push(SimpleLexeme.LEFT_PAREN);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". Expression expected (var, const or right paren)\n");
                    }
                }
                break;
            }
            case EXPRESSION: {
                switch(dictionary.get(fToken)) {
                    case OP_1:
                    case OP_2:
                    case OP_3: {
                        newTokensOnStack.add(new Operator(Operators.convert(token.charAt(0))));
                        newState = ParseState.OPERATOR;
                        break;
                    }
                    case VAR: {
                        newTokensOnStack.add(new Variable(token.charAt(0)));
                        newTokensOnStack.add(SimpleLexeme.ASSIGNMENT);
                        parseStack.push(ReduceLexeme.NEW_ASG);
                        break;
                    }
                    case RB: {
                        parseStack.push(ReduceLexeme.RIGHT_PAREN);
                        break;
                    }
                    case IF: {
                        newTokensOnStack.add(SimpleLexeme.IF_HOLDER);
                        parseStack.push(ReduceLexeme.NEW_BLOCK);
                        break;
                    }
                    case WHILE: {
                        newTokensOnStack.add(SimpleLexeme.WHILE_HOLDER);
                        parseStack.push(ReduceLexeme.NEW_BLOCK);
                        break;
                    }
                    case END: {
                        parseStack.push(ReduceLexeme.EXPRESSION_END);
                        break;
                    }
                    case PROGRAM_END: {
                        parseStack.push(ReduceLexeme.PROGRAM_END);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". Operator or new statement or \"end\" expected\n");
                    }
                }
                break;
            }
            case OPERATOR: {
                switch(dictionary.get(fToken)) {
                    case NUM: {
                        parseStack.push(new Constant(Integer.parseInt(token)));
                        newState = ParseState.EXPRESSION_2;
                        break;
                    }
                    case VAR: {
                        parseStack.push(new Variable(token.charAt(0)));
                        newState = ParseState.EXPRESSION_2;
                        break;
                    }
                    case LB: {
                        newTokensOnStack.add(SimpleLexeme.LEFT_PAREN);
                        newState = ParseState.EXPRESSION_HOLDER;
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". Expression after operator expected\n");
                    }
                }
                break;
            }
            case EXPRESSION_2: {
                switch(dictionary.get(fToken)) {
                    case OP_1:
                    case OP_2:
                    case OP_3: {
                        newTokensOnStack.add(new Operator(Operators.convert(token.charAt(0))));
                        newState = ParseState.OPERATOR_2;
                        break;
                    }
                    case VAR: {
                        newTokensOnStack.add(new Variable(token.charAt(0)));
                        newTokensOnStack.add(SimpleLexeme.ASSIGNMENT);
                        parseStack.push(ReduceLexeme.NEW_ASG);
                        break;
                    }
                    case RB: {
                        parseStack.push(ReduceLexeme.RIGHT_PAREN);
                        break;
                    }
                    case IF: {
                        newTokensOnStack.add(SimpleLexeme.IF_HOLDER);
                        parseStack.push(ReduceLexeme.NEW_BLOCK);
                        break;
                    }
                    case WHILE: {
                        newTokensOnStack.add(SimpleLexeme.WHILE_HOLDER);
                        parseStack.push(ReduceLexeme.NEW_BLOCK);
                        break;
                    }
                    case END: {
                        parseStack.push(ReduceLexeme.EXPRESSION_END);
                        break;
                    }
                    case PROGRAM_END: {
                        parseStack.push(ReduceLexeme.PROGRAM_END);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". Operator, new statement or \"end\" expected\n");
                    }
                }
                break;
            }
            case OPERATOR_2: {
                switch(dictionary.get(fToken)) {
                    case NUM: {
                        parseStack.push(new Constant(Integer.parseInt(token)));
                        newState = ParseState.EXPRESSION_3;
                        break;
                    }
                    case VAR: {
                        parseStack.push(new Variable(token.charAt(0)));
                        newState = ParseState.EXPRESSION_3;
                        break;
                    }
                    case LB: {
                        newTokensOnStack.add(SimpleLexeme.LEFT_PAREN);
                        newState = ParseState.EXPRESSION_HOLDER;
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". Expression after operator expected\n");
                    }
                }
                break;
            }
            case EXPRESSION_3: {
                switch(dictionary.get(fToken)) {
                    case OP_1:
                    case OP_2:
                    case OP_3: {
                        newTokensOnStack.add(new Operator(Operators.convert(token.charAt(0))));
                        newState = ParseState.OPERATOR_3;
                        break;
                    }
                    case VAR: {
                        newTokensOnStack.add(new Variable(token.charAt(0)));
                        newTokensOnStack.add(SimpleLexeme.ASSIGNMENT);
                        parseStack.push(ReduceLexeme.NEW_ASG);
                        break;
                    }
                    case RB: {
                        parseStack.push(ReduceLexeme.RIGHT_PAREN);
                        break;
                    }
                    case IF: {
                        newTokensOnStack.add(SimpleLexeme.IF_HOLDER);
                        parseStack.push(ReduceLexeme.NEW_BLOCK);
                        break;
                    }
                    case WHILE: {
                        newTokensOnStack.add(SimpleLexeme.WHILE_HOLDER);
                        parseStack.push(ReduceLexeme.NEW_BLOCK);
                        break;
                    }
                    case END: {
                        parseStack.push(ReduceLexeme.EXPRESSION_END);
                        break;
                    }
                    case PROGRAM_END: {
                        parseStack.push(ReduceLexeme.PROGRAM_END);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect token " + token + ". Operator, new statement or end expected\n");
                    }
                }
                break;
            }
            case OPERATOR_3: {
                switch(dictionary.get(fToken)) {
                    case NUM: {
                        parseStack.push(new Constant(Integer.parseInt(token)));
                        newState = ParseState.EXPRESSION_4;
                        break;
                    }
                    case VAR: {
                        parseStack.push(new Variable(token.charAt(0)));
                        newState = ParseState.EXPRESSION_4;
                        break;
                    }
                    case LB: {
                        newTokensOnStack.add(SimpleLexeme.LEFT_PAREN);
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
        newState = reduce(newState, parseStack);

        if (!newTokensOnStack.isEmpty()) {
            newTokensOnStack.forEach(parseStack::push);
        }

        return newState;
    }

    private static ParseState reduce(ParseState state, Stack<GrammarVariable> parseStack) {
        ParseState newState = state;

        switch(state) {
            case EXPRESSION_4: {
                if (parseStack.size() < 8) {
                    throw new RuntimeException("Internal error: stack size smaller than expected in state EXPRESSION_4\n");
                }

                boolean red = false;

                GrammarVariable potentialEndExpression = parseStack.pop();
                Expression expression4;
                if (potentialEndExpression instanceof ReduceLexeme) {
                    expression4 = (Expression) parseStack.pop();
                } else if (potentialEndExpression instanceof Expression) {
                    expression4 = (Expression) potentialEndExpression;
                } else {
                    throw new RuntimeException("Incorrect stack content: expression or reduce lexeme on top expected: " + potentialEndExpression.getClass() + "\n");
                }

                GrammarVariable op3 = parseStack.pop();
                GrammarVariable exp3 = parseStack.pop();
                GrammarVariable op2 = parseStack.pop();
                GrammarVariable exp2 = parseStack.pop();

                if (exp2 instanceof Expression expression2 && exp3 instanceof Expression expression3 &&
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
                } else {
                    throw new RuntimeException("Following stack content expected: exp, op, exp, op");
                }
                if (potentialEndExpression instanceof ReduceLexeme) {
                    parseStack.push(potentialEndExpression);
                }
                if (!red) {
                    break;
                }
            }
            case EXPRESSION_3: {
                if (parseStack.size() < 6) {
                    throw new RuntimeException("Internal error: stack size smaller than expected for state EXPRESSION_3\n");
                }

                GrammarVariable potentialEndExpression = parseStack.pop();
                Expression expression3;
                if (potentialEndExpression instanceof ReduceLexeme) {
                    expression3 = (Expression) parseStack.pop();
                } else if (potentialEndExpression instanceof Expression) {
                    expression3 = (Expression) potentialEndExpression;
                } else {
                    throw new RuntimeException("Incorrect stack content: expression or reduce lexeme expected on top\n");
                }

                GrammarVariable op2 = parseStack.pop();
                GrammarVariable exp2 = parseStack.pop();
                GrammarVariable op1 = parseStack.pop();
                GrammarVariable exp1 = parseStack.pop();

                if (exp1 instanceof Expression expression1 && exp2 instanceof Expression expression2 &&
                        op1 instanceof Operator operator1 && op2 instanceof Operator operator2) {
                    if (operator2.priority() == Operators.MAX_PRIORITY || (potentialEndExpression instanceof ReduceLexeme &&
                            operator2.priority() > operator1.priority())) {
                        newState = ParseState.EXPRESSION_2;
                        Expression product = new ExpressionProduct(expression2, expression3, operator2);
                        parseStack.push(expression1);
                        parseStack.push(operator1);
                        parseStack.push(product);
                    } else if (operator1.priority() >= operator2.priority()) {
                        newState = ParseState.EXPRESSION_2;
                        ExpressionProduct product = new ExpressionProduct(expression1, expression2, operator1);
                        parseStack.push(product);
                        parseStack.push(operator2);
                        parseStack.push(expression3);
                    } else {
                        parseStack.push(expression1);
                        parseStack.push(operator1);
                        parseStack.push(expression2);
                        parseStack.push(operator2);
                        parseStack.push(expression3);
                    }
                }
                if (!(potentialEndExpression instanceof ReduceLexeme)) {
                    break;
                } else {
                    parseStack.push(potentialEndExpression);
                }
            }
            case EXPRESSION_2: {
                if (parseStack.size() < 4) {
                    throw new RuntimeException("Internal error: parse stack smaller than expected for state EXPRESSION_2\n");
                }

                GrammarVariable potentialEndExpression = parseStack.pop();
                Expression expression2;
                if (potentialEndExpression instanceof ReduceLexeme) {
                    expression2 = (Expression) parseStack.pop();
                } else if (potentialEndExpression instanceof Expression) {
                    expression2 = (Expression) potentialEndExpression;
                } else {
                    throw new RuntimeException("Incorrect stack content: expression or reduce lexeme expected on top\n");
                }

                GrammarVariable expectedOperator = parseStack.pop();
                GrammarVariable expectedExpressionFirst = parseStack.pop();

                if (expectedExpressionFirst  instanceof Expression expression1 && expectedOperator instanceof Operator operator) {
                    if (potentialEndExpression instanceof ReduceLexeme || operator.priority() == Operators.MAX_PRIORITY) {
                        newState = ParseState.EXPRESSION;
                        ExpressionProduct product = new ExpressionProduct(expression1, expression2, operator);
                        parseStack.push(product);
                    } else {
                        parseStack.push(expression1);
                        parseStack.push(operator);
                        parseStack.push(expression2);
                    }
                } else {
                    throw new RuntimeException("incorrect stack trace for expression product: expression and operator expected" + "\n");
                }
                if (potentialEndExpression instanceof ReduceLexeme) {
                    parseStack.push(potentialEndExpression);
                } else {
                    break;
                }
            }
            case INITIAL:
            case EXPRESSION: {
                if (parseStack.isEmpty()) {
                    throw new RuntimeException("Internal error: stack is empty. Expression expected\n");
                }
                GrammarVariable potentialEndExpression = parseStack.pop();
                if (potentialEndExpression instanceof ReduceLexeme) {
                    parseStack.push(potentialEndExpression);
                    newState = reduceStatement(newState, parseStack);
                } else {
                    parseStack.push(potentialEndExpression);
                }
                break;
            }
        }
        return newState;
    }

    private static ParseState reduceStatement(ParseState state, Stack<GrammarVariable> parseStack) {
        if (parseStack.isEmpty()) {
            throw new RuntimeException("Stack is empty\n");
        }

        ParseState newState = state;

        GrammarVariable token = parseStack.pop();
        ReduceLexeme reductionSource;
        if (token instanceof ReduceLexeme) {
            reductionSource = (ReduceLexeme) token;
        } else {
            parseStack.push(token);
            return state;
        }

        switch(reductionSource) {
            case NEW_ASG: {
                if (parseStack.size() < 2) {
                    throw new RuntimeException("Reduce error: expression and statement expected on stack to create new assignment. Stack too small\n");
                }

                GrammarVariable exp = parseStack.pop();
                GrammarVariable lex = parseStack.pop();

                if (!(exp instanceof Expression expression && lex instanceof SimpleLexeme lastAction)) {
                    throw new RuntimeException("incorrect stack content for reduce: expression and lexeme expected\n");
                }

                newState = ParseState.ASSIGNMENT;

                switch (lastAction) {
                    case ASSIGNMENT: {

                        if (parseStack.isEmpty()) {
                            throw new RuntimeException("Stack empty: variable expected to reduce assignment\n");
                        }

                        GrammarVariable expectedVar = parseStack.pop();
                        if (expectedVar instanceof Variable var) {
                            Assignment assignment = new Assignment(var, expression);
                            mergeWithPreviousStatement(assignment, parseStack);
                        } else {
                            throw new RuntimeException("Internal error: Incorrect stack trace for assignment. Variable expected" + "\n");
                        }
                        break;
                    }
                    case WHILE_HOLDER: {
                        parseStack.push(SimpleLexeme.WHILE_HOLDER);
                        parseStack.push(expression);
                        break;
                    }
                    case IF_HOLDER: {
                        parseStack.push(SimpleLexeme.IF_HOLDER);
                        parseStack.push(expression);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect state stack trace. Lexeme expected, got: " + lastAction + "\n");
                    }
                }
                break;
            }
            case NEW_BLOCK: {
                newState = ParseState.EXPRESSION_HOLDER;

                if (parseStack.size() < 2) {
                    throw new RuntimeException("Internal error: reduce stack smaller than expected. Expression and lexeme expected to create new block\n");
                }

                GrammarVariable exp = parseStack.pop();
                GrammarVariable lex = parseStack.pop();

                if (!(exp instanceof Expression expression && lex instanceof SimpleLexeme lastAction)) {
                    throw new RuntimeException("Incorrect stack content: expression and lexeme expected\n");
                }

                switch (lastAction) {
                    case ASSIGNMENT: {

                        if (parseStack.isEmpty()) {
                            throw new RuntimeException("Stack is empty. Variable expected to reduce assignment\n");
                        }

                        GrammarVariable expectedVar = parseStack.pop();
                        if (expectedVar instanceof Variable var) {
                            Assignment assignment = new Assignment(var, expression);
                            mergeWithPreviousStatement(assignment, parseStack);
                        } else {
                            throw new RuntimeException("Incorrect stack trace for assignment: variable expected\n");
                        }
                        break;
                    }
                    case WHILE_HOLDER: {
                        parseStack.push(SimpleLexeme.WHILE_HOLDER);
                        parseStack.push(expression);
                        break;
                    }
                    case IF_HOLDER: {
                        parseStack.push(SimpleLexeme.IF_HOLDER);
                        parseStack.push(expression);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Incorrect stack trace: lexeme expected. Got: " + lastAction + "\n");
                    }
                }
                break;
            }
            case EXPRESSION_END: {
                if (parseStack.size() < 3) {
                    throw new RuntimeException("Stack too small. Expression, lexeme and variable expected\n");
                }

                GrammarVariable exp = parseStack.pop();
                GrammarVariable lex = parseStack.pop();
                GrammarVariable var = parseStack.pop();

                if (!(exp instanceof Expression expression && lex instanceof SimpleLexeme lastAction &&
                        lastAction == SimpleLexeme.ASSIGNMENT && var instanceof Variable variable)) {
                    throw new RuntimeException("Incorrect stack trace: expression, lexeme and variable expected\n");
                }

                Assignment assignment = new Assignment(variable, expression);
                mergeWithPreviousStatement(assignment, parseStack);
            }
            case INITIAL_END: {
                newState = ParseState.INITIAL;

                if (parseStack.size() < 3) {
                    throw new RuntimeException("Stack smaller than expected: list, expression and lexeme expected\n");
                }

                GrammarVariable listInBlock = parseStack.pop();
                GrammarVariable exp = parseStack.pop();
                GrammarVariable lex = parseStack.pop();

                if (!(listInBlock instanceof StatementList list && exp instanceof Expression expression &&
                        lex instanceof SimpleLexeme blockInit && (blockInit == SimpleLexeme.IF_HOLDER || blockInit == SimpleLexeme.WHILE_HOLDER))) {
                    throw new RuntimeException("Incorrect stack content: list, expression and block lexeme expected\n");
                }

                Statement block;
                if (blockInit == SimpleLexeme.IF_HOLDER) {
                    block = new IfBlock(expression, list);
                } else {
                    block = new WhileBlock(expression, list);
                }
                mergeWithPreviousStatement(block, parseStack);
                break;
            }
            case RIGHT_PAREN: {
                if (parseStack.size() < 3) {
                    throw new RuntimeException("Stack to small to reduce right paren: expression, lexeme and something more expected\n");
                }

                GrammarVariable exp = parseStack.pop();
                GrammarVariable lex = parseStack.pop();

                if (!(exp instanceof Expression expression && lex instanceof SimpleLexeme paren && paren == SimpleLexeme.LEFT_PAREN)) {
                    throw new RuntimeException("Incorrect stack trace: expression and left paren expected\n");
                }

                BracketExpression bracketExpression = new BracketExpression(expression);


                GrammarVariable potOperator3 = parseStack.pop();

                if (potOperator3 instanceof Operator operator3) {
                    if (parseStack.size() < 2) {
                        throw new RuntimeException("Stack too small: expression and lexeme expected before operator\n");
                    }

                    GrammarVariable exp3 = parseStack.pop();

                    if (!(exp3 instanceof Expression expression3)) {
                        throw new RuntimeException("Incorrect stack trace: expression before operator expected\n");
                    }

                    GrammarVariable potOperator2 = parseStack.pop();
                    if (potOperator2 instanceof Operator operator2) {
                        if (parseStack.size() < 2) {
                            throw new RuntimeException("Stack too small: expression and lexeme expected before operator\n");
                        }

                        GrammarVariable exp2 = parseStack.pop();

                        if (!(exp2 instanceof Expression expression2)) {
                            throw new RuntimeException("Incorrect stack trace: expression before operator expected\n");
                        }

                        GrammarVariable potOperator1 = parseStack.pop();

                        if (potOperator1 instanceof Operator operator1) {

                            if (parseStack.size() < 2) {
                                throw new RuntimeException("Internal error: reduce stack smaller than expected\n");
                            }

                            GrammarVariable exp1 = parseStack.pop();

                            if (!(exp1 instanceof Expression expression1)) {
                                throw new RuntimeException("Incorrect stack trace: expression before operator expected\n");
                            }

                            GrammarVariable previousAction = parseStack.pop();
                            if (previousAction instanceof SimpleLexeme) {
                                parseStack.push(previousAction);
                                parseStack.push(expression1);
                                parseStack.push(operator1);
                                parseStack.push(expression2);
                                parseStack.push(operator2);
                                parseStack.push(expression3);
                                parseStack.push(operator3);
                                parseStack.push(bracketExpression);
                                newState = reduce(ParseState.EXPRESSION_4, parseStack);
                            } else {
                                throw new RuntimeException("Incorrect stack trace: lexeme before expression expected\n");
                            }
                        } else {
                            parseStack.push(potOperator1);
                            parseStack.push(expression2);
                            parseStack.push(operator2);
                            parseStack.push(expression3);
                            parseStack.push(operator3);
                            parseStack.push(bracketExpression);
                            newState = reduce(ParseState.EXPRESSION_3, parseStack);
                        }
                    } else {
                        parseStack.push(potOperator2);
                        parseStack.push(expression3);
                        parseStack.push(operator3);
                        parseStack.push(bracketExpression);
                        newState = reduce(ParseState.EXPRESSION_2, parseStack);
                    }
                } else {
                    parseStack.push(potOperator3);
                    parseStack.push(bracketExpression);
                    newState = ParseState.EXPRESSION;
                }
                break;
            }
            case PROGRAM_END: {

                if (parseStack.isEmpty()) {
                    throw new RuntimeException("Stack empty before programm end\n");
                }

                if (parseStack.size() == 1) {
                    return newState;
                }

                GrammarVariable expectedExpression = parseStack.pop();
                GrammarVariable action = parseStack.pop();
                GrammarVariable expectedVar = parseStack.pop();
                if (expectedExpression instanceof Expression expression && expectedVar instanceof Variable var && action == SimpleLexeme.ASSIGNMENT) {
                    Assignment assignment = new Assignment(var, expression);
                    mergeWithPreviousStatement(assignment, parseStack);
                } else {
                    throw new RuntimeException("Incorrect stack trace: expression, assignment lexeme and variable expected\n");
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
            if (!token.equals("if") && !token.equals("while")
                && !token.equals("end") && !token.equals(".")
                    && !token.equals("=") && !token.equals("(")
                        && !token.equals(")")) {
                throw new RuntimeException("No such token in language: " + token + "\n");

            }
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

    private static void mergeWithPreviousStatement(Statement statement, Stack<GrammarVariable> parseStack) {
        if (!parseStack.empty()) {
            GrammarVariable expectedList = parseStack.pop();
            if (expectedList instanceof StatementList list) {
                StatementListStatement newList = new StatementListStatement(list, statement);
                parseStack.push(newList);
            } else {
                parseStack.push(expectedList);
                parseStack.push(statement);
            }
        } else {
            parseStack.push(statement);
        }
    }

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
