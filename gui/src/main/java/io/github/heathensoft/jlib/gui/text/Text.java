package io.github.heathensoft.jlib.gui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public class Text extends ParagraphList {

    private final List<Paragraph> list;

    public Text() {
        list = new ArrayList<>();
    }

    public Text(String text) {
        list = parse(text);
    }

    public boolean isBlank() {
        return list.isEmpty();
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
                result.add(Paragraph.parse(line));
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
