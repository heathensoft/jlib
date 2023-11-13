package io.github.heathensoft.jlib.gui.deprecated;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Frederik Dahl
 * 20/09/2023
 */


abstract class ParagraphList<T extends Paragraph> extends Text<T> {

    protected final List<T> list;

    public ParagraphList() {
        this(16);
    }

    public ParagraphList(int capacity) {
        if (this instanceof ParsedText)
        list = new ArrayList<>(capacity);
        else list = new LinkedList<>();
    }

    public ParagraphList(String text) {
        list = parse(text);
    }

    protected ParagraphList(List<T> list) {
        this.list = list;
    }

    public void add(String string) {
        add(paragraphOf(string));
    }

    public void add(List<T> list) {
        for (T paragraph : list) add(paragraph);
    }

    public void add(T paragraph) {
        list.add(paragraph);
    }

    public void add(T paragraph, int index) { list.add(index,paragraph); }

    public void insert(T paragraph, int index) {
        list.add(index,paragraph);
    }

    public T get(int index) {
        return list.get(index);
    }

    public T remove(int index) {
        return list.remove(index);
    }

    public void clear() {
        list.clear();
    }

    public int size() {
        return list.size();
    }

    public boolean isBlank() {
        return list.isEmpty();
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    protected abstract T paragraphOf(String string);

    protected List<T> parse(String text) {
        if (this instanceof ParsedText) {
            ArrayList<T> result;
            if (text == null || text.isBlank()) {
                result = new ArrayList<>(0);
            } else { text = text.trim();
                List<String> lines = text.lines().collect(Collectors.toList());
                result = new ArrayList<>(lines.size());
                for (String line : lines) {
                    result.add(paragraphOf(line));
                } if (!result.isEmpty()) { // cutting out trailing new-lines:
                    while (result.size() > 0) {
                        int num_paragraphs = result.size();
                        Paragraph paragraph = result.get(num_paragraphs - 1);
                        if (paragraph.isBlank()) {
                            result.remove(num_paragraphs - 1);
                        } else break; }
                } result.trimToSize();
            } return result;
        } else {
            LinkedList<T> result;
            if (text == null || text.isBlank()) {
                result = new LinkedList<>();
            } else { text = text.trim();
                List<String> lines = text.lines().collect(Collectors.toList());
                result = new LinkedList<>();
                for (String line : lines) {
                    result.add(paragraphOf(line));
                } if (!result.isEmpty()) { // cutting out trailing new-lines:
                    while (result.size() > 0) {
                        int num_paragraphs = result.size();
                        Paragraph paragraph = result.get(num_paragraphs - 1);
                        if (paragraph.isBlank()) {
                            result.remove(num_paragraphs - 1);
                        } else break; }
                }
            } return result;
        }

    }
}
