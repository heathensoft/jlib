package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.*;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.box.BoxContainer;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.ImageDisplay;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.Paragraph;
import io.github.heathensoft.jlib.ui.text.Text;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 08/03/2024
 */


public class TextFieldOld extends Box implements TextProcessor {

    private Text text;
    private int text_font;
    private float text_size;
    private boolean text_wrap;
    private boolean editing_enabled;
    private boolean sb_lock;
    private float sb_grab_y;
    private final float sb_width;
    private final float tf_padding;
    private float tf_text_offset;
    private float tf_text_height;
    private final Vector2f text_cursor_position;
    private final Interactable.Instance scroll_bar;
    private ImageDisplay boxBackground;
    private BoxWindow parent_window;

    public void setBoxBackground(ImageDisplay boxBackground) {
        this.boxBackground = boxBackground;
    }

    public TextFieldOld(float tf_width, float tf_height, float tf_padding, float sb_width, float text_size) {
        tf_width = Math.max(16,tf_width);
        tf_height = Math.max(16,tf_height);
        this.sb_width = Math.max(4,sb_width);
        this.tf_padding = Math.max(0,Math.min(tf_padding,Math.min(tf_width,tf_height)/3f));
        this.desired_width = tf_width + this.sb_width;
        this.desired_height = tf_height;
        this.text_cursor_position = new Vector2f();
        this.iID = iObtainID();
        this.scroll_bar = new Interactable.Instance();
        this.text = new Text();
        this.editing_enabled = false;
        this.text_wrap = false;
        this.text_size = text_size;
        this.text_font = 0;
    }


    protected void onWindowInit(BoxWindow window, BoxContainer parent) {
        this.parent_window = window;
    }

    protected void onWindowClose(BoxWindow window) {
        if (isActiveTextProcessor()) deactivateTextProcessor();
        scroll_bar.iYieldFocus();
        iYieldFocus();
    }

    protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {

        if (iHovered()) {
            if (editing_enabled) {
                GUI.state.useCursorIcon(CursorObjects.CURSOR_TEXT_INPUT);
            } else GUI.state.useCursorIcon(CursorObjects.CURSOR_ARROW);
            if (iJustPressed(Mouse.LEFT)) {
                if (editing_enabled && text.isBlank()) {
                    text.add(new Paragraph());
                } window.setAutoFocus(this);
                iFocus();
            } else if (GUI.mouse.scrolled()) {
                float scroll_amount = GUI.mouse.get_scroll();
                if (scroll_amount > 0.0f) {
                    scrollUp(text_size * scroll_amount);
                    scroll_bar.iFocus();
                } else if (scroll_amount < 0.0f) {
                    scrollDown(text_size * -scroll_amount);
                    scroll_bar.iFocus();
                }
            }
        }

        if (iHasFocus()) {
            if (!isActiveTextProcessor()) {
                activateTextProcessor();
            }
        } else {
            if (isActiveTextProcessor()) {
                deactivateTextProcessor();
            }
        }

        if (scroll_bar.iGrabbed(Mouse.LEFT)) {
            float window_height = currentHeight() - (2 * tf_padding);
            float tf_wiggle_room = tf_text_height - window_height;
            if (tf_wiggle_room > 0) { scroll_bar.iFocus();
                float normalized_height = window_height / tf_text_height;
                float sb_outer_height = currentHeight();
                float sb_inner_height = sb_outer_height * normalized_height;
                float sb_wiggle_room = sb_outer_height - sb_inner_height;
                float drag_vector_y = GUI.mouse_drag_vector(U.vec2(),Mouse.LEFT).y;
                float sb__position = sb_grab_y - drag_vector_y;
                sb__position = clamp(sb__position,0,sb_wiggle_room);
                float normalized_position = sb__position / sb_wiggle_room;
                tf_text_offset = tf_wiggle_room * normalized_position;
            }

        } else if (scroll_bar.iHovered()) {
            if (scroll_bar.iJustPressed(Mouse.LEFT)) {
                float window_height = currentHeight() - (2 * tf_padding);
                float text_height = Math.max(tf_text_height,window_height);
                float window_wiggle_room = text_height - window_height;
                if (window_wiggle_room > 0) {
                    float normalized_height = window_height / tf_text_height;
                    float scroll_bar_height = currentHeight() * normalized_height;
                    float scroll_bar_wiggle_room = currentHeight() - scroll_bar_height;
                    sb_grab_y = scroll_bar_wiggle_room * windowPositionNormalized();
                    scroll_bar.iFocus();
                }
            } else if (GUI.mouse.scrolled()) {
                float scroll_amount = GUI.mouse.get_scroll();
                if (scroll_amount > 0.0f) { scroll_bar.iFocus();
                    scrollUp(text_size * scroll_amount);
                } else if (scroll_amount < 0.0f) { scroll_bar.iFocus();
                    scrollDown(text_size * -scroll_amount);
                }
            }
        }

        refresh();

        if (editing_enabled && iHasFocus() && !text.isBlank() && textFieldWiggleRoom() > 0) {
            float cursor_y0 = -text_cursor_position.y;
            float cursor_y1 = cursor_y0 + text_size;
            float window_height = currentHeight() - (2 * tf_padding);
            float window_y0 = tf_text_offset;
            float window_y1 = window_y0 + window_height;
            if(cursor_y0 < window_y0) scrollUp(window_y0 - cursor_y0);
            else if(cursor_y1 > window_y1) scrollDown(cursor_y1 - window_y1);
        }
        Rectanglef bounds = bounds(U.rectf(),x,y);
        if (boxBackground == null) renderer.drawElement(bounds,0xFF212121, iID);
        else boxBackground.render(window,renderer,bounds,0,0,dt);
        bounds.minX = bounds.maxX - sb_width;
        renderScrollBar(renderer,bounds);

    }

