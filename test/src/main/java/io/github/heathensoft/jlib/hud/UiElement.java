package io.github.heathensoft.jlib.hud;

import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.ui.HudAssets;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 23/11/2022
 */


public interface UiElement {

    void update(float dt);

    void render(SpriteBatch batch, HudAssets assets, Vector2f position);
}
