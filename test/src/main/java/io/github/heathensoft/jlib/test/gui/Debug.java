package io.github.heathensoft.jlib.test.gui;

import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.test.graphicsOld.Color;
import io.github.heathensoft.jlib.test.graphicsOld.SpriteBatch;
import io.github.heathensoft.jlib.test.graphicsOld.TextureRegion;
import io.github.heathensoft.jlib.test.gui.window.Size;
import io.github.heathensoft.jlib.test.gui.text.AsciiFont;
import io.github.heathensoft.jlib.test.gui.text.Glyph;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Has to be used in the render loop.
 *
 *
 * @author Frederik Dahl
 * 26/12/2022
 */


public class Debug {

    private static final String TAB = "   ";
    private static final DecimalFormat DF = new DecimalFormat("0.00");
    private static final List<String> DEBUG_OUT = new ArrayList<>();
    private static final Color TEXT_COLOR = Color.WHITE.cpy();
    private static final Color NUMERALS_COLOR = Color.GREEN.cpy();
    private static final Color BACKGROUND_COLOR = new Color(0,0,0,0.7f);

    public static void render(SpriteBatch batch, GUI gui) {

        if (!DEBUG_OUT.isEmpty()) {
            GUIGraphics graphics = gui.GRAPHICS;
            AsciiFont font = graphics.amigaFont();
            TextureRegion bg = graphics.blank();
            float color_num = NUMERALS_COLOR.toFloatBits();
            float color_txt = TEXT_COLOR.toFloatBits();
            float color_bg = BACKGROUND_COLOR.toFloatBits();
            int advance = font.getChar('x').advance();
            int h = font.height();
            int margin = 12;
            int line_padding = 2;
            int padding = 1;
            int bg_h = h + padding + padding;
            int y0 = gui.HEIGHT - margin;
            int x0 = margin;
            for (String s : DEBUG_OUT) {
                y0 -= h;
                int characters = s.length();
                if (characters > 0) {
                    int bg_w = characters * advance + padding + padding;
                    batch.draw(bg,x0-padding,y0-padding,bg_w,bg_h,color_bg,0);
                    for (int i = 0; i < characters; i++) {
                        byte b = (byte) s.charAt(i);
                        Glyph g = font.getChar(b);
                        TextureRegion r = g.region();
                        if (b > 47 && b < 58) {
                            batch.draw(r,x0,y0,advance,h,color_num,0);
                        }  else batch.draw(r,x0,y0,advance,h,color_txt,0);
                        x0 += advance;
                    }
                }
                x0 = margin;
                y0 -= line_padding;
            }
            DEBUG_OUT.clear();
        }
    }

    public static void out(String string) {
        DEBUG_OUT.add(string);
    }

    public static void out(String name, int value) {
        String out = name + ": " + value;
        DEBUG_OUT.add(out);
    }

    public static void out(String name, float value) {
        String out = name + ": " + DF.format(value);
        DEBUG_OUT.add(out);
    }

    public static void out(String name, Vector2f value) {
        String x = "x: " + DF.format(value.x);
        String y = "y: " + DF.format(value.y);
        String out = name + ": [" + x + " , " + y + "]";
        DEBUG_OUT.add(out);
    }

    public static void out(String name, Vector3f value) {
        String x = "x: " + DF.format(value.x);
        String y = "y: " + DF.format(value.y);
        String z = "z: " + DF.format(value.z);
        String out = name + ": [" + x + " , " + y + " , " + z + "]";
        DEBUG_OUT.add(out);
    }

    public static void out(String name, Vector2i value) {
        String x = "x: " + value.x;
        String y = "y: " + value.y;
        String out = name + ": [" + x + " , " + y + "]";
        DEBUG_OUT.add(out);
    }

    public static void out(String name, Size value) {
        String w = "w: " + DF.format(value.width());
        String h = "h: " + DF.format(value.height());
        String out = name + ": [" + w + " , " + h + "]";
        DEBUG_OUT.add(out);
    }

    public static void out(String name, Coordinate value) {
        String x = "x: " + value.x;
        String y = "y: " + value.y;
        String out = name + ": [" + x + " , " + y + "]";
        DEBUG_OUT.add(out);
    }

    public static void out(String name, Area value) {
        String minX = "minX: " + value.minX();
        String minY = "minY: " + value.minY();
        String maxX = "maxX: " + value.maxX();
        String maxY = "maxY: " + value.maxY();
        String out = name + ": [" + minX + " , " + minY + " , " + maxX + " , " + maxY +"]";
        DEBUG_OUT.add(out);
    }

}
