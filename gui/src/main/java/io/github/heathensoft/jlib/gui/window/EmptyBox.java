package io.github.heathensoft.jlib.gui.window;



/**
 * @author Frederik Dahl
 * 09/11/2023
 */


public class EmptyBox extends Box {

    public EmptyBox() { this(0,0); }

    public EmptyBox(int width, int height) {
        this.restingSize.set(width,height);
        this.currentSize.set(restingSize());
    }

}
