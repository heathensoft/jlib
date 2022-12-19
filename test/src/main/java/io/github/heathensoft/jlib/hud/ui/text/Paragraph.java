package io.github.heathensoft.jlib.hud.ui.text;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 01/12/2022
 */


public class Paragraph {

    private final List<Word> words;
    private boolean containsKeywords;

    protected Paragraph(String string, boolean searchKeywords) {
        if (searchKeywords) {
            words = InteractableWord.parseParagraph(string);
            for (Word word : words) {
                if (word instanceof Keyword) {
                    containsKeywords = true;
                    break;
                }
            }
        }
        else { String[] array = string.split("\\s+");
            words = new ArrayList<>(array.length);
            for (String value : array) {
                words.add(new Word(value.getBytes(
                StandardCharsets.US_ASCII)));
            }
        }
    }

    public boolean containsKeywords() {
        return containsKeywords;
    }

    public List<Word> words() {
        return words;
    }
}
