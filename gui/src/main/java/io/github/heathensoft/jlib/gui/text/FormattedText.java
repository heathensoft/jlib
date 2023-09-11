package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.lwjgl.gfx.Color32;


/**
 * @author Frederik Dahl
 * 10/09/2023
 */


public interface FormattedText {

    boolean isBlank();

    int length();

    String toString();


    static String wrapAsComment(String string) {
        if (string == null || string.isBlank()) return "";
        return "#" + string.trim().replaceAll("\\s+","_");
    }

    static String wrapAsEntity(String string) {
        if (string == null || string.isBlank()) return "";
        return "$" + string.trim().replaceAll("\\s+","_");
    }

    static String wrapAsKeyword(String string) {
        if (string == null || string.isBlank()) return "";
        return "%" + string.trim().replaceAll("\\s+","_");
    }

    static String wrapAsAction(String string) {
        if (string == null || string.isBlank()) return "";
        return "&" + string.trim().replaceAll("\\s+","_");
    }

    static String wrapAsActionSuccess(String string) {
        if (string == null || string.isBlank()) return "";
        return "&&" + string.trim().replaceAll("\\s+","_");
    }

    static String wrapAsActionFailure(String string) {
        if (string == null || string.isBlank()) return "";
        return "&&&" + string.trim().replaceAll("\\s+","_");
    }

    static String hexFormat(Color32 color) {
        return "0x" + color;
    }

    static String hexFormat(int i) {
        return "0x" + Integer.toHexString(i).toUpperCase();
    }

}
