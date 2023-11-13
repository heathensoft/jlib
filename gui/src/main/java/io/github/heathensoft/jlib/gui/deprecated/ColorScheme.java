package io.github.heathensoft.jlib.gui.deprecated;


import org.json.simple.JSONObject;

/**
 * @author Frederik Dahl
 * 14/09/2023
 */


public class ColorScheme {

    private static final String OBJECT_NAME = "Text Colors";
    private static final String TEXT_BACKGROUND = "background";
    private static final String TEXT_DEFAULT = "default";
    private static final String TEXT_NUMBERS = "numbers";
    private static final String TEXT_HIGHLIGHT = "highlight";
    private static final String TEXT_COMMENTS = "comments";
    private static final String TEXT_DEBUGGING = "debugging";
    private static final String TEXT_WARNINGS = "warnings";
    private static final String TEXT_ACTION = "action";
    private static final String TEXT_ACTION_SUCCESS = "action_success";
    private static final String TEXT_ACTION_FAILURE = "action_failure";
    private static final String TEXT_ENTITY = "entity";
    private static final String TEXT_ENTITY_FRIENDLY = "entity_friendly";
    private static final String TEXT_ENTITY_HOSTILE = "entity_hostile";
    private static final String TEXT_CUSTOM_0 = "custom_0";
    private static final String TEXT_CUSTOM_1 = "custom_1";
    private static final String TEXT_CUSTOM_2 = "custom_2";
    private static final String TEXT_CUSTOM_3 = "custom_3";
    private static final String TEXT_CUSTOM_4 = "custom_4";
    private static final String TEXT_CUSTOM_5 = "custom_5";
    private static final String TEXT_CUSTOM_6 = "custom_6";
    private static final String TEXT_CUSTOM_7 = "custom_7";
    private static final String TEXT_CUSTOM_8 = "custom_8";
    private static final String TEXT_CUSTOM_9 = "custom_9";

    public Color32 text_default;
    public Color32 text_numbers;
    public Color32 text_highlight;
    public Color32 text_comments;
    public Color32 text_debugging;
    public Color32 text_warnings;
    public Color32 text_action;
    public Color32 text_action_success;
    public Color32 text_action_failure;
    public Color32 text_entity;
    public Color32 text_entity_friendly;
    public Color32 text_entity_hostile;
    public Color32 text_background;
    public Color32 text_custom_0;
    public Color32 text_custom_1;
    public Color32 text_custom_2;
    public Color32 text_custom_3;
    public Color32 text_custom_4;
    public Color32 text_custom_5;
    public Color32 text_custom_6;
    public Color32 text_custom_7;
    public Color32 text_custom_8;
    public Color32 text_custom_9;

    private ColorScheme() { }

    public Color32 colorOfWord(Word word) {
        if (!word.isRegularWord()) {
            if (word instanceof Keyword) {
                if (word instanceof Keyword.Value) {
                    return text_numbers;
                } else if (word instanceof Keyword.Entity) {
                    if (word instanceof Keyword.Entity.Friendly) {
                        return text_entity_friendly;
                    } else if (word instanceof Keyword.Entity.Hostile) {
                        return text_entity_hostile;
                    } return text_entity;
                } else if (word instanceof Keyword.Action) {
                    if (word instanceof Keyword.Action.Success) {
                        return text_action_success;
                    } else if (word instanceof Keyword.Action.Failure) {
                        return text_action_failure;
                    } return text_action;
                } else if (word instanceof Keyword.InlineComment w) {
                    return text_comments;
                } else if (word instanceof Keyword.Custom) {
                    if (word.getClass().equals(Keyword.Custom.class)) {
                        return text_custom_0;
                    } else if (word instanceof Keyword.Custom.C1) {
                        return text_custom_1;
                    } else if (word instanceof Keyword.Custom.C2) {
                        return text_custom_2;
                    } else if (word instanceof Keyword.Custom.C3) {
                        return text_custom_3;
                    } else if (word instanceof Keyword.Custom.C4) {
                        return text_custom_4;
                    } else if (word instanceof Keyword.Custom.C5) {
                        return text_custom_5;
                    } else if (word instanceof Keyword.Custom.C6) {
                        return text_custom_6;
                    } else if (word instanceof Keyword.Custom.C7) {
                        return text_custom_7;
                    } else if (word instanceof Keyword.Custom.C8) {
                        return text_custom_8;
                    } else if (word instanceof Keyword.Custom.C9) {
                        return text_custom_9;
                    }
                } return text_highlight;
            }
        } return text_default;
    }

