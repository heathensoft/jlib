package io.github.heathensoft.jlib.hud.ui.window;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.hud.Hud;
import io.github.heathensoft.jlib.hud.HudGraphics;
import io.github.heathensoft.jlib.hud.ui.Box;
import io.github.heathensoft.jlib.hud.ui.HudItem;
import io.github.heathensoft.jlib.hud.ui.Size;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import org.joml.Vector2f;

/**
 * Old school desktop like window. pixel perfect, amiga 500 style font. etc.
 * Resize, drag, maximize, restore, close, open.
 * @author Frederik Dahl
 * 17/12/2022
 */


public class HudWindow extends HudItem {

    public static final int border_thickness = 3;

    protected final Box content;
    protected final Size currentSize;
    protected final Size desiredSize;
    protected final NavigationBar navigationBar;
    protected final WindowBorder.Top borderTop;
    protected final WindowBorder.Left borderLeft;
    protected final WindowBorder.Right borderRight;
    protected final WindowBorder.Bottom borderBottom;
    protected final Color borderColor = Color.WHITE.cpy();
    protected final Color backgroundColor = Color.EMPTY.cpy();
    protected final Color backgroundImageColor = Color.EMPTY.cpy();
    protected final Color navTextColor = Color.WHITE.cpy();
    protected final Color navBackgroundColor = Color.EMPTY.cpy();
    protected final Color navButtonColor = Color.WHITE.cpy();
    protected final Color navCloseButtonHoverColor = Color.RED.cpy();
    protected final Color navRestoreButtonHoverColor = Color.GREEN.cpy();

    public HudWindow(Hud context, Box content, String title) {
        super(context, title);
        this.content = content;
        this.navigationBar = new NavigationBar(this);
        this.borderBottom = new WindowBorder.Bottom(this);
        this.borderRight = new WindowBorder.Right(this);
        this.borderLeft = new WindowBorder.Left(this);
        this.borderTop = new WindowBorder.Top(this);
        float desiredW = Math.max(content.desiredWidth(),
        navigationBar.desiredSize().width()) + (2 * border_thickness);
        float desiredH = content.desiredHeight() +
        navigationBar.desiredSize().height() + (2 * border_thickness);
        this.desiredSize = new Size(desiredW,desiredH);
        this.currentSize = new Size(desiredSize);
        restore();
        center();
        focus();
        open();
    }

    public void render(SpriteBatch batch) {
        drawBackgroundImage(batch);
        drawBackground(batch);
        drawNavigation(batch);
        drawContents(batch);
        drawBorders(batch);
    }

    private void drawBackgroundImage(SpriteBatch batch) {
        /* todo: */
    }

    private void drawBackground(SpriteBatch batch) {
        if (backgroundColor.a > 0) {
            TextureRegion blank = context.GRAPHICS.blank();
            float color = backgroundColor.toFloatBits();
            int x = x0() + border_thickness;
            int y = y0() - height() + border_thickness;
            int w = width() - (2 * border_thickness);
            int h = height() - (2 * border_thickness);
            batch.draw(blank,x,y,w,h,color,0);
        }
    }

    private void drawNavigation(SpriteBatch batch) {
        int x = x0() + border_thickness;
        int y = y0() - border_thickness;
        navigationBar.render(batch,context.GRAPHICS,x,y);
    }

    private void drawContents(SpriteBatch batch) {
        int x = x0() + border_thickness;
        int y = y0() - border_thickness;
        y -= navigationBar.discreteHeight();
        content.render(batch,context.GRAPHICS,x,y);
    }

    private void drawBorders(SpriteBatch batch) {
        if (borderColor.a > 0) {
            TextureRegion vertical = context.GRAPHICS.coreAsset(HudGraphics.WINDOW_BORDER_VERTICAL);
            TextureRegion bottom = context.GRAPHICS.coreAsset(HudGraphics.WINDOW_BORDER_BOTTOM);
            TextureRegion top = context.GRAPHICS.coreAsset(HudGraphics.WINDOW_BORDER_TOP);
            float color = borderColor.toFloatBits();
            final int bottomId = borderBottom.iData();
            final int rightId = borderRight.iData();
            final int leftId = borderLeft.iData();
            final int topId = borderTop.iData();
            final int b1 = border_thickness;
            final int b2 = b1 + b1;
            final int b3 = b2 + b1;
            final int W = width();
            final int H = height();
            final int x0 = x0();
            final int y0 = y0();
            int x, y, w, h;
            x = x0 + b1; y = y0 - b1;
            w = W - b2; h = b2;
            batch.draw(top,x,y,w,h,color,topId);
            h = b3; y = y0 - H - b1;
            batch.draw(bottom,x,y,w,h,color,bottomId);
            x = x0 - b1; y = y0 - H + b1;
            w = b3; h = H - b2;
            batch.draw(vertical,x,y,w,h,color,leftId);
            x = x0 + W - b2;
            batch.draw(vertical,x,y,w,h,color,rightId);
        }
    }

