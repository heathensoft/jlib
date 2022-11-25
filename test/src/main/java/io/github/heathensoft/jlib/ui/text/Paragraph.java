package io.github.heathensoft.jlib.ui.text;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class Paragraph {
    
    private final List<Word> words;
    
    protected Paragraph(String string) {
        String[] array = string.split("\\s+");
        words = new ArrayList<>(array.length);
        for (String value : array) {
            words.add(new Word(value.getBytes(
            StandardCharsets.US_ASCII)));}
    }
    
    public List<Word> words() {
        return words;
    }
    
}
