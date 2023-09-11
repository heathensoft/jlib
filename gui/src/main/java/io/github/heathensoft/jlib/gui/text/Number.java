package io.github.heathensoft.jlib.gui.text;

import java.nio.charset.StandardCharsets;

/**
 * @author Frederik Dahl
 * 06/09/2023
 */


public class Number extends Word {

    protected Number(String string) {
        this(string.getBytes(StandardCharsets.US_ASCII));
    }

    protected Number(byte[] value) {
        super(value);
    }

}
