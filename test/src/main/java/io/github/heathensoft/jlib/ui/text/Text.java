package io.github.heathensoft.jlib.ui.text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class Text {
    
    private final List<Paragraph> paragraphs;
    
    public Text(String string) {
        string = string == null ? " " : string;
        List<String> strings = string.lines().collect(Collectors.toList());
        paragraphs = new ArrayList<>(strings.size());
        for (String s : strings) paragraphs.add(new Paragraph(s));
    }
    
    public List<Paragraph> paragraphs() {
        return paragraphs;
    }
    
    /**
     * @param containerWidth in the character width units
     * @return estimated height of the text;
     */
    public int calculateHeight(int containerWidth) { // store height, and do this on resize
        int px = 0;
        int py = 0;
        for (Paragraph paragraph : paragraphs) {
            py++;
            for (Word word : paragraph.words()) {
                int l = word.length();
                if (px + l > containerWidth) {
                    px = 0;
                    py++;
                } /* + 1 for space (as in space-bar ) */
                px += l + 1;
            }
        } return py;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        for (Paragraph p : paragraphs) {
            for (Word w : p.words()) {
                sb.append(w.toString()).append(' ');
            } sb.deleteCharAt(sb.length()-1);
            sb.append('\n');
        } sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}
