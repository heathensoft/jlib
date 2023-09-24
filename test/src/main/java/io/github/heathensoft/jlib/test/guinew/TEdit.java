package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.gui.textnew.PlainText;
import io.github.heathensoft.jlib.gui.textnew.TextEditor;

/**
 * @author Frederik Dahl
 * 23/09/2023
 */


public class TEdit extends TextEditor {

    public TEdit() {
        super();
    }

    public TEdit(PlainText text) {
        super(text);
    }

    protected void onMoveUp(int lineIndex) {

    }

    protected void onMoveDown(int lineIndex) {

    }

    protected void onEdit() {
        System.out.println(text());
    }
}
