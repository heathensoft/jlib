package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.lwjgl.gfx.Color32;

/**
 * @author Frederik Dahl
 * 11/09/2023
 */


public class ColorScheme {

    public Color32 text_color_default;
    public Color32 text_color_header;
    public Color32 text_color_numbers;
    public Color32 text_color_comments;
    public Color32 text_color_debug;
    public Color32 text_color_warning;
    public Color32 text_color_keywords;
    public Color32 text_color_entity;
    public Color32 text_color_action_neutral;
    public Color32 text_color_action_success;
    public Color32 text_color_action_failure;

    public Color32 gui_color_background_1;
    public Color32 gui_color_background_2;
    public Color32 gui_color_outline;
    public Color32 gui_color_highlight;
    public Color32 gui_color_inactive;
    public Color32 gui_color_cancel;


    public ColorScheme() { // Default Scheme (Darcula)
        text_color_default = new Color32("a9b7c6");
        text_color_header = new Color32("9876aa");
        text_color_numbers = new Color32("6897bb");
        text_color_comments = new Color32("808080");
        text_color_debug = new Color32("629755");
        text_color_warning = new Color32("bc3f3c");
        text_color_keywords = new Color32("cc7832");
        text_color_entity = new Color32("ffc66d");
        text_color_action_neutral = new Color32("9876aa");
        text_color_action_success = new Color32("77b767");
        text_color_action_failure = new Color32("bc3f3c");
    }
}
