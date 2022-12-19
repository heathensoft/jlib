package io.github.heathensoft.jlib.hud.ui.text;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Frederik Dahl
 * 01/12/2022
 */


public class Text {

    private final List<Paragraph> paragraphs;

    public Text(String string, boolean searchKeywords) {
        string = string == null ? "" : string;
        List<String> strings = string.lines().collect(Collectors.toList());
        paragraphs = new ArrayList<>(strings.size());
        for (String s : strings) paragraphs.add(new Paragraph(s,searchKeywords));
    }

    public List<Paragraph> paragraphs() {
        return paragraphs;
    }

    /**
     * Calculates height of virtual text-field
     * @param font font
     * @param containerWidth in pixels
     * @param scale discrete scale of font (1 is 1:1)
     * @return estimated height of the text in character units (lines)
     */
    // store height, and do this on resize
    public int calculateHeight(AsciiFont font, int containerWidth, int scale) {
        if (scale == 0) return 0;
        if (font.isMonoSpaced()) {
            int font_advance = font.getChar('A').advance() * scale;
            int widthInChar = (containerWidth / font_advance);
            int px = 0; int py = 0;
            for (Paragraph paragraph : paragraphs) {
                py++;
                for (Word word : paragraph.words()) {
                    int l = word.length();
                    if (px + l > widthInChar) {
                        px = 0;
                        py++;
                    } /* + 1 for space (as in space-bar ) */
                    px += l + 1;
                }
            } return py;
        }
        else {
            /* not monospaced */
            return 0;
        }

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