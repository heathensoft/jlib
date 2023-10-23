package io.github.heathensoft.jlib.test.guinew;


import io.github.heathensoft.jlib.gui.text.CommandLine;

/**
 * @author Frederik Dahl
 * 19/10/2023
 */


public class Cmd extends CommandLine {

    public Cmd() {
        super();
    }

    public Cmd(int capacity) {
        super(capacity);
    }

    public Cmd(int colorIndexValid, int colorIndexInvalid, int capacity) {
        super(colorIndexValid, colorIndexInvalid, capacity);
    }


}
