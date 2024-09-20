package io.github.heathensoft.jlib.common.text.code;

/**
 * @author Frederik Dahl
 * 14/05/2024
 */


public enum TokenType {

    // Non Symbols
    UNASSIGNED(0,"[??]", TokenClass.NONE, CodeColor.DEFAULT),
    INTEGER_DECIMAL(1,"[num]", TokenClass.PRIMITIVE, CodeColor.NUMBERS),
    HEXADECIMAL(2,"[hex]", TokenClass.PRIMITIVE, CodeColor.NUMBERS),
    BINARY(3,"[bin]", TokenClass.PRIMITIVE, CodeColor.NUMBERS),
    FLOATING_POINT(4,"[dec]", TokenClass.PRIMITIVE, CodeColor.NUMBERS),
    EXPONENTIAL(5,"[exp]", TokenClass.PRIMITIVE, CodeColor.NUMBERS),
    BOOLEAN(6,"[bool]", TokenClass.PRIMITIVE, CodeColor.NUMBERS),
    IDENTIFIER(7,"[ide]", TokenClass.IDENTIFIER, CodeColor.DEFAULT),
    KEYWORD(8,"[key]", TokenClass.IDENTIFIER, CodeColor.KEYWORDS),
    DATATYPE(9,"[typ]", TokenClass.IDENTIFIER, CodeColor.DATATYPES),
    SPECIAL(10,"[ide]", TokenClass.IDENTIFIER, CodeColor.SPECIAL), // # @ || ALL CAPS
    COMMENT(11,"[//*]", TokenClass.COMMENT, CodeColor.COMMENTS),
    STRING(12,"[str]", TokenClass.STRING, CodeColor.STRINGS),
    CHAR(13,"[char]", TokenClass.STRING, CodeColor.STRINGS),

    // Multiple Combined Ascii Symbols
    OPEN_CLOSE_SQUARE(14,"[[]]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    OPEN_CLOSE_PARENT(15,"[()]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    OPEN_CLOSE_LESSER_GREATER(16,"[<>]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    ASSIGNMENT(17,"[ass]", TokenClass.SYMBOL, CodeColor.DEFAULT), // = += -= *= /= %= &= ^= |= <<= >>= >>>=
    SHIFT(18,"[shift]", TokenClass.SYMBOL, CodeColor.DEFAULT), //<< >> >>>
    EQUALITY(19,"[equ]", TokenClass.SYMBOL, CodeColor.DEFAULT), // == !=
    LOGICAL_AND(20,"[&&]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    LOGICAL_OR(21,"[||]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    GREATER_EQUALS(22,"[>=]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    LESSER_EQUALS(23,"[<=]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    INCREMENT(24,"[++]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    DECREMENT(25,"[--]", TokenClass.SYMBOL, CodeColor.DEFAULT),

    // Singleton Ascii Symbols
    EXCLAMATION_MARK(33,"!", TokenClass.SYMBOL, CodeColor.DEFAULT),
    QUOTATION_MARK(34,"\"", TokenClass.SYMBOL, CodeColor.DEFAULT),
    HASH_SIGN(35,"#", TokenClass.SYMBOL, CodeColor.DEFAULT),
    DOLLAR_SIGN(36,"$", TokenClass.SYMBOL, CodeColor.DEFAULT),
    MODULO(37,"%", TokenClass.SYMBOL, CodeColor.DEFAULT),
    AMPERSAND(38,"&", TokenClass.SYMBOL, CodeColor.DEFAULT),
    APOSTROPHE(39,"\'", TokenClass.SYMBOL, CodeColor.DEFAULT),
    OPEN_PARENT(40,"(", TokenClass.SYMBOL, CodeColor.DEFAULT),
    CLOSE_PARENT(41,")", TokenClass.SYMBOL, CodeColor.DEFAULT),
    ASTERISK(42,"*", TokenClass.SYMBOL, CodeColor.DEFAULT),
    PLUS(43,"+", TokenClass.SYMBOL, CodeColor.DEFAULT),
    COMMA(44,",", TokenClass.SYMBOL, CodeColor.DEFAULT),
    MINUS(45,"-", TokenClass.SYMBOL, CodeColor.DEFAULT),
    DOT(46,".", TokenClass.SYMBOL, CodeColor.DEFAULT),
    SLASH(47,"/", TokenClass.SYMBOL, CodeColor.DEFAULT),
    COLON(58,":", TokenClass.SYMBOL, CodeColor.DEFAULT),
    SEMICOLON(59,";", TokenClass.SYMBOL, CodeColor.DEFAULT),
    LESS_THAN(60,"<", TokenClass.SYMBOL, CodeColor.DEFAULT),
    EQUALS(61,"=", TokenClass.SYMBOL, CodeColor.DEFAULT),
    GREATER_THAN(62,">", TokenClass.SYMBOL, CodeColor.DEFAULT),
    QUESTION_MARK(63,"?", TokenClass.SYMBOL, CodeColor.DEFAULT),
    AT_SIGN(64,"@", TokenClass.SYMBOL, CodeColor.DEFAULT),
    OPEN_SQUARE(91,"[", TokenClass.SYMBOL, CodeColor.DEFAULT),
    BACKSLASH(92,"\\", TokenClass.SYMBOL, CodeColor.DEFAULT),
    CLOSE_SQUARE(93,"]", TokenClass.SYMBOL, CodeColor.DEFAULT),
    CARET(94,"^", TokenClass.SYMBOL, CodeColor.DEFAULT),
    UNDERSCORE(95,"_", TokenClass.SYMBOL, CodeColor.DEFAULT),
    GRAVE_ASCENT(96,"`", TokenClass.SYMBOL, CodeColor.DEFAULT),
    OPEN_CURLY(123,"{", TokenClass.SYMBOL, CodeColor.DEFAULT),
    VERTICAL_BAR(124,"|", TokenClass.SYMBOL, CodeColor.DEFAULT),
    CLOSE_CURLY(125,"}", TokenClass.SYMBOL, CodeColor.DEFAULT),
    TILDE(126,"~", TokenClass.SYMBOL, CodeColor.DEFAULT);

    public static final int num = 26;
    public static TokenType[] all = values();
    public static TokenType byIdentifier(int id) {
        if (id >= 0) {
            if (id < num) {
                return all[id];
            } if (id > 32 && id < 127) {
                int new_value = id - (33 - num);
                if (id >= 58) new_value -= 10;
                if (id >= 91) new_value -= 26;
                if (id >= 123) new_value -= 26;
                return all[new_value];}
        } return TokenType.UNASSIGNED;
    }

    public final CodeColor highlight;
    public final TokenClass classifier;
    public final String debug_string;
    public final int identifier;
    TokenType(int identifier, String debug_string,
        TokenClass classifier, CodeColor highlight) {
        this.classifier = classifier;
        this.debug_string = debug_string;
        this.identifier = identifier;
        this.highlight = highlight;
    }


}
