package io.github.heathensoft.jlib.gui.window.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.GUIGraphics;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;

/**
 * @author Frederik Dahl
 * 23/12/2022
 */


public interface BoxContent extends Disposable {

    void render(SpriteBatch batch, GUIGraphics graphics, int x0, int y0);

    void update(float dt);
}
