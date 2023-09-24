package io.github.heathensoft.jlib.gui.text;

import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import org.json.simple.JSONObject;

/**
 * @author Frederik Dahl
 * 14/09/2023
 */


public class ColorScheme {

    public String name;
    public Color32 text_letters;
    public Color32 text_numbers;
    public Color32 text_highlighted;
    public Color32 text_comments;
    public Color32 text_debugging;
    public Color32 text_warnings;
    public Color32 text_action;
    public Color32 text_action_success;
    public Color32 text_action_failure;
    public Color32 text_entity;
    public Color32 text_entity_friendly;
    public Color32 text_entity_hostile;


    private ColorScheme() { }


    public static ColorScheme fromJson(JSONObject jsonObject) throws Exception {
        exception:
        if (jsonObject != null && jsonObject.size() == 2) {
            ColorScheme scheme = new ColorScheme();
            Object name_obj = jsonObject.get("name");
            Object text_obj = jsonObject.get("text");
            //Object hud_obj = jsonObject.get("hud");
            if (name_obj == null || text_obj == null) break exception;
            try { JSONObject current_json_object;
                String color_string;
                scheme.name = (String) name_obj;
                if (scheme.name.isBlank()) break exception;
                current_json_object = (JSONObject) text_obj;
                color_string = (String) current_json_object.get("letters");
                if (color_string == null) break exception;
                scheme.text_letters = new Color32(color_string);
                color_string = (String) current_json_object.get("numbers");
                if (color_string == null) break exception;
                scheme.text_numbers = new Color32(color_string);
                color_string = (String) current_json_object.get("highlighted");
                if (color_string == null) break exception;
                scheme.text_highlighted = new Color32(color_string);
                color_string = (String) current_json_object.get("comments");
                if (color_string == null) break exception;
                scheme.text_comments = new Color32(color_string);
                color_string = (String) current_json_object.get("debugging");
                if (color_string == null) break exception;
                scheme.text_debugging = new Color32(color_string);
                color_string = (String) current_json_object.get("warnings");
                if (color_string == null) break exception;
                scheme.text_warnings = new Color32(color_string);
                color_string = (String) current_json_object.get("action");
                if (color_string == null) break exception;
                scheme.text_action = new Color32(color_string);
                color_string = (String) current_json_object.get("action_success");
                if (color_string == null) break exception;
                scheme.text_action_success = new Color32(color_string);
                color_string = (String) current_json_object.get("action_failure");
                if (color_string == null) break exception;
                scheme.text_action_failure = new Color32(color_string);
                color_string = (String) current_json_object.get("entity");
                if (color_string == null) break exception;
                scheme.text_entity = new Color32(color_string);
                color_string = (String) current_json_object.get("entity_friendly");
                if (color_string == null) break exception;
                scheme.text_entity_friendly = new Color32(color_string);
                color_string = (String) current_json_object.get("entity_hostile");
                if (color_string == null) break exception;
                scheme.text_entity_hostile = new Color32(color_string);
            } catch (ClassCastException e) {
                break exception;
            }
            return scheme;
        }
        throw new  Exception("Unable to parse json object");
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJsonObject() {
        JSONObject main = new JSONObject();
        main.put("name",name);
        {
            JSONObject object = new JSONObject();
            object.put("letters",text_letters.toString());
            object.put("numbers",text_numbers.toString());
            object.put("highlighted",text_highlighted.toString());
            object.put("comments",text_comments.toString());
            object.put("debugging",text_debugging.toString());
            object.put("warnings",text_warnings.toString());
            object.put("action",text_action.toString());
            object.put("action_success",text_action_success.toString());
            object.put("action_failure",text_action_failure.toString());
            object.put("entity",text_entity.toString());
            object.put("entity_friendly",text_entity_friendly.toString());
            object.put("entity_hostile",text_entity_hostile.toString());
            main.put("text",object);
        }
        {
            //JSONObject object = new JSONObject();
            //object.put("background",hud_background.toString());
            //main.put("hud",object);
        }

        return main;
    }

    public Color32 colorOf(Paragraph paragraph) {
        if (paragraph instanceof PHeader) {
            return text_highlighted;
        } else if (paragraph instanceof PComment) {
            return text_comments;
        } else if (paragraph instanceof PDebug) {
            return text_debugging;
        } else if (paragraph instanceof PWarning) {
            return text_warnings;
        } else if (paragraph instanceof PUnparsed) {
            if (paragraph instanceof PColored colored) {
                return colored.color();
            } else if (paragraph instanceof PEditor edit) {
                if (edit.isHighlighted()) {
                    return text_highlighted;
                } else return text_letters;
            }
        } return text_letters;
    }

    public Color32 colorOf(Word word) {
        if (word instanceof Keyword) {
            if (word instanceof Keyword.InlineComment) {
                return text_comments;
            } else if (word instanceof Keyword.Action) {
                if (word instanceof Keyword.Action.Success) {
                    return text_action_success;
                } else if (word instanceof Keyword.Action.Failure) {
                    return text_action_failure;
                } return text_action;
            } else if (word instanceof Keyword.Entity) {
                if (word instanceof Keyword.Entity.Friendly) {
                    return text_entity_friendly;
                } else if (word instanceof Keyword.Entity.Hostile) {
                    return text_entity_hostile;
                } return text_entity;
            } else if (word instanceof Keyword.Value) {
                return text_numbers;
            } return text_highlighted;
        } return text_letters;
    }

    public static ColorScheme default_theme() {
        return retro_theme();
    }


    public static ColorScheme retro_theme() {
        ColorScheme scheme = new ColorScheme();
        scheme.name = "Retro";
        scheme.text_letters = new Color32("7c8691");
        scheme.text_numbers = new Color32("6c99a8");
        scheme.text_highlighted = new Color32("b0aea0");
        scheme.text_comments = new Color32("525c68");
        scheme.text_debugging = new Color32("566e3a");
        scheme.text_warnings = new Color32("9c595d");
        scheme.text_action = new Color32("6f96a3");
        scheme.text_action_success = new Color32("babc81");
        scheme.text_action_failure = new Color32("9c595d");
        scheme.text_entity = new Color32("437087");
        scheme.text_entity_friendly = new Color32("88965d");
        scheme.text_entity_hostile = new Color32("9b6b79");
        return scheme;
    }



}
