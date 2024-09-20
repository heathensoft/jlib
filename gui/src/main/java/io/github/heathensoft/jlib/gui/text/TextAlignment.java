package io.github.heathensoft.jlib.gui.text;

/**
 * @author Frederik Dahl
 * 22/02/2024
 */


public enum TextAlignment {
    LEFT(false),
    RIGHT(false),
    CENTERED(true);
    public final boolean centered;
    TextAlignment(boolean centered) {
        this.centered = centered;
    }
    public static TextAlignment DEFAULT = LEFT;
}
