package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.window.GUIWindow;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import io.github.heathensoft.jlib.lwjgl.window.CursorObject;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Window;
import io.github.heathensoft.jlib.common.utils.RectPacker;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 17/12/2022
 */


public class GUI implements Disposable {

    // CURSORS

    public static final int CURSOR_COUNT = 6;
    public static final int CURSOR_ARROW = 0;
    public static final int CURSOR_TEXT_INPUT = 1;
    public static final int CURSOR_CROSS_HAIR = 2;
    public static final int CURSOR_HAND = 3;
    public static final int CURSOR_H_RESIZE = 4;
    public static final int CURSOR_V_RESIZE = 5;

    // WINDOW

    public static final int WIN_BORDER_THICKNESS = 3;
    public static final Color WIN_BORDER_COLOR = Color.WHITE.cpy();
    public static final Color WIN_BG_IMG_COLOR = Color.EMPTY.cpy();
    public static final Color WIN_BG_COLOR = Color.EMPTY.cpy();

    // NAVIGATION

    public static final int NAV_BTN_AREA_WIDTH;
    public static final int NAV_BTN_AREA_HEIGHT;
    public static final int NAV_BTN_SIZE = 16;
    public static final int NAV_BTN_PADDING = 1;
    public static final int NAV_BAR_PADDING = 4;
    public static final int NAV_BAR_BORDER_THICKNESS = 2;
    public static final int NAV_TXT_HEIGHT = 16;
    public static final int NAV_TXT_CHAR_WIDTH = 8;
    public static final int NAV_TXT_AREA_HEIGHT;

    static {
        int outer = NAV_BAR_PADDING * 2;
        int inner = 3 * NAV_BTN_SIZE + 2 * NAV_BTN_PADDING;
        NAV_BTN_AREA_HEIGHT = outer + NAV_BTN_SIZE;
        NAV_BTN_AREA_WIDTH = outer + inner;
        NAV_TXT_AREA_HEIGHT = outer + NAV_TXT_HEIGHT;
    }

    public static final Color NAV_TXT_COLOR = Color.WHITE.cpy();
    public static final Color NAV_BG_COLOR = Color.EMPTY.cpy();
    public static final Color NAV_BTN_COLOR = Color.WHITE.cpy();
    public static final Color NAV_BTN_INACTIVE_COLOR = Color.WHITE.cpy();
    public static final Color NAV_BTN_CLOSE_HOVER_COLOR = Color.RED.cpy();
    public static final Color NAV_BTN_RESTORE_HOVER_COLOR = Color.GREEN.cpy();
    public static final Color NAV_BTN_MAXIMIZE_HOVER_COLOR = Color.GREEN.cpy();

    // SCROLLBARS

    public static final int SCROLLBAR_AREA_WIDTH = 11;
    public static final int SCROLLBAR_EDGE_HEIGHT = 5;
    public static final int SCROLLBAR_WHEEL_WIDTH = 7;
    public static final int SCROLLBAR_WHEEL_EXTRA = 1;
    public static final Color SCROLLBAR_BG_COLOR = Color.EMPTY.cpy();
    public static final Color SCROLLBAR_INNER_COLOR = Color.WHITE.cpy();
    public static final Color SCROLLBAR_OUTER_COLOR = Color.WHITE.cpy();

    // INSTANCE

    public final int WIDTH;
    public final int HEIGHT;
    public final GUIGraphics GRAPHICS;
    public final Matrix4f MATRIX;
    public final GUIShader SHADER;
    public final IDBuffer ID_BUFFER;
    public final GUIFramebuffer FRAMEBUFFER;
    private final CursorObject[] CURSORS;
    private int current_cursor;


