package io.github.heathensoft.jlib.ui.window;

import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.ui.HudAssets;
import org.joml.Vector2f;

/**
 * base interface for ui components
 * @author Frederik Dahl
 * 23/11/2022
 */


public interface UiComponent {

    /**
     * update method if needed
     * @param dt delta time
     */
    void update(float dt);

    /**
     * Render the component.
     * @param batch batch to draw to
     * @param assets assets to choose from
     * @param posX top left corner x (as set by parent component)
     * @param posY top left corner y (as set by parent component)
     */
    void render(SpriteBatch batch, HudAssets assets, float posX, float posY);

    default void render(SpriteBatch batch, HudAssets assets, Vector2f position) {
        render(batch,assets,position.x, position.y);
    }
}
