package io.github.heathensoft.jlib.common.text.code;

import io.github.heathensoft.jlib.common.ChainEvent;
import io.github.heathensoft.jlib.common.text.Ascii;

/**
 * Tokenizes code. Breaking the raw source into logical parts.
 *
 * @author Frederik Dahl
 * 14/05/2024
 */


public class GenericScanner {

    private final Ascii.Trie keywords;
    private final Ascii.Trie datatypes;
    private TokenArray tokens;
    private int src_len;
    private int index;

    GenericScanner(Ascii.Trie keywords, Ascii.Trie datatypes) {
        this.datatypes = datatypes;
        this.keywords = keywords;
    }

    private void nextCharacter(int jump) { index += jump; }
    private void nextCharacter() { index++; }
    private void prevCharacter() { index--; }
    private boolean notAtEnd() { return index < src_len; }
    private boolean atTheEnd() { return index == src_len; }

    public TokenArray tokenize(Ascii.Buffer source) {
        TokenArray tokens = new TokenArray();
        if (source != null && source.length() > 0) {
            this.src_len = source.length();
            this.tokens = tokens;
            this.index = 0;
            ChainEvent<Ascii.Buffer> next = this::nextToken;
            while (next != null) next = next.apply(source);
            this.tokens = null;
        } return tokens;
    }


    private ChainEvent<Ascii.Buffer> nextToken(Ascii.Buffer source) {
        if (!tokens.isEmpty()) tokens.setEnd(index);
        while (notAtEnd()) {
            int c = source.get(index);
            if (c > 32) break;
            nextCharacter();
        } if (atTheEnd()) return null;
        tokens.appendToken();
        tokens.setStart(index);
        int c = source.get(index);
        if (isIdentifierStart(c)) return this::processLetters;
        else if (isNumberStart(c)) return this::processNumerals;
        else if (isSymbol_33_47(c)) return this::processAscii_33_47;
        else if (isSymbol_58_64(c)) return this::processAscii_58_64;
        else if (isSymbol_91_96(c)) return this::processAscii_91_96;
        else if (isSymbol_123_126(c)) return this::processAscii_123_126;
        throw new IllegalStateException("");
    }

    private ChainEvent<Ascii.Buffer> processLetters(Ascii.Buffer source) {
        tokens.setType(TokenType.IDENTIFIER.identifier);
        while (true) { nextCharacter();
            if (atTheEnd()) break;
            int c = source.get(index);
            if (!isIdentifierContinue(c)) break;
        } int prev = source.get(index - 1);
        if (prev == 46) prevCharacter();
        return this::processIdentifier;
    }

    private ChainEvent<Ascii.Buffer> processIdentifier(Ascii.Buffer source) {
        int type = tokens.tokenType();
        if (type == TokenType.IDENTIFIER.identifier) {
            int start = tokens.tokenStart();
            if (datatypes.containsWord(source,start,index)) {
                tokens.setType(TokenType.DATATYPE.identifier);
            } else if (keywords.containsWord(source,start,index)){
                tokens.setType(TokenType.KEYWORD.identifier);
            } else if (containsWord(bool_array,source,start,index)){
                tokens.setType(TokenType.BOOLEAN.identifier);
            } else { tokens.storeToken();
                if (tokens.prevToken()) {
                    int prev_type = tokens.tokenType();
                    if (prev_type == 64 || prev_type == 35) { // @ #
                        tokens.setType(TokenType.SPECIAL.identifier);
                        tokens.restoreToken();
                        tokens.setType(TokenType.SPECIAL.identifier);
                    } else tokens.restoreToken();
                } if (tokens.tokenType() != TokenType.SPECIAL.identifier) {
                    if (identifierAllCaps(source,start,index)) {
                        tokens.setType(TokenType.SPECIAL.identifier);
                    }
                }
            } return this::nextToken;
        } throw new IllegalStateException("");
    }

