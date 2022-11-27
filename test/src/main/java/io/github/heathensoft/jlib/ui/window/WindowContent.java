package io.github.heathensoft.jlib.ui.window;

import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import io.github.heathensoft.jlib.ui.HudAssets;
import org.joml.Vector2f;

/**
 * @author Frederik Dahl
 * 27/11/2022
 */


public class WindowContent implements UiComponent {

    private Box content;
    private String title;
    private TextureRegion icon;
    private Color backgroundColor;
    private int backgroundTexture;
    private float backgroundScaling;
    private boolean resizable;

    public WindowContent() {

    }

    public void update(float dt) {
        content.update(dt);
    }

    public void render(SpriteBatch batch, HudAssets assets, float posX, float posY) {
        if (backgroundTexture != 0) {
            Size size = size();
            Texture texture = assets.texture(backgroundTexture);
            float w = size.width();
            float h = size.height();
            float u2 = w * backgroundScaling / (float) texture.width();
            float v2 = h * backgroundScaling / (float) texture.height();
            float color = backgroundColor.toFloatBits();
            batch.draw(0,0,u2,v2,posX,posY-size.height(),w,h,color,backgroundTexture);
        } content.render(batch, assets, posX, posY);
    }

    public void render(SpriteBatch batch, HudAssets assets, Vector2f position) {
        content.render(batch, assets, position);
    }

    public TextureRegion icon() {
        return icon;
    }

    public Size size() {
        return content.contentSize();
    }

    public String title() {
        return title;
    }

    public void resize(Vector2f vector) {
        if (resizable) content.resize(vector);
    }

    public void resizeHorizontal(float dx) {
        if (resizable) content.resizeHorizontal(dx);
    }

    public void resizeVertical(float dy) {
        if (resizable) content.resizeVertical(dy);
    }

    public void restore() {
        content.restore();
    }
}
