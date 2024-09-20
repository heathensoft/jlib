package io.github.heathensoft.jlib.common.text.code;

import io.github.heathensoft.jlib.common.utils.Color;
import org.joml.Vector4f;

/**
 * @author Frederik Dahl
 * 20/05/2024
 */


public enum CodeColor {
    DEFAULT(Color.hex_to_rgb("A9B7C6FF"),"Default"),
    NUMBERS(Color.hex_to_rgb("6897BBFF"),"Numbers"),
    KEYWORDS(Color.hex_to_rgb("CC7832FF"),"Keywords"),
    DATATYPES(Color.hex_to_rgb("9876AAFF"),"Datatypes"),
    COMMENTS(Color.hex_to_rgb("808080FF"),"Comments"),
    STRINGS(Color.hex_to_rgb("6A8759FF"),"Strings"),
    SPECIAL(Color.hex_to_rgb("F92674FF"),"Special"); // ALL CAPS, @Annotations and #preprocessor
    public final Vector4f color;
    public final String descriptor;
    public static final CodeColor[] array = values();
    public static CodeColor typeByOrdinal(int id) { return array[id]; }
    CodeColor(Vector4f color, String descriptor) {
        this.descriptor = descriptor;
        this.color = color;
    }


}
