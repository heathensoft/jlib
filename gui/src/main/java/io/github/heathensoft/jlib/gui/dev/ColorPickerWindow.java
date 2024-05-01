package io.github.heathensoft.jlib.gui.dev;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.text.CommandLine;
import io.github.heathensoft.jlib.gui.text.TextUtils;
import io.github.heathensoft.jlib.gui.window.*;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 10/11/2023
 */


public class ColorPickerWindow extends WindowGUI {

    private final ColorPicker picker;
    private final HexInputField inputField;

    public ColorPickerWindow() throws Exception {
        super("ColorPicker",Anchor.NONE);
        this.picker = new ColorPicker();
        this.inputField = new HexInputField(picker);
        this.inputField.set(Color.rgb_to_hex(picker.getRgb()));
        create(new PickerRootContainer(picker,inputField));
    }

    private HexInputField inputField() { return inputField; }

    private ColorPicker picker() { return picker; }

    public void editColorVector(Vector4f rgb) {
        picker.editColor(rgb);
        inputField.set(Color.rgb_to_hex(picker.getRgb()));
    }

    public Vector4f getColor(Vector4f dst) {
        return dst.set(picker.getRgb());
    }

    public void prepare(float dt) { picker.refreshHsvWindowTexture(); }

    public void dispose() {
        super.dispose();
        Disposable.dispose(picker);
    }

    private static final class PickerRootContainer extends RootContainer {

        private final ColorPicker colorPicker;

        PickerRootContainer(ColorPicker colorPicker, HexInputField inputField)  {
            super(512,512, 5);
            this.colorPicker = colorPicker;
            VBoxContainer vBoxContainer = new VBoxContainer(3);
            EmptyBox emptyBox = new EmptyBox(256,27);
            emptyBox.lockVertical();
            HueSlider hueSlider = new HueSlider(256,32);
            hueSlider.lockVertical();
            HsvWindow hsvWindow = new HsvWindow(256,128);
            InputBox inputBox = new InputBox(inputField,128,32,2);
            inputBox.lockVertical();
            vBoxContainer.addBox(emptyBox);
            vBoxContainer.addBox(hsvWindow);
            vBoxContainer.addBox(hueSlider);
            vBoxContainer.addBox(inputBox);
            this.add(vBoxContainer);
        }

        public void renderBackground(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef quad = bounds(U.popRect(),x,y);
            int abgr = Color.rgb_to_intBits(colorPicker.getRgb());
            renderer.drawElement(quad,abgr,id,0,true);
            U.pushRect();
        }
    }


    private static final class HueSlider extends Box {
        HueSlider(int width, int height) {
            this.restingSize.set(width,height);
            this.currentSize.set(restingSize());
            this.id = iObtainID();
        }
        public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            ColorPickerWindow window = context.cast(ColorPickerWindow.class);
            ColorPicker colorPicker = window.picker();
            float w = currentSize.width();
            float h = currentSize.height();
            if (iPressed(Mouse.LEFT)) {
                context.focus();
                HexInputField inputField = window.inputField();
                Vector2f mouse = context.mouse_position(U.popVec2());
                float mouse_x = U.clamp(mouse.x,x,x+w);
                U.pushVec2();
                float picker_position = U.remap(mouse_x,x,x+w,0,1);
                colorPicker.selectHue(picker_position);
                inputField.set(Color.rgb_to_hex(colorPicker.getRgb()));
            }
            Texture slider_texture = colorPicker.hue_slider_texture();
            Rectanglef quad = U.popSetRect(x,y-h,x+w,y);
            Vector4f region = U.popSetVec4(0,0,1,1);
            renderer.drawElement(slider_texture,region,quad,id);
            U.pushVec4();

            float slider_position = U.remap(colorPicker.sliderPosition(),0,1,x,x+w);
            float slider_width = 2f;
            float slider_x = slider_position - slider_width / 2f;
            quad.minX = slider_x; quad.maxX = slider_x + slider_width;
            renderer.drawElement(quad,0xFF000000,id);
            U.pushRect();
        }
    }

    private static final class HsvWindow extends Box {
        HsvWindow(int width, int height) {
            this.restingSize.set(width,height);
            this.currentSize.set(restingSize());
            this.id = iObtainID();
        }
        public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            ColorPickerWindow window = context.cast(ColorPickerWindow.class);
            ColorPicker colorPicker = window.picker();
            float w = currentSize.width();
            float h = currentSize.height();
            if (iPressed(Mouse.LEFT)) {
                context.focus();
                HexInputField inputField = window.inputField();
                Vector2f mouse = context.mouse_position(U.popVec2());
                float mouse_x = U.clamp(mouse.x,x,x+w);
                float mouse_y = U.clamp(mouse.y,y-h,y);
                float picker_x = U.remap(mouse_x,x,x+w,0,1);
                float picker_y = U.remap(mouse_y,y-h,y,0,1);
                colorPicker.pick(mouse.set(picker_x,picker_y));
                U.pushVec2();
                inputField.set(Color.rgb_to_hex(colorPicker.getRgb()));
            }
            Texture hsv_window_texture = colorPicker.hsv_window_texture();
            Rectanglef quad = U.popSetRect(x,y-h,x+w,y);
            Vector4f region = U.popSetVec4(0,0,1,1);
            renderer.drawElement(hsv_window_texture,region,quad,id);
            U.pushVec4();

            float picker_position_x = U.remap(colorPicker.pickerPositionX(),0,1,x,x+w);
            float picker_position_y = U.remap(colorPicker.pickerPositionY(),0,1,y-h,y);
            quad.minX = picker_position_x - 2; quad.maxX = picker_position_x + 2;
            quad.minY = picker_position_y - 2; quad.maxY = picker_position_y + 2;
            renderer.drawElement(quad,0xFF000000,id);
            U.pushRect();
        }
    }

    private static final class HexInputField extends CommandLine {
        private final ColorPicker colorPicker;
        HexInputField(ColorPicker colorPicker) {
            super(120);
            this.colorPicker = colorPicker;
        }
        protected boolean onFieldEdit(String value) {
            return TextUtils.is_hexadecimal(value) && (value.length() == 6 || value.length() == 8);
        }
        protected void onKeyEnter(String value, boolean isValid) {
            if (isValid) {
                colorPicker.setRGB(Color.hex_to_rgb(value,U.popVec4()));
                U.pushVec4();
            }
        }

        protected void onKeyDown() {
            float value = colorPicker.pickerPositionY();
            float saturation = colorPicker.pickerPositionX();
            if (value > 0) { value = Math.max(0,value - 0.05f);
                colorPicker.pick(U.popSetVec2(saturation,value));
                set(Color.rgb_to_hex(colorPicker.getRgb()));
                U.pushVec2();
            }
        }

        protected void onKeyUp() {
            float value = colorPicker.pickerPositionY();
            float saturation = colorPicker.pickerPositionX();
            if (value < 1) { value = Math.min(1,value + 0.05f);
                colorPicker.pick(U.popSetVec2(saturation,value));
                set(Color.rgb_to_hex(colorPicker.getRgb()));
                U.pushVec2();
            }
        }
    }
}
