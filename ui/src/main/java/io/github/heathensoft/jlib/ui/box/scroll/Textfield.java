package io.github.heathensoft.jlib.ui.box.scroll;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.*;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.Interactable;
import io.github.heathensoft.jlib.ui.box.BoxContainer;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.gfx.FontsGUI;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.Paragraph;
import io.github.heathensoft.jlib.ui.text.Text;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static io.github.heathensoft.jlib.common.utils.U.round;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;

/**
 * @author Frederik Dahl
 * 18/04/2024
 */


public class Textfield extends ScrollBox implements TextProcessor {

    private Text text;
    private BoxWindow context;
    private Vector4f custom_color = GUI.variables.textfield_default_custom_color;
    private float padding = GUI.variables.textfield_default_padding;
    private float font_size = GUI.variables.gui_default_font_size;
    private int font = GUI.variables.gui_default_font;
    private float content_height;
    private boolean lock_scrollbar;
    private boolean editing_enabled;
    private boolean word_wrap_enabled;
    private boolean use_custom_color;


    public Textfield(float content_width, float content_height) { this(new Text(),content_width,content_height); }
    public Textfield(Text text, float content_width, float content_height) {
        this.desired_width = content_width + boxPaddingHorizontal();
        this.desired_height = content_height + boxPaddingVertical();
        this.iID = iObtainIDAndRegisterAsConsumer();
        this.text = text;
    }


    public boolean iAcceptItemDrop(Interactable drop) {
        if (editing_enabled) {
            if (drop instanceof ItemContainer.Item<?> item) {
                text.add(new Paragraph(item.name));
                return true;
            }
        } return false;
    }


    protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        if (iHovered()) {
            if (editing_enabled) {
                GUI.state.useCursorIcon(CursorObjects.CURSOR_TEXT_INPUT);
            } else GUI.state.useCursorIcon(CursorObjects.CURSOR_ARROW);
            if (iJustPressed(Mouse.LEFT)) { iFocus();
                GUI.windows.focusRequest(window);
                if (editing_enabled) {
                    window.setAutoFocus(this);
                    if (text.isBlank()) text.add(new Paragraph());
                }
            } else if (GUI.mouse.scrolled()) {
                float scroll_amount = GUI.mouse.get_scroll();
                windowScroll(scroll_amount * font_size,content_height);
                scrollbar.iFocus();
            }
        }
        if (iHasFocus()) {
            if (!isActiveTextProcessor())
                activateTextProcessor();
        } else if (isActiveTextProcessor()) {
            deactivateTextProcessor();
        }