    protected void displayFadingFontSize() {
        Rectanglef bounds = U.rectf();
        if (parent_window.getBoundsOf(this,bounds)) {
            bounds.maxX -= sb_width;
            parent_window.displayFading(null,null,round(text_size),null,bounds);
        }
    }





    protected void renderText(BoxWindow window, RendererGUI renderer, float x, float y, float dt) {
        if (!text.isBlank()) {
            boolean show_cursor = editing_enabled && iHasFocus();
            Rectanglef bounds = bounds(U.rectf(),x,y);
            bounds.maxX -= sb_width;
            renderer.drawText(text,bounds,tf_text_offset,text_font,tf_padding,text_size,text_wrap,show_cursor);
        }

    }


    private void renderScrollBar(RendererGUI renderer, Rectanglef bounds) {
        int color, id;
        float text_field_wiggle_room = textFieldWiggleRoom();
        if (!text.isBlank() && (text_field_wiggle_room > 0)) {
            float sb_max_height = (bounds.lengthY() - sb_width);
            float window_height = currentHeight() - (2 * tf_padding);
            float normalized_height = window_height / tf_text_height;
            float sb_height = sb_max_height * normalized_height;
            float sb_wr = sb_max_height - sb_height;
            float sb_position = sb_wr * windowPositionNormalized(text_field_wiggle_room);
            bounds = U.rectf().set(bounds);
            bounds.maxY -= sb_position;
            bounds.minY = bounds.maxY - (sb_height + sb_width);
            color = 0xdd0000FF;
            id = scroll_bar.interactableID();
        } else { id = iID;
            color = 0x66666666;
        } renderer.drawElement(bounds,color,id,0.0f);
    }

    public void keyPress(int key, int mods) {
        if (editing_enabled) {
            if ((mods & GLFW_MOD_CONTROL) > 0) {
                if (key == GLFW_KEY_KP_ADD) {
                    if (text_size < 256) text_size++;
                    displayFadingFontSize();
                } else if (key == GLFW_KEY_KP_SUBTRACT) {
                    if (text_size > 8) text_size--;
                    displayFadingFontSize();
                } else if (key == GLFW_KEY_C) {
                    String string = text.toString();
                    Window window = Engine.get().window();
                    window.setClipboard(string);
                } else if (key == GLFW_KEY_V) {
                    Window window = Engine.get().window();
                    String string = window.getClipboard();
                    text.set(string);
                } else if (key == GLFW_KEY_X) {
                    String string = text.toString();
                    Window window = Engine.get().window();
                    window.setClipboard(string);
                    text.clear();
                } else if (key == GLFW_KEY_F) {
                    text_font = (text_font + 1) % FontsGUI.FONT_SLOTS;
                    displayFadingFont();
                }else text.keyPress(key, mods);
            } else text.keyPress(key, mods);
        } else { // editing disabled
            if ((mods & GLFW_MOD_CONTROL) > 0) {
                if (key == GLFW_KEY_KP_ADD) {
                    if (text_size < 256) text_size++;
                    displayFadingFontSize();
                } else if (key == GLFW_KEY_KP_SUBTRACT) {
                    if (text_size > 8) text_size--;
                    displayFadingFontSize();
                } else if (key == GLFW_KEY_C) {
                    String string = text.toString();
                    Window window = Engine.get().window();
                    window.setClipboard(string);
                } else if (key == GLFW_KEY_F) {
                    text_font = (text_font + 1) % FontsGUI.FONT_SLOTS;
                    displayFadingFont();
                }// No key-mods
            } else if (key == GLFW_KEY_UP) {
                scrollUp(text_size);
            } else if (key == GLFW_KEY_DOWN) {
                scrollDown(text_size);
            }
        }
    }

