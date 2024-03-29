package io.github.heathensoft.jlib.ui.box;

import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;
import io.github.heathensoft.jlib.ui.text.Text;
import org.joml.Vector4f;

/**
 * @author Frederik Dahl
 * 20/03/2024
 */


public abstract class TextField extends Box implements TextProcessor {

    private static final int MIN_WIDTH = 16;
    private static final int MIN_HEIGHT = 16;
    private static final int DEFAULT_SB_WIDTH = 5;
    private static final int DEFAULT_FONT_SIZE = 22;

    private Text text;
    private Texture background_texture;
    private Vector4f background_color;
    private Vector4f scrollbar_color_active;
    private Vector4f scrollbar_color_inactive;


}
