package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.lwjgl.gfx.Color32;

/**
 * @author Frederik Dahl
 * 15/09/2023
 */


public class PColored extends PSpecial {

    private final Color32 color;

    public PColored(String text, Color32 color) {
        super(text);
        this.color = color;
    }

    public Color32 color() {
        return color;
    }
}
