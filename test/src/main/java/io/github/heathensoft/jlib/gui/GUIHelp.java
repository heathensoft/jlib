package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.gui.text.AsciiFont;
import io.github.heathensoft.jlib.gui.text.Text;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.SpriteBatch;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import org.joml.Vector2f;
import org.joml.Vector2i;

/**
 * @author Frederik Dahl
 * 27/12/2022
 */


public class GUIHelp {

    private static GUI gui;
    private static Text txt;
    private static Area area;
    private static String raw;
    private static int content_width;
    private static int content_height;
    private static boolean render;
    private static boolean initialized;
    public static final int FONT_SCALE = 1;
    public static final int TXT_LEADING = 2;
    public static final int BORDER_THICKNESS = 2;
    public static final int CONTAINER_PADDING = 0;
    public static final int DESIRED_CONTENT_WIDTH = 180;
    public static final Color TXT_COLOR = Color.WHITE.cpy();
    public static final Color BORDER_COLOR = Color.WHITE.cpy();
    public static final Color BG_COLOR = Color.RED.cpy();


    protected static void initialize(GUI gui) {
        if (!initialized) {
            GUIHelp.gui = gui;
            area = new Area();
            initialized = true;
        }
    }


    public static void render(SpriteBatch batch) {
        if (render) {
            drawBackground(batch);
            drawBorder(batch);
            drawText(batch);
            render = false;
        }

    }

    private static void drawBackground(SpriteBatch batch) {
        int x0 = area.minX();
        int y0 = area.minY();
        int w = area.cols();
        int h = area.rows();
        TextureRegion region = gui.GRAPHICS.blank();
        batch.draw(region,x0,y0,w,h,BG_COLOR.toFloatBits(),0);
    }

    private static void drawBorder(SpriteBatch batch) {

    }

    private static void drawText(SpriteBatch batch) {
        Debug.out("Content width: ",content_width);
        int x0 = area.minX() + CONTAINER_PADDING + BORDER_THICKNESS;
        int y0 = area.maxY() - CONTAINER_PADDING - BORDER_THICKNESS;
        float color = TXT_COLOR.toFloatBits();
        txt.render(batch,gui.GRAPHICS.tinyFont(),x0,y0,color,0,FONT_SCALE,content_width,TXT_LEADING);
    }

    public static void display(Text text, Vector2f mouse) {
        if (text != null) {
            int m_x = (int) mouse.x;
            int m_y = (int) mouse.y;
            Area windowArea = new Area(0,0,gui.WIDTH,gui.HEIGHT);
            if (windowArea.contains(m_x,m_y)) {
                if (GUIHelp.txt != text) {
                    AsciiFont font = gui.GRAPHICS.tinyFont();
                    Vector2i size = text.calculateBounds(font,DESIRED_CONTENT_WIDTH,FONT_SCALE,TXT_LEADING);
                    int width = size.x;
                    int height = size.y;
                    if (height == 0 || width == 0) return;
                    content_width = width;
                    content_height = height;
                    GUIHelp.txt = text;
                    raw = null;
                } alignBox(m_x, m_y);
                render = true;
            }
        }
    }

    public static void display(String string, Vector2f mouse) {
        if (string != null) {
            int m_x = (int) mouse.x;
            int m_y = (int) mouse.y;
            Area windowArea = new Area(0,0,gui.WIDTH,gui.HEIGHT);
            if (windowArea.contains(m_x,m_y)) {
                if (!string.equals(raw)) {
                    AsciiFont font = gui.GRAPHICS.tinyFont();
                    txt = new Text(string,false);
                   //content_height = txt.calculateHeight(font,FONT_SCALE, DESIRED_CONTENT_WIDTH,TXT_LEADING);
                    raw = string;
                } alignBox(m_x, m_y);
                render = true;
            }
        }
    }

    private static void alignBox(int m_x, int m_y) {
        int outer = 2 * (CONTAINER_PADDING + BORDER_THICKNESS);
        int w = content_width + outer;
        int h = content_height + outer;
        int y0;
        int x0 = m_x - (w/2);
        int x1 = x0 + w;
        int y1 = (m_y + 16) + h; // testing above mouse first
        if (y1 > gui.HEIGHT) {
            y1 = m_y - 16;
        } y0 = y1 - h;
        if (x0 < 0) {
            x0 = 0;
            x1 = x0 + w;
        } else if (x1 > gui.WIDTH) {
            x1 = gui.WIDTH;
            x0 = x1 - w;
        } area.set(x0,y0,x1,y1);
    }
}
