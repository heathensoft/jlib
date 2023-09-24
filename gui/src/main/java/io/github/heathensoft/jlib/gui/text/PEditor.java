package io.github.heathensoft.jlib.gui.text;

/**
 * @author Frederik Dahl
 * 17/09/2023
 */


public class PEditor extends PUnparsed {

    private boolean highlighted;

    protected PEditor(String text) {
        super(text);
    }

    public void toggleHighlight(boolean on) {
        highlighted = on;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
}