    public Color32 colorOfParagraph(Paragraph paragraph) {
        if (paragraph instanceof PlainParagraph p)
            return colorOfPlain(p);
        else if (paragraph instanceof ParsedParagraph p)
            return colorOfParsed(p);
        return text_default;
    }

    public Color32 colorOfPlain(PlainParagraph paragraph) {
        if (paragraph instanceof ColoredParagraph p) return p.color();
        if (!paragraph.isRegularPlain()) {
            if (paragraph instanceof TextField c)
                if (!c.fieldIsValid()) return text_action_failure;
        } return text_default;
    }

    public Color32 colorOfParsed(ParsedParagraph paragraph) {
        if (paragraph instanceof ColoredParagraph p) return p.color();
        if (!paragraph.isRegularParsed()) {
            if (paragraph instanceof DebugLine) {
                return text_debugging;
            } else if (paragraph instanceof Highlighted) {
                return text_highlight;
            } else if (paragraph instanceof Comment) {
                return text_comments;
            } else if (paragraph instanceof Warning) {
                return text_warnings;
            }
        }
        return text_default;
    }


    public static ColorScheme fromJson(JSONObject jsonObject) throws Exception {
        exception:
        if (jsonObject != null && jsonObject.size() == 1) {
            ColorScheme scheme = new ColorScheme();
            Object text_obj = jsonObject.get(OBJECT_NAME);
            if (text_obj == null) break exception;
            try { JSONObject current_json_object;
                String color_string;
                current_json_object = (JSONObject) text_obj;
                color_string = (String) current_json_object.get(TEXT_BACKGROUND);
                if (color_string == null) break exception;
                scheme.text_background = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_DEFAULT);
                if (color_string == null) break exception;
                scheme.text_default = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_NUMBERS);
                if (color_string == null) break exception;
                scheme.text_numbers = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_HIGHLIGHT);
                if (color_string == null) break exception;
                scheme.text_highlight = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_COMMENTS);
                if (color_string == null) break exception;
                scheme.text_comments = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_DEBUGGING);
                if (color_string == null) break exception;
                scheme.text_debugging = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_WARNINGS);
                if (color_string == null) break exception;
                scheme.text_warnings = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_ACTION);
                if (color_string == null) break exception;
                scheme.text_action = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_ACTION_SUCCESS);
                if (color_string == null) break exception;
                scheme.text_action_success = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_ACTION_FAILURE);
                if (color_string == null) break exception;
                scheme.text_action_failure = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_ENTITY);
                if (color_string == null) break exception;
                scheme.text_entity = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_ENTITY_FRIENDLY);
                if (color_string == null) break exception;
                scheme.text_entity_friendly = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_ENTITY_HOSTILE);
                if (color_string == null) break exception;
                scheme.text_entity_hostile = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_0);
                if (color_string == null) break exception;
                scheme.text_custom_0 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_1);
                if (color_string == null) break exception;
                scheme.text_custom_1 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_2);
                if (color_string == null) break exception;
                scheme.text_custom_2 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_3);
                if (color_string == null) break exception;
                scheme.text_custom_3 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_4);
                if (color_string == null) break exception;
                scheme.text_custom_4 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_5);
                if (color_string == null) break exception;
                scheme.text_custom_5 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_6);
                if (color_string == null) break exception;
                scheme.text_custom_6 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_7);
                if (color_string == null) break exception;
                scheme.text_custom_7 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_8);
                if (color_string == null) break exception;
                scheme.text_custom_8 = new Color32(color_string);
                color_string = (String) current_json_object.get(TEXT_CUSTOM_9);
                if (color_string == null) break exception;
                scheme.text_custom_9 = new Color32(color_string);
            } catch (ClassCastException e) {
                break exception;
            } return scheme;
        } throw new  Exception("Unable to parse json object: " + ColorScheme.class.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {
        JSONObject main = new JSONObject();
        JSONObject object = new JSONObject();
        object.put(TEXT_DEFAULT, text_default.toString());
        object.put(TEXT_NUMBERS,text_numbers.toString());
        object.put(TEXT_HIGHLIGHT, text_highlight.toString());
        object.put(TEXT_COMMENTS,text_comments.toString());
        object.put(TEXT_DEBUGGING,text_debugging.toString());
        object.put(TEXT_WARNINGS,text_warnings.toString());
        object.put(TEXT_ACTION,text_action.toString());
        object.put(TEXT_ACTION_SUCCESS,text_action_success.toString());
        object.put(TEXT_ACTION_FAILURE,text_action_failure.toString());
        object.put(TEXT_ENTITY,text_entity.toString());
        object.put(TEXT_ENTITY_FRIENDLY,text_entity_friendly.toString());
        object.put(TEXT_ENTITY_HOSTILE,text_entity_hostile.toString());
        object.put(TEXT_CUSTOM_0,text_custom_0.toString());
        object.put(TEXT_CUSTOM_1,text_custom_1.toString());
        object.put(TEXT_CUSTOM_2,text_custom_2.toString());
        object.put(TEXT_CUSTOM_3,text_custom_3.toString());
        object.put(TEXT_CUSTOM_4,text_custom_4.toString());
        object.put(TEXT_CUSTOM_5,text_custom_5.toString());
        object.put(TEXT_CUSTOM_6,text_custom_6.toString());
        object.put(TEXT_CUSTOM_7,text_custom_7.toString());
        object.put(TEXT_CUSTOM_8,text_custom_8.toString());
        object.put(TEXT_CUSTOM_9,text_custom_9.toString());
        main.put(OBJECT_NAME,object);
        return main;
    }

    public static ColorScheme default_theme() {
        return retro_theme();
    }


    public static ColorScheme retro_theme() {
        ColorScheme scheme = new ColorScheme();
        scheme.text_default = new Color32("7c8691");
        scheme.text_numbers = new Color32("6c99a8");
        scheme.text_highlight = new Color32("b0aea0");
        scheme.text_comments = new Color32("525c68");
        scheme.text_debugging = new Color32("566e3a");
        scheme.text_warnings = new Color32("9c595d");
        scheme.text_action = new Color32("6f96a3");
        scheme.text_action_success = new Color32("babc81");
        scheme.text_action_failure = new Color32("9c595d");
        scheme.text_entity = new Color32("437087");
        scheme.text_entity_friendly = new Color32("88965d");
        scheme.text_entity_hostile = new Color32("9b6b79");
        scheme.text_background = new Color32("20281d");
        scheme.text_custom_0 = scheme.text_default.cpy();
        scheme.text_custom_1 = scheme.text_default.cpy();
        scheme.text_custom_2 = scheme.text_default.cpy();
        scheme.text_custom_3 = scheme.text_default.cpy();
        scheme.text_custom_4 = scheme.text_default.cpy();
        scheme.text_custom_5 = scheme.text_default.cpy();
        scheme.text_custom_6 = scheme.text_default.cpy();
        scheme.text_custom_7 = scheme.text_default.cpy();
        scheme.text_custom_8 = scheme.text_default.cpy();
        scheme.text_custom_9 = scheme.text_default.cpy();
        return scheme;
    }



}