    private ChainEvent<Ascii.Buffer> processNumerals(Ascii.Buffer source) {
        tokens.setType(TokenType.INTEGER_DECIMAL.identifier);
        while (true) {
            nextCharacter();
            if (atTheEnd()) break;
            int c = source.get(index);
            if (isNumberContinue(c)) continue;
            int prev = source.get(index - 1);
            if (c == 46) return this.processDecimal(source);
            if (c == 88 || c == 120) {
                if (prev == 95) break;
                if (prev == 48) {  // 0x | 0X
                    int start = tokens.tokenStart();
                    if (index - start == 1) // len == 2
                        return this::processHexadecimal;
                } return this::nextToken;
            } if (c == 66 || c == 98) {
                if (prev == 95) break;
                if (prev == 48) {  // 0b | 0B
                    int start = tokens.tokenStart();
                    if (index - start == 1) // len == 2
                        return this::processBinary;
                } return this::nextToken;
            } if (c == 69 || c == 101) { // e | E
                if (prev == 95) break;
                return this::processExponential;}
            else break;
        }
        return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processDecimal(Ascii.Buffer source) {
        tokens.setType(TokenType.FLOATING_POINT.identifier);
        nextCharacter();
        if (notAtEnd()) {
            int c = source.get(index);
            if (isNumberStart(c)) {
                while (notAtEnd()) {
                    nextCharacter();
                    c = source.get(index);
                    if (!isNumberContinue(c)) break;
                }  int prev = source.get(index - 1);
                if (prev == 95) {
                    prevCharacter();
                    c = prev;
                }
            }
            if (notAtEnd()) {
                if (c == 70 || c == 102) { // f
                    nextCharacter();
                } else if (c == 68 || c == 100) { // d
                    nextCharacter();
                }
            }
        } return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processBinary(Ascii.Buffer source) {
        tokens.setType(TokenType.BINARY.identifier);
        nextCharacter();
        if (notAtEnd()) {
            int c = source.get(index);
            if (isBinaryStart(c)) {
                while (notAtEnd()) {
                    nextCharacter();
                    c = source.get(index);
                    if (!isBinaryContinue(c)) break;
                } int prev = source.get(index - 1);
                if (prev == 95) {
                    prevCharacter();
                    c = prev;
                }
            }
            if (notAtEnd() && c == 76) { // L
                nextCharacter();

            }
        } return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processHexadecimal(Ascii.Buffer source) {
        tokens.setType(TokenType.HEXADECIMAL.identifier);
        nextCharacter();
        if (notAtEnd()) {
            int c = source.get(index);
            if (isHexStart(c)) {
                while (notAtEnd()) {
                    nextCharacter();
                    c = source.get(index);
                    if (!isHexContinue(c)) break;
                } int prev = source.get(index - 1);
                if (prev == 95) {
                    prevCharacter();
                    c = prev;
                }
            }
            if (notAtEnd() && c == 76) { // L
                nextCharacter();

            }
        } return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processExponential(Ascii.Buffer source) {
        tokens.setType(TokenType.EXPONENTIAL.identifier);
        nextCharacter();
        double d = 1e+1f;
        if (notAtEnd()) {
            int c = source.get(index);
            if (c == 43 || c == 45) nextCharacter();
            if (notAtEnd()) {
                c = source.get(index);
                if (isNumberStart(c)) {
                    nextCharacter();
                    while (notAtEnd()) {
                        c = source.get(index);
                        if (!isNumberContinue(c)) break;
                        nextCharacter();
                    } int prev = source.get(index - 1);
                    if (prev == 95) {
                        prevCharacter();
                    } else if (notAtEnd()) {
                        if (c == 70 || c == 102) { // f
                            nextCharacter();
                        } else if (c == 68 || c == 100) { // d
                            nextCharacter();
                        }
                    }
                }
            }
        } return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processAscii_33_47(Ascii.Buffer source) {
        int c = source.get(index); // " ' /* () != %= &= && *= += ++ -= --
        tokens.setType(c);
        if (c == 33) { // !=
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 61) {
                    tokens.setType(TokenType.EQUALITY.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        }
        if (c == 34) return this::processString;
        if (c == 37) { // %=
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 61) {
                    tokens.setType(TokenType.ASSIGNMENT.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        } if (c == 38) { // &= , &&
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 61) {
                    tokens.setType(TokenType.ASSIGNMENT.identifier);
                    nextCharacter();
                } else if (c == 38) {
                    tokens.setType(TokenType.LOGICAL_AND.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        }
        if (c == 39) return this::processCharString;
        if (c == 42) { // *=
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 61) {
                    tokens.setType(TokenType.ASSIGNMENT.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        } if (c == 43) { // += , ++
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 61) {
                    tokens.setType(TokenType.ASSIGNMENT.identifier);
                    nextCharacter();
                } else if (c == 43) {
                    tokens.setType(TokenType.INCREMENT.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        } if (c == 45) { // -= , --
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 61) {
                    tokens.setType(TokenType.ASSIGNMENT.identifier);
                    nextCharacter();
                } else if (c == 45) {
                    tokens.setType(TokenType.DECREMENT.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        }
        if (c == 47) { // // /* /=
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 42) return this::processComment;
                if (c == 47) return this::processOneLineComment;
                if (c == 61) {
                    tokens.setType(TokenType.ASSIGNMENT.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        } if (c == 40) {
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 41) {
                    tokens.setType(TokenType.OPEN_CLOSE_PARENT.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        }
        else {
            nextCharacter();
            return this::nextToken;
        }
    }

    private ChainEvent<Ascii.Buffer> processAscii_58_64(Ascii.Buffer source) {
        int c = source.get(index); // << <<< >>> >> <= >= <> <<= >>= >>>=
        tokens.setType(c);
        if (c == 60) {
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 60) {
                    tokens.setType(TokenType.SHIFT.identifier);
                    nextCharacter();
                    if (!atTheEnd()) {
                        c = source.get(index);
                        if (c == 60) {
                            nextCharacter();
                            if (!atTheEnd()) {
                                c = source.get(index);
                                if (c == 61) {
                                    tokens.setType(TokenType.ASSIGNMENT.identifier);
                                    nextCharacter();
                                }
                            }
                        } else if (c == 61) {
                            tokens.setType(TokenType.ASSIGNMENT.identifier);
                            nextCharacter();
                        }
                    }
                } else if (c == 61) {
                    tokens.setType(TokenType.LESSER_EQUALS.identifier);
                    nextCharacter();
                } else if (c == 62) {
                    tokens.setType(TokenType.OPEN_CLOSE_LESSER_GREATER.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        } if (c == 61) { // = , ==
            tokens.setType(TokenType.ASSIGNMENT.identifier);
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 61) {
                    tokens.setType(TokenType.EQUALITY.identifier);
                    nextCharacter();
                }
            }
            return this::nextToken;
        } if (c == 62) {
            nextCharacter();
            if (!atTheEnd()) {
                c = source.get(index);
                if (c == 62) {
                    tokens.setType(TokenType.SHIFT.identifier);
                    nextCharacter();
                    if (!atTheEnd()) {
                        c = source.get(index);
                        if (c == 62) {
                            nextCharacter();
                            if (!atTheEnd()) {
                                c = source.get(index);
                                if (c == 61) {
                                    tokens.setType(TokenType.ASSIGNMENT.identifier);
                                    nextCharacter();
                                }
                            }
                        } else if (c == 61) {
                            tokens.setType(TokenType.ASSIGNMENT.identifier);
                            nextCharacter();
                        }
                    }
                } else if (c == 61) {
                    tokens.setType(TokenType.GREATER_EQUALS.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        }
        nextCharacter();
        return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processAscii_91_96(Ascii.Buffer source) {
        int c = source.get(index); // [] , ^=
        tokens.setType(c);
        if (c == 91) { // []
            nextCharacter();
            if (notAtEnd()) {
                c = source.get(index);
                if (c == 93) {
                    tokens.setType(TokenType.OPEN_CLOSE_SQUARE.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        } if (c == 94) { // ^=
            nextCharacter();
            if (notAtEnd()) {
                c = source.get(index);
                if (c == 61) {
                    tokens.setType(TokenType.EQUALITY.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        }
        else {
            nextCharacter();
            return this::nextToken;
        }

    }

    private ChainEvent<Ascii.Buffer> processAscii_123_126(Ascii.Buffer source) {
        int c = source.get(index); // ||, |=
        tokens.setType(c);
        if (c == 124) { // |
            nextCharacter();
            c = source.get(index);
            if (notAtEnd()) {
                if (c == 61) {
                    tokens.setType(TokenType.EQUALITY.identifier);
                    nextCharacter();
                } else if (c == 124) {
                    tokens.setType(TokenType.LOGICAL_OR.identifier);
                    nextCharacter();
                }
            } return this::nextToken;
        }  else {
            nextCharacter();
            return this::nextToken;
        }
    }


    private ChainEvent<Ascii.Buffer> processString(Ascii.Buffer source) {
        tokens.setType(TokenType.STRING.identifier);
        nextCharacter();
        while (notAtEnd()) {
            int c = source.get(index);
            if (c == 10) break;
            if (c == 34) {
                int prev = source.get(index - 1);
                if (prev != 92) {
                    nextCharacter();
                    break;
                }
            } nextCharacter();
        } return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processCharString(Ascii.Buffer source) {
        tokens.setType(TokenType.CHAR.identifier);
        nextCharacter();
        while (notAtEnd()) {
            int c = source.get(index);
            if (c == 10) break;
            if (c == 39) {
                int prev = source.get(index - 1);
                if (prev != 92) {
                    nextCharacter();
                    break;
                }
            } nextCharacter();
        } return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processOneLineComment(Ascii.Buffer source) {
        tokens.setType(TokenType.COMMENT.identifier);
        nextCharacter();
        while (notAtEnd()) {
            if (source.get(index) == 10) break;
            nextCharacter();
        } return this::nextToken;
    }

    private ChainEvent<Ascii.Buffer> processComment(Ascii.Buffer source) {
        tokens.setType(TokenType.COMMENT.identifier);
        nextCharacter();
        while (notAtEnd()) {
            int c = source.get(index);
            if (c == 42) {
                nextCharacter();
                if (atTheEnd()) break;
                c = source.get(index);
                if (c == 47) {
                    nextCharacter();
                    break;
                } else prevCharacter();
            } nextCharacter();
        } return this::nextToken;
    }


    private static final int[][] bool_array = {
        {116,114,117,101}, {102,97,108,115,101}
    };

    private boolean containsWord(int[][] words, Ascii.Buffer source, int start, int end) {
        int src_len = source.length();
        Ascii.checkArrayRange(start,end,src_len);
        int len = end - start;
        int num_words = words.length;
        for (int[] word : words) {
            int word_len = word.length;
            if (len == word_len) {
                int found = 0;
                for (int i = 0; i < word_len; i++) {
                    int c = source.get(start + i);
                    if (c != word[i]) break; found++;
                } if (found == len) return true;
            }
        } return false;
    }

    private boolean identifierAllCaps(Ascii.Buffer source, int start, int end) {
        for (int i = start; i < end; i++) {
            int c = source.get(i);
            if (isUppercaseLetter(c) || isNumberContinue(c)) continue;
            return false;
        } return true;
    }

    private static boolean isWhiteSpace(int c) { return c < 33; }
    private static boolean isLetter(int c) { return isLowercaseLetter(c) || isUppercaseLetter(c); }
    private static boolean isHexStart(int c) { return isNumberStart(c) || (c > 64 && c < 71) || (c > 96 && c < 103); }
    private static boolean isIdentifierStart(int c) { return isLetter(c) || c == 95; }
    private static boolean isNumberStart(int c) { return c < 58 && c > 47; }
    private static boolean isBinaryStart(int c) { return c == 48 || c == 49; }
    private static boolean isIdentifierContinue(int c) { return isLetter(c) || isNumberContinue(c) || c == 95; }
    private static boolean isHexContinue(int c) { return isHexStart(c) || c == 95; }
    private static boolean isBinaryContinue(int c) { return isBinaryStart(c) || c == 95; }
    private static boolean isNumberContinue(int c) { return isNumberStart(c) || c == 95; }
    private static boolean isUppercaseLetter(int c) { return c > 64 && c < 91; }
    private static boolean isLowercaseLetter(int c) {return c > 96 && c < 123; }
    private static boolean isSymbol_33_47(int c) { return c > 32 && c < 48; }
    private static boolean isSymbol_58_64(int c) { return c > 57 && c < 65; }
    private static boolean isSymbol_91_96(int c) { return c > 90 && c < 97; }
    private static boolean isSymbol_123_126(int c) { return c > 122; }
}
