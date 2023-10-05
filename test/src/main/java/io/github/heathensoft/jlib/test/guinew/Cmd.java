package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.gui.textnew.ColoredParagraph;
import io.github.heathensoft.jlib.gui.textnew.TextField;
import io.github.heathensoft.jlib.gui.textnew.TextUtils;
import io.github.heathensoft.jlib.lwjgl.gfx.Color32;

/**
 * @author Frederik Dahl
 * 25/09/2023
 */


public class Cmd extends TextField {

    private Color32 color = new Color32("cc7832");

    public Cmd() {
        super();
    }

    public Cmd(int capacity) {
        super(capacity);
    }

    public Cmd(String string) {
        super(string);
    }

    @Override
    protected void onKeyEnter(String value) {

    }

    @Override
    protected void onFieldEdit(String value) {

    }

    @Override
    protected boolean validateField(String value) {
        return TextUtils.isHexadecimal(value);
    }

    @Override
    protected boolean showCursor() {
        return true;
    }


    public Color32 color() {
        return color;
    }


}
