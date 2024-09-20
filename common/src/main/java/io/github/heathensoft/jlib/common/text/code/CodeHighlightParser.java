package io.github.heathensoft.jlib.common.text.code;

import io.github.heathensoft.jlib.common.text.Ascii;

/**
 * @author Frederik Dahl
 * 14/05/2024
 */


public class CodeHighlightParser {

    private final GenericScanner scanner;
    public CodeHighlightParser(Ascii.Trie keywords, Ascii.Trie datatypes) { scanner = new GenericScanner(keywords, datatypes); }
    public CodeHighlightParser(Language language) {
        this(language.keywords(),language.datatypes());
    }

    /**
     * Parse raw code and produce an array of "highlighted" sequences.
     * The Array will always contain at least one sequence (DEFAULT if source is empty).
     * And the sum length in characters of all the sequences is Integer.MAX_VALUE.
     * Where the last sequence length is this sum minus the length of the other sequences.
     * @param source raw stream of ascii characters.
     * @return array of highlighted sequences.
     */
    public CodeHighlights parseCode(Ascii.Buffer source) {
        TokenArray tokens = scanner.tokenize(source);
        CodeHighlights codeHighlight = new CodeHighlights();
        if (tokens.isEmpty()) {
            codeHighlight.ensureCapacity(1);
            codeHighlight.addSequence(CodeColor.DEFAULT,Integer.MAX_VALUE);
        } else { tokens.rewind();
            int capacity = Math.max(128,(int)(tokens.numTokens() * 0.75f));
            codeHighlight.ensureCapacity(capacity);
            int start = 0;
            TokenType tokenType = TokenType.byIdentifier(tokens.tokenType());
            CodeColor highlight = tokenType.highlight;
            while (true) {
                if (tokens.nextToken()) {
                    tokenType = TokenType.byIdentifier(tokens.tokenType());
                    CodeColor nextHighlight = tokenType.highlight;
                    if (nextHighlight != highlight) {
                        int end = tokens.tokenStart();
                        int len = end - start;
                        codeHighlight.addSequence(highlight,len);
                        highlight = nextHighlight;
                        start = end;
                    }
                } else { int len = Integer.MAX_VALUE - start;
                    codeHighlight.addSequence(highlight,len);
                    break;
                }
            } codeHighlight.trim();
        } codeHighlight.rewind();
        return codeHighlight;
    }
}
