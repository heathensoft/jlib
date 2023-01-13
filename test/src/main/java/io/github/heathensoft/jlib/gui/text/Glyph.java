package io.github.heathensoft.jlib.gui.text;


import io.github.heathensoft.jlib.graphicsOld.TextureRegion;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public record Glyph(TextureRegion region, AsciiFont font, int advance) { }
