package io.github.heathensoft.jlib.gui;

/**
 * @author Frederik Dahl
 * 29/05/2024
 */


public abstract class WindowADT {

    public static final int STATE_REGISTERED = 0x0001;
    public static final int STATE_OPEN = 0x0002;
    public static final int STATE_TRANSITIONING = 0x0004;
    public static final int STATE_IN_FOCUS = 0x0008;

    private int window_id;
    private int window_group;
    private int window_state;





    public <T extends WindowADT> T cast(Class<T> clazz) {
        if (getClass() != clazz) {
            throw new ClassCastException("GUI Window");
        } return clazz.cast(this);
    }

    public void setGroup(int group) {
        window_group = group;
    }


}
