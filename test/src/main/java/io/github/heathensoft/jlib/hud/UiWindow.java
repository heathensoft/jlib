package io.github.heathensoft.jlib.hud;

import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.ui.HUD;


/**
 * @author Frederik Dahl
 * 23/11/2022
 */


public abstract class UiWindow implements UiElement, Comparable<UiWindow> {

    protected HUD hud;
    protected String name;
    protected String title;
    protected Color backgroundColor;
    protected boolean open;
    protected boolean focus;
    protected float border;
    protected float margin;
    protected float padding;
    protected float x0;
    protected float y0;
    protected float w;
    protected float h;


    @Override
    public int compareTo(UiWindow o) {
        if (o.focus) return 1;
        if (focus)  return -1;
        return 0;
    }
}
