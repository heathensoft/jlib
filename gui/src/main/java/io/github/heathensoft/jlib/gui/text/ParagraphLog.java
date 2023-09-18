package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.common.storage.generic.FixedStack;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public class ParagraphLog extends Text {

    private final FixedStack<Paragraph> stack;

    public ParagraphLog(int capacity) {
        stack = new FixedStack<>(capacity);
    }

    public boolean isBlank() {
        return stack.isEmpty();
    }

    public void add(List<Paragraph> list) {
        ListIterator<Paragraph> itr = list.listIterator(list.size());
        while (itr.hasPrevious()) add(itr.previous());
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
