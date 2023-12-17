package grammar.dictionary;

import java.util.Map;

public class LanguageDictionary {
    public static final Map<String, Words> dictionary = Map.ofEntries(
            Map.entry("var", Words.VAR),
            Map.entry("num", Words.NUM),
            Map.entry("op1", Words.OP_1),
            Map.entry("op2", Words.OP_2),
            Map.entry("op3", Words.OP_3),
            Map.entry("=", Words.EQ),
            Map.entry("if", Words.IF),
            Map.entry("end", Words.END),
            Map.entry("while", Words.WHILE),
            Map.entry("(", Words.LB),
            Map.entry(")", Words.RB),
            Map.entry(".", Words.PROGRAM_END)
    );
}
