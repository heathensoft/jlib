package io.github.heathensoft.jlib.gui.text;


import io.github.heathensoft.jlib.graphicsOld.Color;
import io.github.heathensoft.jlib.gui.interactable.UInteractable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * json format:
 *
 * {
 *     "keywords": [
 *     {
 *         "keyword" : "keyword",
 *         "color-default" : "F4AA0008",
 * 		   "color-hovered" : "315555EC",
 * 		   "description" : "..."
 *
 *     },
 *
 *     {
 *         ...
 *     }
 *
 *     ]
 * }
 *
 * @author Frederik Dahl
 * 29/11/2022
 */


public class InteractableWord extends UInteractable {

    private static Map<String, InteractableWord> map;

    private float currentColor;
    private final float defaultColor;
    private final float hoveredColor;
    private final String description;
    private final String keyword;

    private InteractableWord(String keyword, String description, Color defaultColor, Color hoveredColor) {
        this.keyword = keyword;
        this.description = description;
        this.defaultColor = defaultColor.toFloatBits();
        this.hoveredColor = hoveredColor.toFloatBits();
        this.currentColor = this.defaultColor;
        iRegisterInteractable();
        setOnCursorHover(position -> {
            final InteractableWord iWord = InteractableWord.this;
            iWord.currentColor = iWord.hoveredColor;});
        setOnCursorLeave(() -> {
            final InteractableWord iWord = InteractableWord.this;
            iWord.currentColor = iWord.defaultColor;});
    }

    public float color() {
        return currentColor;
    }

    public String description() {
        return description;
    }

    public String keyword() {
        return keyword;
    }

    public static InteractableWord get(String keyword) {
        return map == null ? null : map.get(keyword);
    }

    public static void load(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        Object mainObject = parser.parse(json);
        JSONObject jsonObject = (JSONObject)mainObject;
        JSONArray jsonArray = (JSONArray)jsonObject.get("keywords");
        parse_exception:
        if (jsonArray != null) {
            int new_words = jsonArray.size();
            if (new_words > 0) {
                if (map == null) {
                    map = new HashMap<>((int)Math.ceil(new_words * 1.34));
                } for (Object obj : jsonArray) {
                    JSONObject jsonKeyword = (JSONObject) obj;
                    if (jsonKeyword == null) break parse_exception;
                    String keyword = (String) jsonKeyword.get("keyword");
                    if (keyword == null) break parse_exception;
                    String color_default = (String) jsonKeyword.get("color-default");
                    if (color_default == null) break parse_exception;
                    int l = color_default.length();
                    if (l < 6 || l > 8) break parse_exception;
                    String color_hovered = (String) jsonKeyword.get("color-hovered");
                    if (color_hovered == null) break parse_exception;
                    l = color_hovered.length();
                    if (l < 6 || l > 8) break parse_exception;
                    String description = (String) jsonKeyword.get("description");
                    if (description == null) break parse_exception;
                    description = description.replace("\t","");
                    try { InteractableWord iWord = new InteractableWord(keyword,description,
                        Color.valueOf(color_default),Color.valueOf(color_hovered));
                        map.put(iWord.keyword,iWord);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        break parse_exception;
                    }
                }
            } return;
        } clear();
        throw new ParseException(ParseException.ERROR_UNEXPECTED_EXCEPTION);
    }

    public static List<Word> parseParagraph(String line) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^%]\\S*|%.+?%)\\s*").matcher(line);
        while (m.find()) list.add(m.group(1));
        List<Word> words = new ArrayList<>(list.size());
        for (String s : list) {
            if (s.startsWith("%") && s.endsWith("%")) {
                s = s.replace("%", "");
                InteractableWord iWord = get(s);
                if (iWord != null) {
                    words.add(new Keyword(iWord));
                    continue;}
            } words.add(new Word(s.getBytes(
              StandardCharsets.US_ASCII)));
        } return words;
    }

    public static void clear() {
        if (map != null) {
            for (var entry : map.entrySet()) {
                entry.getValue().dispose();
            } map.clear();
            map = null;
        }
    }

    @Override
    public void dispose() {
        iRemoveInteractable();
    }
}
