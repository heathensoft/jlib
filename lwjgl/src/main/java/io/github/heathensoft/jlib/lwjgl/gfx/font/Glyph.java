package io.github.heathensoft.jlib.lwjgl.gfx.font;

import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;

/**
 * @author Frederik Dahl
 * 01/10/2023
 */


public class Glyph extends TextureRegion {

    protected final float offsetX;
    protected final float offsetY;
    protected final float advance;
    protected final char character;

    public Glyph(TextureRegion region, char character, float offsetX, float offsetY, float advance) {
        super(region);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.advance = advance;
        this.character = character;
    }

    public float offsetX() {
        return offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    public float advance() {
        return advance;
    }

    public char character() {
        return character;
    }

    public String toString() {
        return Character.toString(character);
    }

}