    public GUI(int width, int height) throws Exception {
        WIDTH = width; HEIGHT = height;
        Matrix4f view = new Matrix4f();
        view.identity().lookAt(0,0,1,
        0,0,-1, 0,1,0);
        MATRIX = new Matrix4f();
        MATRIX.ortho(0,WIDTH,0,HEIGHT,0.01f,1);
        MATRIX.mul(view);
        SHADER = new GUIShader();
        GRAPHICS = new GUIGraphics();
        ID_BUFFER = new IDBuffer(width, height);
        FRAMEBUFFER = new GUIFramebuffer(ID_BUFFER,width,height);
        CURSORS = new CursorObject[CURSOR_COUNT];
        Window window = Engine.get().window();
        for (int i = 0; i < CURSOR_COUNT; i++) {
            Optional<CursorObject> opt =
            window.createCursor(GLFW_ARROW_CURSOR + i);
            if (opt.isPresent()) CURSORS[i] = opt.get();
        } GUIHelp.initialize(this);
    }

    /*
    public void repack() {
        rectangles = IntBuffer.allocate(5 * num * 2);
        Random random = new Random();
        int max = 128;
        int min = 4;
        for (int i = 0; i < num; i++) {
            int w = random.nextInt(max + 1 - min) + min;
            int h = random.nextInt(max + 1 - min) + min;
            rectangles.put(i).put(w).put(h);
        }
        max = 64;
        min = 8;
        for (int i = 0; i < num; i++) {
            int w = random.nextInt(max + 1 - min) + min;
            int h = random.nextInt(max + 1 - min) + min;
            rectangles.put(i).put(w).put(h);
        }

        rectangles.flip();

        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer bounds = stack.mallocInt(2);
            RectPacker.pack(rectangles,bounds);
            System.out.println(bounds.get(0) + " , " + bounds.get(1));
        }
    }

     */

    public void render(SpriteBatch batch, Vector2f mouseViewport) {
        Framebuffer.bindDraw(FRAMEBUFFER);
        Framebuffer.viewport();
        FRAMEBUFFER.clearColorBuffer();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        SHADER.prepare(this);
        batch.begin();
        Debug.render(batch,this);
        GUIWindow.renderAll(batch);
        GUIHelp.render(batch);
        /*
        float c0 = new Color(1.0f,0,0,1).toFloatBits();
        float c1 = new Color(0,1.0f,0,1).toFloatBits();
        float c2 = new Color(0,0,1.0f,1).toFloatBits();
        float c3 = new Color(1.0f,1.0f,0,1).toFloatBits();
        float c4 = new Color(0,1.0f,1.0f,1).toFloatBits();
        float c5 = new Color(1.0f,0,1.0f,1).toFloatBits();
        for (int i = 0; i < (num * 2); i++) {
            int offset = i * 5;
            int id = rectangles.get(offset);
            int w = rectangles.get(offset + 1);
            int h = rectangles.get(offset + 2);
            int x = rectangles.get(offset + 3);
            int y = rectangles.get(offset + 4);
            float c;
            switch (i % 6) {
                case 1 -> {c = c1;}
                case 2 -> {c = c2;}
                case 3 -> {c = c3;}
                case 4 -> {c = c4;}
                case 5 -> {c = c5;}
                default -> {c = c0;}
            }
            batch.draw(GRAPHICS.blank(),x,y,w,h,c,0);
        }

         */
        batch.end();
        ID_BUFFER.readID(mouseViewport);
    }

    public void update(float dt) {
        GUIWindow.updateAll(dt);
    }

    public void setCursor(int cursor) {
        if (cursor < 0 || cursor >= CURSOR_COUNT)
            cursor = CURSOR_ARROW;
        if (cursor != current_cursor) {
            CursorObject object = CURSORS[cursor];
            if (object != null) {
                current_cursor = cursor;
                object.use();
            }
        }
    }

    public int interactableData() {
        return ID_BUFFER.pixelID();
    }

    public void clearIdBuffer() {
        // Might be better to do this before rendering "world"
        // Clear together with the "entity" framebuffer
        Framebuffer.bindDraw(ID_BUFFER);
        Framebuffer.viewport();
        Framebuffer.clear();
    }

    public void dispose() {

        //if (rectangles != null) MemoryUtil.memFree(rectangles);

        GUIWindow.disposeAll();
        Disposable.dispose(SHADER);
        Disposable.dispose(GRAPHICS);
        Disposable.dispose(ID_BUFFER);
        Disposable.dispose(FRAMEBUFFER);
        for (CursorObject cursor : CURSORS)
            cursor.dispose();
    }

}
