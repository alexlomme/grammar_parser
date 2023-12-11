package grammar.dictionary;

import java.util.Map;

public class LanguageDictionary {
    public static final Map<String, Words> dictionary = Map.of(
            "var", Words.VAR,
            "num", Words.NUM,
            "op", Words.OP,
            "=", Words.EQ,
            "if", Words.IF,
            "end", Words.END,
            "while", Words.WHILE,
            "(", Words.LB,
            ")", Words.RB,
            ".", Words.PROGRAM_END
    );
}
