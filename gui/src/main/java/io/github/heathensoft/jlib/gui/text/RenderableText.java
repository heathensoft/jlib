package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.gui.gfx.TextBatchGUI;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 21/10/2023
 */


public interface RenderableText {

    void draw(TextBatchGUI batch, Rectanglef bounds);

    boolean isBlank();
}
