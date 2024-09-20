package io.github.heathensoft.jlib.test.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.lwjgl.window.TextProcessor;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.gfx.ColorPicker;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import io.github.heathensoft.jlib.ui.text.InputField;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.HexFormat;

/**
 * @author Frederik Dahl
 * 11/04/2024
 */


public class ColorPickerBox extends Box implements TextProcessor {

    private static final float border_width = 8;
    private static final float desired_content_width = 128;
    private static final float hsv_window_height = 128;
    private static final float slider_height = 16f;
    private static final float inner_spacing = 4f;
    private static final float input_field_font_size = 28;

    private final ColorPicker picker;
    private final InputField inputField;
    private final Instance iHsv_window;
    private final Instance iHue_slider;
    private final Instance iAlpha_slider;


    // Also. instead of locking boxes, make it a gradual lock. 0 -> 1

    public ColorPickerBox() throws Exception {
        picker = new ColorPicker();
        iHsv_window = new Instance();
        iHue_slider = new Instance();
        iAlpha_slider = new Instance();
        desired_width = border_width * 2 + desired_content_width;
        desired_height = border_width * 2 + hsv_window_height + 2 * (slider_height + inner_spacing);
        inputField = new InputField(this::onInputSubmit,this::validateHex,8);
    }

    public void editColor(Vector4f color) { picker.editColor(color); }

    protected void prepareBox(BoxWindow window, float dt) { picker.refresh(); }

    protected void closeBox() { closeInputField(); }

    protected void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef bounds =  bounds(U.popRect(),x,y);
        Rectanglef gadgets;

        renderer.drawGadgetBorders(bounds,border_width,Color.rgb_to_intBits(picker.getRgb()),parent_id);
        U.rectExpand(bounds,-border_width);
        float hsv_widow_height = bounds.lengthY() - 2 * (slider_height + inner_spacing);
        bounds.minY = bounds.maxY - hsv_widow_height;
        renderer.drawElement(picker.hsvWindowTexture(),bounds,iHsv_window.interactableID());



        if (iHsv_window.iPressed()) {
            if (iHsv_window.iJustPressed(Mouse.RIGHT)) {
                if (isActiveTextProcessor()) closeInputField();
                else openInputField();
            } else if (iHsv_window.iPressed(Mouse.LEFT)) {
                closeInputField();

                picker.pick(U.clamp(GUI.mouseToLocalNormalized(bounds,U.popVec2())));
                U.pushVec2();
                if (iHsv_window.iJustPressed()) {
                    GUI.windows.focusRequest(window);
                } displayFadingHex(window, x, y);
            }
        } else if (GUI.state.mouseIsPressed() || GUI.state.worldPressed()) {
            closeInputField();
        }

        {
            int color = Color.hsv_to_intBits(Color.invertHSV(U.popSetVec4(picker.getHsv())));
            U.pushVec4();
            float px = bounds.minX + picker.pickerPositionX() * bounds.lengthX();
            float py = bounds.minY + picker.pickerPositionY() * bounds.lengthY();
            gadgets = U.popSetRect(px - 3,py - 3,px + 3, py + 3);
            renderer.drawRound(gadgets,color,iHsv_window.interactableID());
            U.pushRect();
        }

        if (isActiveTextProcessor()) {
            Rectanglef field_bounds = U.popSetRect(bounds);
            float height = Math.min(hsv_widow_height,input_field_font_size);
            bounds.minY = bounds.maxY - height;
            renderer.drawElement(field_bounds,0x99000000,iHsv_window.interactableID());
            U.pushRect();
        }

        bounds.translate(0,-(hsv_widow_height + inner_spacing));
        bounds.minY = bounds.maxY - slider_height;
        renderer.drawElement(picker.husSliderTexture(),bounds,iHue_slider.interactableID());
        if (iHue_slider.iPressed(Mouse.LEFT)) {
            picker.selectHue(U.clamp(GUI.mouseToLocalNormalized(bounds,U.popVec2()).x));
            U.pushVec2();
            if (iHue_slider.iJustPressed()) {
                GUI.windows.focusRequest(window);
            } displayFadingHex(window, x, y);
        }

        {
            float px = bounds.minX + picker.hueSliderPosition() * bounds.lengthX();
            gadgets = U.popSetRect(px - 2, bounds.minY,px + 2, bounds.maxY);
            renderer.drawElement(gadgets,0xFF000000,iHue_slider.interactableID());
            U.pushRect();
        }

        bounds.translate(0,-(slider_height + inner_spacing));
        Vector4f uv = picker.getAlphaBackgroundUV(bounds,U.popVec4());
        renderer.drawElement(picker.alphaSliderBackground(),uv,bounds,iAlpha_slider.interactableID());
        renderer.drawElement(picker.alphaSliderTexture(),bounds, Color.rgb_to_intBits(picker.getRgb()),iAlpha_slider.interactableID());
        if (iAlpha_slider.iPressed(Mouse.LEFT)) {
            picker.selectAlpha(U.clamp(GUI.mouseToLocalNormalized(bounds,U.popVec2()).x));
            U.pushVec2();
            if (iAlpha_slider.iJustPressed()) {
                GUI.windows.focusRequest(window);
            } displayFadingHex(window, x, y);
        }
        U.pushVec4();
        U.pushRect();
    }


    protected void renderBoxText(BoxWindow window, RendererGUI renderer, float x, float y, float dt) {
        if (isActiveTextProcessor()) {
            Rectanglef bounds = U.rectExpand(bounds(U.popRect(),x,y),-border_width);
            float hsv_widow_height = bounds.lengthY() - 2 * (slider_height + inner_spacing);
            float height = Math.min(hsv_widow_height,input_field_font_size);
            bounds.minY = bounds.maxY - height;
            int color = inputField.fieldIsValid() ? 0xFFFFFFFF : 0xAAEEEEEE;
            renderer.fonts().bindFontMetrics(1);
            inputField.draw(renderer,bounds,color,0,true);
            U.pushRect();
        }
    }

    private void displayFadingHex(BoxWindow window, float x, float y) {
        Rectanglef bounds = U.rectExpand(bounds(U.popRect(),x,y),-border_width);
        bounds.minY = bounds.maxY - 30;
        String hex = Color.rgb_to_hex(picker.getRgb());
        window.displayFading(hex,bounds);
        U.pushRect();
    }

    private void openInputField() {
        if (!isActiveTextProcessor()) {
            inputField.set(Color.rgb_to_hex(picker.getRgb()));
            activateTextProcessor();
            iHsv_window.iFocus();
        }
    }

    private void closeInputField() {
        if (isActiveTextProcessor()) {
            deactivateTextProcessor();
            iHsv_window.iYieldFocus();
        }
    }

    public void keyPress(int key, int mods, boolean repeat) { inputField.keyPress(key, mods); }

    public void charPress(byte character) { inputField.charPress(character); }

    private void onInputSubmit(String hex) {
        if (inputField.fieldIsValid()) {
            picker.setRGB(Color.hex_to_rgb(hex,U.popVec4()));
            U.pushVec4();
            closeInputField();
        }
    }

    private boolean validateHex(String string) {
        return is_hexadecimal(string) && (string.length() == 8);
    }

    private boolean is_hexadecimal(String s) {
        if (s == null) return false;
        try { int h = HexFormat.fromHexDigits(s);
        } catch (IllegalArgumentException i) {
            return false;
        } return true;
    }

    public void dispose() {
        Disposable.dispose(picker,iAlpha_slider,
                iHue_slider,iHsv_window); }
}
