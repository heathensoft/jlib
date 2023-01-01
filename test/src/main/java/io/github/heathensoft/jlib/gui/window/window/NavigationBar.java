package io.github.heathensoft.jlib.gui.window.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.gui.GUIGraphics;
import io.github.heathensoft.jlib.gui.window.Box;
import io.github.heathensoft.jlib.gui.window.Size;
import io.github.heathensoft.jlib.gui.window.Spacing;
import io.github.heathensoft.jlib.gui.text.AsciiFont;
import io.github.heathensoft.jlib.gui.text.Glyph;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;

import java.nio.charset.StandardCharsets;

import static io.github.heathensoft.jlib.gui.GUI.*;


/**
 * @author Frederik Dahl
 * 17/12/2022
 */


public class NavigationBar extends Box {

    private final DynamicWindow window;
    private final NavButtons navButtons;
    private final WindowDragZone dragZone;
    private final Size currentSize;
    private final Size desiredSize;

    NavigationBar(DynamicWindow window) {
        super(new Spacing(),
                new Spacing(0,0,NAV_BAR_BORDER_THICKNESS,0),
                new Spacing(NAV_BAR_PADDING),
                NAV_BG_COLOR,WIN_BORDER_COLOR);
        this.navButtons = new NavButtons(window);
        this.dragZone = new WindowDragZone(window);
        int text_w = window.title().getBytes(StandardCharsets.US_ASCII).length * NAV_TXT_CHAR_WIDTH;
        int text_area_w = text_w + 2 * NAV_BAR_PADDING;
        int desired_h = Math.max(NAV_TXT_AREA_HEIGHT, NAV_BTN_AREA_HEIGHT);
        desired_h += NAV_BAR_BORDER_THICKNESS;
        int desired_w = text_area_w + NAV_BTN_AREA_WIDTH + NAV_BAR_BORDER_THICKNESS;
        this.desiredSize = new Size(desired_w,desired_h);
        this.currentSize = new Size(desiredSize);
        this.window = window;
    }

    public void update(float dt) {
        navButtons.update(dt);
    }

    public void render(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {
        drawBackground(batch,graphics,x0,y0,dragZone.iData());
        drawBorder(batch, graphics, x0, y0);
        drawText(batch, graphics, x0, y0);
        drawButtons(batch, graphics, x0, y0);
    }

    private void drawText(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {
        if (NAV_TXT_COLOR.a > 0) {
            x0 += NAV_BAR_PADDING;
            y0 -= (NAV_TXT_AREA_HEIGHT - NAV_BAR_PADDING);
            AsciiFont font = graphics.amigaFont();
            int char_width = NAV_TXT_CHAR_WIDTH;
            float color = NAV_TXT_COLOR.toFloatBits();
            byte[] title = window.title().getBytes(StandardCharsets.US_ASCII);
            for (int i = 0; i < title.length; i++) {
                float x = x0 + i * char_width;
                Glyph glyph = font.getChar(title[i]);
                TextureRegion region = glyph.region();
                batch.draw(region,x,y0,char_width, NAV_TXT_HEIGHT, color,dragZone.iData());
            }
        }
    }

    private void drawButtons(SpriteBatch batch, GUIGraphics graphics, int x0, int y0) {
        // Draw separation line between text and buttons;
        TextureRegion blank = graphics.blank();
        float color = WIN_BORDER_COLOR.toFloatBits();
        int id = dragZone.iData();
        y0 -= NAV_TXT_AREA_HEIGHT;
        x0 += textAreaWidth();
        batch.draw(blank,x0,y0,NAV_BAR_BORDER_THICKNESS,NAV_TXT_AREA_HEIGHT,color,id);
        // Buttons
        y0 += NAV_TXT_AREA_HEIGHT;
        x0 += NAV_BAR_BORDER_THICKNESS;
        navButtons.render(batch,graphics,x0,y0);
    }

    protected Area dragZone() {
        int x0 = window.x0() + WIN_BORDER_THICKNESS;
        int y1 = window.y0() - WIN_BORDER_THICKNESS;
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
        Disposable.dispose(navButtons,dragZone);
    }

    private int textAreaWidth() {
        int W = discreteWidth();
        return W - NAV_BTN_AREA_WIDTH - NAV_BAR_BORDER_THICKNESS;
    }
}
