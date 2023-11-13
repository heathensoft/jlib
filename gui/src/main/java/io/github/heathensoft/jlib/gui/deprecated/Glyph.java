package io.github.heathensoft.jlib.gui.deprecated;

import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;

/**
 * @author Frederik Dahl
 * 05/09/2023
 */


public class Glyph extends TextureRegion {

    private final byte character;

    public Glyph(TextureRegion region, byte character) {
        super(region);
        this.character = character;
    }

    public byte character() {
        return character;
    }

    public int advance() {
        return w();
    }

    public String toString() {
        return String.valueOf((char) character);
    }
}
