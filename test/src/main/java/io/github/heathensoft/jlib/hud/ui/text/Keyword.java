package io.github.heathensoft.jlib.hud.ui.text;


import java.nio.charset.StandardCharsets;

/**
 * @author Frederik Dahl
 * 29/11/2022
 */


public class Keyword extends Word {

    private final InteractableWord iWord;

    protected Keyword(InteractableWord iWord) {
        super(iWord.keyword().getBytes(StandardCharsets.US_ASCII));
        this.iWord = iWord;
    }

    public InteractableWord interactable() {
        return iWord;
    }
}
