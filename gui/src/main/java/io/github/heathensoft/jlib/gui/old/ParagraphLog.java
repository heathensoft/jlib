package io.github.heathensoft.jlib.gui.old;

import io.github.heathensoft.jlib.common.storage.generic.FixedStack;

import java.util.Iterator;
import java.util.ListIterator;

/**
 * @author Frederik Dahl
 * 20/09/2023
 */


public class ParagraphLog extends Text<ParsedParagraph> {

    private final FixedStack<ParsedParagraph> stack;

    public ParagraphLog(int capacity) {
        stack = new FixedStack<>(capacity);
    }

    public void logText(ParsedText text) {
        ListIterator<ParsedParagraph> itr;
        itr = text.list.listIterator(text.list.size());
        while (itr.hasPrevious()) log(itr.previous());
    }

    public void logText(String string) {
        logText(new ParsedText(string));
    }

    public void log(ParsedParagraph paragraph) {
        stack.push(paragraph);
    }

    public void log(String string) {
        stack.push(TextUtils.stringToParsed(string));
    }

    public void clear() {
        stack.clear();
    }

    public int size() {
        return stack.size();
    }

    public boolean isBlank() {
        return stack.isEmpty();
    }

    public Iterator<ParsedParagraph> iterator() {
        return stack.iterator();
    }
}
