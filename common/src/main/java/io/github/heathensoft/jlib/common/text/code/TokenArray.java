package io.github.heathensoft.jlib.common.text.code;

import io.github.heathensoft.jlib.common.text.Ascii;

import java.util.Arrays;

/**
 * @author Frederik Dahl
 * 14/05/2024
 */


public class TokenArray {

    private static final int[] EMPTY_ARRAY = new int[0];
    private static final int TOKEN_SIZE = 3;
    private static final int TYP_OFFSET = 0;
    private static final int STA_OFFSET = 1;
    private static final int END_OFFSET = 2;
    private static final int GROW_BY = TOKEN_SIZE * 256;
    private int[] tokens = EMPTY_ARRAY;
    private int num_tokens;
    private int token_pointer; // index of token type
    private int stored_token_pointer;

    public boolean isEmpty() { return num_tokens == 0; }
    public void storeToken() { stored_token_pointer = token_pointer; }
    public void restoreToken() { token_pointer = stored_token_pointer; }
    public void rewind() { token_pointer = 0; }
    public int numTokens() { return num_tokens; }
    public int tokenIndex() { return token_pointer / TOKEN_SIZE; }
    public int tokenType() { return tokens[token_pointer + TYP_OFFSET]; }
    public int tokenStart() { return tokens[token_pointer + STA_OFFSET]; }
    public int tokenEnd() { return tokens[token_pointer + END_OFFSET]; }
    public void setType(int type) { tokens[token_pointer + TYP_OFFSET] = type; }
    public void setStart(int start) { tokens[token_pointer + STA_OFFSET] = start; }
    public void setEnd(int end) {
        tokens[token_pointer + END_OFFSET] = end;
    }


    // Will also set current token -> appended token
    public void appendToken() {
        int len = num_tokens * TOKEN_SIZE;
        int cap = tokens.length;
        if (len >= cap) tokens = Arrays.copyOf(tokens,cap + GROW_BY);
        token_pointer = len;
        num_tokens++;
    }

    public boolean prevToken() {
        if (token_pointer > 0) {
            token_pointer -= TOKEN_SIZE;
            return true;
        } return false;
    }

    public boolean nextToken() {
        int lim = (num_tokens - 1) * TOKEN_SIZE;
        if (token_pointer < lim) {
            token_pointer += TOKEN_SIZE;
            return true;
        }return false;
    }

    public String tokenToString(Ascii.Buffer source) {
        int sta = tokens[token_pointer + 1];
        int end = tokens[token_pointer + 2];
        return source.toString(sta,end);
    }

    public void debugPrintContents(Ascii.Buffer source) {
        if (!isEmpty()) {
            int tmp = token_pointer;
            int len = numTokens();
            int src_len = source.length();
            int pointer = 0;
            for (int i = 0; i < len; i++) {
                if (pointer >= src_len) break;
                int typ = tokens[pointer];
                int sta = tokens[pointer + 1];
                int end = tokens[pointer + 2];
                String txt = source.toString(sta,end-sta);
                System.out.println("["+typ+"]  " + txt);
                pointer += TOKEN_SIZE;
            }
        }
    }

    public String toString() {
        if (isEmpty()) return "";
        int len = numTokens();
        StringBuilder builder = new StringBuilder(len * 4); // approx.
        for (int i = 0; i < len; i++) {
            int type = tokens[i*TOKEN_SIZE];
            builder.append(TokenType.byIdentifier(type).debug_string);
        } return builder.toString();
    }
}
