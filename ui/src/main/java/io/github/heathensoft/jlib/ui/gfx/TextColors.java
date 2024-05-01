package io.github.heathensoft.jlib.ui.gfx;

import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.ui.text.Keyword;
import io.github.heathensoft.jlib.ui.text.Paragraph;
import io.github.heathensoft.jlib.ui.text.Word;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 09/04/2024
 */


public class TextColors {


    public static List<String> toFileFormat() {
        List<String> list = new LinkedList<>();
        Paragraph.Type.toFileFormat(list);
        Word.Type.toFileFormat(list);
        return list;
    }

    public static void fromFileFormat(String fileFormat) {
        if (!fileFormat.isBlank()) {
            fromFileFormat(Arrays.asList(fileFormat.split("\n")));
        }
    }

    public static void fromFileFormat(List<String> fileFormat) {
        if (!fileFormat.isEmpty()) {
            Paragraph.Type.fromFileFormat(fileFormat);
            Word.Type.fromFileFormat(fileFormat);
        }
    }

    public static Vector4f colorOf(Paragraph p) { return p.type().color; }
    public static Vector4f colorOf(Paragraph.Type type) { return type.color; }
    public static Vector4f colorOf(Paragraph p, Word w) {
        if (w instanceof Keyword) return w.type().color;
        else return p.type().color;
    }

    public static float floatBits(Paragraph p, Word w) { return Color.rgb_to_floatBits(colorOf(p, w)); }
    public static float floatBits(Paragraph.Type type) { return Color.rgb_to_floatBits(type.color); }
    public static float floatBits(Paragraph p) { return Color.rgb_to_floatBits(colorOf(p)); }

    // Todo: What??
    public static float floatBits(Paragraph p, Word w, float alpha) {
        Vector4f rgb = U.popSetVec4(colorOf(p, w));
        if(alpha < 0.995f) {
            rgb.w *= alpha;
        } float floatBits = Color.rgb_to_floatBits(rgb);
        U.pushVec4();
        return floatBits;
    }

}
