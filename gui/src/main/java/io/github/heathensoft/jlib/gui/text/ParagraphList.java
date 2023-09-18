package io.github.heathensoft.jlib.gui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public class ParagraphList extends Text {

    private List<Paragraph> list;

    public ParagraphList() {
        this(16);
    }

    public ParagraphList(int capacity) {
        list = new ArrayList<>(capacity);
    }

    public ParagraphList(String text) {
        list = parse(text);
    }

    protected ParagraphList(List<Paragraph> list) {
        this.list = list;
    }

    public void set(String string) {
        list = parse(string);
    }

    public boolean isBlank() {
        return list.isEmpty();
    }

    public void add(List<Paragraph> list) {
        for (Paragraph paragraph : list) add(paragraph);
    }

    public void add(Paragraph paragraph) {
        list.add(paragraph);
    }

    public void clear() {
        list.clear();
    }

    public int size() {
        return list.size();
    }

    public Iterator<Paragraph> iterator() {
        return list.iterator();
    }

    private List<Paragraph> parse(String text) {
        ArrayList<Paragraph> result;
        if (text == null || text.isBlank()) {
            result = new ArrayList<>(0);
        } else { text = text.trim();
            List<String> lines = text.lines().collect(Collectors.toList());
            result = new ArrayList<>(lines.size());
            for (String line : lines) {
                result.add(Paragraph.create(line));
            } if (!result.isEmpty()) { // cutting out trailing new-lines:
                while (result.size() > 0) {
                    int num_paragraphs = result.size();
                    Paragraph paragraph = result.get(num_paragraphs - 1);
                    if (paragraph.isBlank()) {
                        result.remove(num_paragraphs - 1);
                    } else break; }
            } result.trimToSize();
        } return result;
    }


}