        float box_height_padded = boxHeightPadded();
        if (text.isBlank()) {
            content_height = box_height_padded;
            content_offset = 0;
        } else {
            if (processScrollBarEvents(font_size,content_height)) {
                GUI.windows.focusRequest(window);
                scrollbar.iFocus();
            }
            float content_width = boxWidthPadded();
            GUI.fonts.bindFontMetrics(font);
            content_height = text.height(content_width, font_size,word_wrap_enabled);
            content_height = Math.max(box_height_padded, content_height);

            float cursor_position;
            if (editing_enabled && iHasFocus()) {
                Vector2f cursor = U.popVec2();
                text.cursorPosition(cursor,content_width, font_size,word_wrap_enabled);
                cursor_position = cursor.y;
                U.pushVec2();
            } else cursor_position = 0;

            float wiggle_room = wiggleRoomScroll(content_height,box_height_padded);
            boolean focus = iHasFocus() || scrollbar.iHasFocus();
            if (lock_scrollbar &! focus) {
                if (text.isOrderedDefault()) {
                    content_offset = wiggle_room;
                } else content_offset = 0;
            }
            content_offset = U.clamp(content_offset,0,wiggle_room);
            if (editing_enabled && iHasFocus() && wiggle_room > 0) {
                float cursor_y0 = -cursor_position;
                float cursor_y1 = cursor_y0 + font_size;
                float window_y0 = content_offset;
                float window_y1 = window_y0 + box_height_padded;
                if(cursor_y0 < window_y0) windowScroll(window_y0 - cursor_y0,content_height);
                else if(cursor_y1 > window_y1) windowScroll(window_y1 - cursor_y1,content_height);
            }
        }
        Rectanglef bounds = bounds(U.popRect(),x,y);
        renderBackground(renderer,bounds,iID,dt);
        renderScrollbar(renderer,content_height,x,y,dt);
        U.pushRect();
    }

    protected void renderBoxText(BoxWindow window, RendererGUI renderer, float x, float y, float dt) {
        if (!text.isBlank()) {
            boolean show_cursor = editing_enabled && iHasFocus();
            Rectanglef bounds = bounds(U.popRect(),x,y);
            bounds.minX += boxPaddingLeft();
            bounds.minY += boxPaddingBottom();
            bounds.maxX -= boxPaddingRight();
            bounds.maxY -= boxPaddingTop();
            if (use_custom_color) renderer.drawText(text,bounds,custom_color,content_offset, font,0, font_size,word_wrap_enabled,show_cursor);
            else renderer.drawText(text,bounds,content_offset, font,0, font_size,0.0f,word_wrap_enabled,show_cursor);
            U.pushRect();
        }
    }



    public void keyPress(int key, int mods) {
        if (editing_enabled) {
            if ((mods & GLFW_MOD_CONTROL) > 0) {
                if (key == GLFW_KEY_KP_ADD) {
                    if (font_size < 256) font_size++;
                    displayFadingFontSize();
                } else if (key == GLFW_KEY_KP_SUBTRACT) {
                    if (font_size > 8) font_size--;
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
                    font = (font + 1) % FontsGUI.FONT_SLOTS;
                    displayFadingFont();
                }else text.keyPress(key, mods);
            } else text.keyPress(key, mods);
        } else { // editing disabled
            if ((mods & GLFW_MOD_CONTROL) > 0) {
                if (key == GLFW_KEY_KP_ADD) {
                    if (font_size < 256) font_size++;
                    displayFadingFontSize();
                } else if (key == GLFW_KEY_KP_SUBTRACT) {
                    if (font_size > 8) font_size--;
                    displayFadingFontSize();
                } else if (key == GLFW_KEY_C) {
                    String string = text.toString();
                    Window window = Engine.get().window();
                    window.setClipboard(string);
                } else if (key == GLFW_KEY_F) {
                    font = (font + 1) % FontsGUI.FONT_SLOTS;
                    displayFadingFont();
                }// No key-mods
            } else if (key == GLFW_KEY_UP) {
                windowScroll(font_size,content_height);
            } else if (key == GLFW_KEY_DOWN) {
                windowScroll(-font_size,content_height);
            }
        }
    }

    public void charPress(byte character) { if (editing_enabled) text.charPress(character); }
    public void setPadding(float padding) { this.padding = padding; }
    public void lockScrollBar(boolean lock) { lock_scrollbar = lock; }
    public boolean isWordWrapEnabled() { return word_wrap_enabled; }
    public void enableWordWrap(boolean on) { this.word_wrap_enabled = on; }
    public void enableEditing(boolean on) { this.editing_enabled = on; }
    public boolean isEditingEnabled() { return editing_enabled; }
    public void setFont(int font) { this.font = font; }
    public int font() { return font; }
    public void setFontSize(float size) { this.font_size = size; }
    public float fontSize() { return font_size; }
    public float contentHeight() { return content_height; }
    public Vector4f scrollbarColor() { return scrollbar_color; }
    public void setScrollbarColor(Vector4f color) { this.scrollbar_color = color; }
    public void useCustomColor(boolean on) { use_custom_color = on; }
    public Vector4f customColor() { return custom_color; }
    public void setCustomColor(Vector4f color) { this.custom_color = color; }
    public Text text() { return text; }
    public Text setText(Text text) {
        if (text == null) text = new Text();
        Text previous = this.text;
        this.text = text;
        return previous;
    }

    protected float boxPaddingTop() { return padding; }
    protected float boxPaddingRight() { return scrollbar_width; }
    protected float boxPaddingBottom() { return padding; }
    protected float boxPaddingLeft() { return padding; }
    protected void initializeBox(BoxWindow window, BoxContainer parent) { context = window; }
    protected void closeBox() { super.closeBox();
        if (isActiveTextProcessor()) deactivateTextProcessor();
    }

    protected void displayFadingFont() {
        Rectanglef bounds = U.popRect();
        if (context.getBoundsOf(this,bounds)) {
            bounds.maxX -= scrollbar_width;
            GUI.fonts.bindFontMetrics(font);
            String font_name = GUI.fonts.name();
            context.displayFading(font_name,null, font,null,bounds);
        } U.pushRect();
    }

    protected void displayFadingFontSize() {
        Rectanglef bounds = U.popRect();
        if (context.getBoundsOf(this,bounds)) {
            bounds.maxX -= scrollbar_width;
            context.displayFading(null,null,round(font_size),null,bounds);
        } U.pushRect();
    }
}
