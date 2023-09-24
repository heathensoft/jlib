package io.github.heathensoft.jlib.gui.textnew;

import io.github.heathensoft.jlib.gui.Size;

/**
 * @author Frederik Dahl
 * 22/09/2023
 */


public abstract class TextField<T extends Text<?>> {

    // Todo: Do boxes first, then make this inherit

    private T text;
    private FontData fontData;
    private Size visibleArea;
    private float windowPosition;
    private float textAreaPadding;
    private float desired_height;
    private float textScale;
    private int textLeading;
    private boolean textWrapEnabled;
    private boolean scissoringEnabled;


    protected float minimumHeight() {
        return textAreaPadding * 2;
    }

    protected float minimumWidth() {
        return textAreaPadding * 2;
    }

    protected float windowWiggleRoom() {
        return visibleArea.height() - desired_height;
    }



}
