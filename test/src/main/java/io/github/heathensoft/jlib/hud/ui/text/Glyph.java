package io.github.heathensoft.jlib.hud.ui.text;

import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public class Glyph {

    // remember to round these before casting them to integers

    private TextureRegion character_region;
    private int advance;
    private int ascent;
    private int decent;


    public TextureRegion characterRegion() {
        return character_region;
    }

    public int advance() {
        return advance;
    }

    public int ascent() {
        return ascent;
    }

    public int decent() {
        return decent;
    }
}