    public void update(float dt) {
        navigationBar.update(dt);
        content.update(dt);
    }

    public void restore() {
        float desiredContentW = (desiredSize.width() - 2 * border_thickness);
        navigationBar.restore();
        content.restore();
        if (navigationBar.currentSize().width() < desiredContentW) {
            float resize = (desiredContentW - navigationBar.currentSize().width());
            navigationBar.resizeHorizontal(resize);
        } else if (content.currentWidth() < desiredContentW) {
            float resize = (desiredContentW - content.currentWidth());
            content.resizeHorizontal(resize);
        } currentSize.set(desiredSize);
    }

    public void maximize() {
        if (isMaximized()) return;
        final int W = context.WIDTH;
        final int H = context.HEIGHT;
        final int x0 = x0();
        final int y0 = y0();
        final int x1 = x0 + width();
        final int y1 = y0 - height();
        int transX = 0;
        int transY = 0;
        int resizeX = 0;
        int resizeY = 0;
        if (x0 != 0) {
            transX -= x0;
            resizeX += x0;
        }if (y0 != H) {
            transY += H - y0;
            resizeY += H - y0;
        }if (x1 != W) {
            resizeX += W - x1;
        }if (y1 != 0) {
            resizeY += y1;
        } resizeVertical(transY,resizeY);
        resizeHorizontal(transX,resizeX);
    }

    public void center() {
        if (isMaximized()) return;
        float x0 = (context.WIDTH - currentSize().width()) / 2f;
        float y0 = (context.HEIGHT - currentSize().height()) / 2f + currentSize().height();
        setPosition(new Vector2f(x0,y0));
    }

    protected void resizeHorizontal(float translation, float dx) {
        position.x += translation;
        if (dx < 0) {
            float wiggleRoom = (currentSize.width() - desiredSize.width());
            dx = -Math.min(-dx,wiggleRoom);
            navigationBar.resizeHorizontal(dx);
            content.resizeHorizontal(dx);
            currentSize.addWidth(dx);
        } else if (dx > 0) {
            navigationBar.resizeHorizontal(dx);
            content.resizeHorizontal(dx);
            currentSize.addWidth(dx);
        }
    }

    protected void resizeVertical(float translation, float dy) {
        position.y += translation;
        if (dy < 0) {
            float wiggleRoom = (currentSize.height() - desiredSize.height());
            dy = -Math.min(-dy,wiggleRoom);
            content.resizeVertical(dy);
            currentSize.addHeight(dy);
        } else if (dy > 0) {
            content.resizeVertical(dy);
            currentSize.addHeight(dy);
        }
    }

    protected void disposeContents() {
        Disposable.dispose(
                navigationBar,
                borderBottom,
                borderRight,
                borderLeft,
                borderTop,
                content
        );
    }

    public Area navDragZone() {
        return navigationBar.dragZone();
    }

    public Size desiredSize() {
        return desiredSize;
    }

    public Size currentSize() {
        return currentSize;
    }

    public int width() {
        return (int) currentSize.width();
    }

    public int height() {
        return (int) currentSize.height();
    }

    public boolean isMaximized() {
        return x0() == 0 && y0() == context.HEIGHT && width() == context.WIDTH && height() == context.HEIGHT;
    }

    public boolean isRestored() {
        return currentSize.equals(desiredSize);
    }

    public boolean navBarInsideBounds() {
        tmpArea.set(2,2,context.WIDTH - 2,context.HEIGHT - 2);
        return tmpArea.intersects(navDragZone());
    }

    public boolean navBarOutsideBounds() {
        return !navBarInsideBounds();
    }

    public void setBorderColor(Color color) {
        this.borderColor.set(color);
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor.set(color);
    }

    public void setBackgroundImageColor(Color color) {
        this.backgroundImageColor.set(color);
    }

    public void setNavTextColor(Color color) {
        this.navTextColor.set(color);
    }

    public void setNavBackgroundColor(Color color) {
        this.navBackgroundColor.set(color);
    }

    public void setNavButtonColor(Color color) {
        this.navButtonColor.set(color);
    }

    public void setNavCloseButtonHoverColor(Color color) {
        this.navCloseButtonHoverColor.set(color);
    }

    public void setNavRestoreButtonHoverColor(Color color) {
        this.navRestoreButtonHoverColor.set(color);
    }
}
