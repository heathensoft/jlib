package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.gui.gfx.TextBatch;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 21/10/2023
 */


public interface Text {

    void draw(TextBatch batch, Rectanglef quad, int font, int size);

    boolean isBlank();
}
