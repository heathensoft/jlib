package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.common.storage.generic.FixedStack;

import java.util.Iterator;

/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public class EventLog extends ParagraphList {

    private final FixedStack<Paragraph> stack;

    public EventLog(int capacity) {
        stack = new FixedStack<>(capacity);
    }

    public boolean isBlank() {
        return stack.isEmpty();
    }

    public void add(Paragraph paragraph) {
        stack.push(paragraph);
    }

    public void clear() {
        stack.clear();
    }

    public int size() {
        return stack.size();
    }

    public Iterator<Paragraph> iterator() {
        return stack.iterator();
    }


}
