package io.github.heathensoft.jlib.hud.ui.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.hud.HudGraphics;
import io.github.heathensoft.jlib.hud.ui.Box;
import io.github.heathensoft.jlib.hud.ui.Size;
import io.github.heathensoft.jlib.hud.ui.Spacing;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;

import java.nio.charset.StandardCharsets;

/**
 * @author Frederik Dahl
 * 17/12/2022
 */


public class NavigationBar extends Box {

    public static final int button_size = 11;
    public static final int button_space = 1;
    public static final int button_text_padding = 4;
    public static final int nav_padding = 3;
    public static final int border_thickness = 1;
    public static final int letter_width = 8;
    public static final int letter_height = 16;

    private final HudWindow window;
    private final WindowDragZone dragZone;
    private final WindowCloseButton closeButton;
    private final WindowRestoreButton restoreButton;
    private final Size currentSize;
    private final Size desiredSize;

    NavigationBar(HudWindow window) {
        super(new Spacing(),new Spacing(0,0,border_thickness,0),new Spacing(nav_padding),
        window.navBackgroundColor,window.borderColor);
        this.restoreButton = new WindowRestoreButton(window);
        this.closeButton = new WindowCloseButton(window);
        this.dragZone = new WindowDragZone(window);
        int text_w = window.title().getBytes(StandardCharsets.US_ASCII).length * letter_width;
        int buttons_w = 2 * button_size + button_space;
        float desired_w = outerSize().width() + text_w + button_text_padding + buttons_w;
        float desired_h = outerSize().height() + Math.max(button_size,letter_height);
        this.desiredSize = new Size(desired_w,desired_h);
        this.currentSize = new Size(desiredSize);
        this.window = window;
    }

    public void update(float dt) {
        restoreButton.update(dt);
        closeButton.update(dt);
    }

    public void render(SpriteBatch batch, HudGraphics graphics, int x0, int y0) {
        drawBackground(batch,graphics,x0,y0,dragZone.iData());
        drawBorder(batch, graphics, x0, y0);
        drawText(batch, graphics, x0, y0);
        drawButtons(batch, graphics, x0, y0);
    }

    private void drawText(SpriteBatch batch, HudGraphics graphics, int x0, int y0) {
        Color color = window.navTextColor;
        if (color.a > 0) {
            x0 = x0 + padding.left();
            y0 = y0 - padding.top() - letter_height;
            byte[] title = window.title().getBytes(StandardCharsets.US_ASCII);
            for (int i = 0; i < title.length; i++) {
                float x = x0 + i * letter_width;
                TextureRegion region = graphics.getCharAmiga(title[i]);
                batch.draw(region,x,y0,letter_width,letter_height,
                color.toFloatBits(),dragZone.iData());
            }
        }
    }

    private void drawButtons(SpriteBatch batch, HudGraphics graphics, int x0, int y0) {
        int y = y0 - padding.top();
        int x = x0 + discreteWidth() - padding.right();
        x = x - button_size - button_space - button_size;
        drawRestoreButton(batch,graphics,x,y);
        x += button_size + button_space;
        drawCloseButton(batch,graphics,x,y);

    }

    private void drawCloseButton(SpriteBatch batch, HudGraphics graphics, int x0, int y0) {
        TextureRegion icon = graphics.coreAsset(HudGraphics.WINDOW_CLOSE_ICON);
        Color color = closeButton.hovered ? window.navCloseButtonHoverColor : window.navButtonColor;
        batch.draw(icon,x0,y0-button_size,button_size,button_size,color.toFloatBits(),closeButton.iData());
    }

    private void drawRestoreButton(SpriteBatch batch, HudGraphics graphics, int x0, int y0) {
        TextureRegion icon;
        if (window.isRestored()) icon = graphics.coreAsset(HudGraphics.WINDOW_MAXIMIZE_ICON);
        else icon = graphics.coreAsset(HudGraphics.WINDOW_RESTORE_ICON);
        Color color = restoreButton.hovered ? window.navRestoreButtonHoverColor : window.navButtonColor;
        batch.draw(icon,x0,y0-button_size,button_size,button_size,color.toFloatBits(),restoreButton.iData());
    }

    protected Area dragZone() {
        int x0 = window.x0() + HudWindow.border_thickness;
        int y1 = window.y0() - HudWindow.border_thickness;
        int y0 = y1 - (int)contentHeight();
        int x1 = x0 + (int)contentWidth();
        return new Area(x0,y0,x1,y1);
    }

    protected Size desiredSize() {
        return desiredSize;
    }

    protected Size currentSize() {
        return currentSize;
    }

    public void resizeHorizontal(float dx) {
        currentSize.addWidth(dx);
    }

    public void resizeVertical(float dy) {
        /* intentional */
    }

    public void restore() {
        currentSize.set(desiredSize);
    }

    public void dispose() {
        Disposable.dispose(closeButton,restoreButton,dragZone);
    }
}