    private void displayFadingFont() {
        Rectanglef bounds = U.rectf();
        if (parent_window.getBoundsOf(this,bounds)) {
            bounds.maxX -= sb_width;
            GUI.fonts.bindFontMetrics(text_font);
            String font_name = GUI.fonts.name();
            parent_window.displayFading(font_name,null,text_font,null,bounds);
        }
    }

    public void keyRelease(int key, int mods) {  }

    public void charPress(byte character) { text.charPress(character); }

    public void lockScrollBar(boolean lock) { sb_lock = lock; }

    public boolean isWrappingEnabled() { return text_wrap; }

    public void enableWrapping(boolean on) { this.text_wrap = on; }

    public void enableEditing(boolean on) { this.editing_enabled = on; }

    public boolean isEditingEnabled() { return editing_enabled; }

    public float textFieldPadding() { return tf_padding; }

    public float scrollbarWidth() { return sb_width; }

    public void setFont(int font) { this.text_font = font; }

    public int font() { return text_font; }

    public void setTextSize(float size) { this.text_size = size; }

    public float textSize() { return text_size; }

    public Text text() { return text; }

    public Text setText(Text text) {
        if (text == null) text = new Text();
        Text previous = this.text;
        this.text = text;
        return previous;
    }

    protected void refresh() {
        Rectanglef bounds = bounds(U.rectf(),0,0);
        bounds.minY += tf_padding;
        bounds.maxY -= tf_padding;
        float tf_window_height = bounds.lengthY();

        if (text.isBlank()) {
            tf_text_height = tf_window_height;
            text_cursor_position.zero();
            tf_text_offset = 0f;
        } else {
            bounds.minX += tf_padding;
            bounds.maxX -= (tf_padding + sb_width);
            float tf_window_width = bounds.lengthX();
            GUI.fonts.bindFontMetrics(text_font);
            tf_text_height = text.height(tf_window_width,text_size,text_wrap);
            tf_text_height = Math.max(tf_window_height,tf_text_height);
            if (editing_enabled && iHasFocus()) text.cursorPosition(text_cursor_position,tf_window_width,text_size,text_wrap);
            else text_cursor_position.zero();
            float tf_wiggle_room = tf_text_height - tf_window_height;
            tf_text_offset = clamp(tf_text_offset,0,tf_wiggle_room);
            boolean focus = iHasFocus() || scroll_bar.iHasFocus();
            if (sb_lock &! focus) {
                if (text.isOrderedDefault()) {
                    tf_text_offset = tf_wiggle_room;
                } else {
                    tf_text_offset = 0;
                }
            }


        }
    }

    private float textFieldWiggleRoom() {
        float tf_window_height = (currentHeight() - (2 * tf_padding));
        return Math.max(0,tf_text_height - tf_window_height);
    }

    private float windowPositionNormalized(float wiggle_room) {
        return wiggle_room <= 0 ? 0 : tf_text_offset / wiggle_room;
    }

    private float windowPositionNormalized() {
        return windowPositionNormalized(textFieldWiggleRoom());
    }

    private void scrollDown(float dy) {
        if (!text.isBlank() && dy > 0) {
            float wiggle_room = textFieldWiggleRoom();
            if (wiggle_room > 0) {
                float window_height = tf_text_height - wiggle_room;
                wiggle_room = tf_text_height - tf_text_offset - window_height;
                dy = Math.min(wiggle_room,dy);
                tf_text_offset += dy;
            }
        }
    }

    private void scrollUp(float dy) {
        if (!text.isBlank() && dy > 0) {
            if (tf_text_offset > 0) {
                tf_text_offset = Math.max(0,tf_text_offset-dy);
            }
        }
    }


    public void dispose() {
        super.dispose();
        scroll_bar.dispose();
        Disposable.dispose(boxBackground);
    }


    private void renderCursorDebug(RendererGUI renderer, float x, float y) {
        x += tf_padding;
        y -= tf_padding;
        y += tf_text_offset;
        renderer.fonts().bindFontMetrics(text_font);
        float scale = renderer.fonts().relativeScale(text_size);
        float cursor_width = renderer.fonts().averageAdvance() * scale;
        float cursor_height = text_size;
        Rectanglef rect = U.rectf();
        rect.minX = x + text_cursor_position.x;
        rect.maxX = rect.minX + cursor_width;
        rect.maxY = y + text_cursor_position.y;
        rect.minY = rect.maxY - cursor_height;
        rect.minX -= 2;
        rect.minY -= 2;
        rect.maxX += 2;
        rect.maxY += 2;
        renderer.drawElement(rect,0xFF0000FF);
    }

    private void renderTextFieldBackgroundDebug(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef bounds = bounds(U.rectf(),x,y);
        bounds.minX += tf_padding;
        bounds.maxX -= (tf_padding + sb_width);
        bounds.maxY -= tf_padding;
        bounds.maxY += tf_text_offset;
        bounds.minY = bounds.maxY - tf_text_height;
        renderer.drawElement(bounds,0x77000000, iID);
    }


}
