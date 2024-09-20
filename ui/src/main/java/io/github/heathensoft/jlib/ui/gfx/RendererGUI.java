package io.github.heathensoft.jlib.ui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.Executor;
import io.github.heathensoft.jlib.common.storage.generic.Pool;
import io.github.heathensoft.jlib.common.storage.generic.Stack;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.text.Text;
import io.github.heathensoft.jlib.ui.text.TextAlignment;
import io.github.heathensoft.jlib.ui.text.TextEditor;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;
import org.joml.primitives.Rectanglei;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.LinkedList;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static java.lang.Math.max;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_BLEND;
import static org.lwjgl.opengl.GL32.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL32.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL32.GL_MAX;
import static org.lwjgl.opengl.GL32.GL_ONE;
import static org.lwjgl.opengl.GL32.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL32.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL32.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL32.glBlendEquation;
import static org.lwjgl.opengl.GL32.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL32.glDisable;
import static org.lwjgl.opengl.GL32.glEnable;
import static org.lwjgl.opengl.GL32.glScissor;
import static org.lwjgl.opengl.GL40.glBlendEquationi;
import static org.lwjgl.opengl.GL40.glBlendFunci;

/**
 * Master Renderer for GUI. Batch draw for quads. begin -> draw gui -> end.
 * REMEMBER YOU CAN SKIP RENDER ELEMENT ID (by using SKIP_ID as id argument) // have this a state instead
 * Attempting to draw disposed textures will result in pink output colors (Color.ERROR_BITS)
 *
 * @author Frederik Dahl
 * 14/10/2023
 */


public class RendererGUI implements Disposable {

    public static final int SKIP_ID = -1;
    public static final int FONT_UNIFORM_BUFFER_BINDING_POINT = 0;

    // CONSTANTS
    private static final int TEXT_BATCH_CAPACITY = 2048;
    private static final int SPRITE_BATCH_CAPACITY = 512;
    private static final int FRAMEBUFFER_SLOT_DIFFUSE = 0;
    private static final int FRAMEBUFFER_SLOT_NORMALS = 1;
    private static final int FRAMEBUFFER_SLOT_EMISSIVE = 2;
    private static final int FRAMEBUFFER_SLOT_PIXEL_ID = 3;

    // RENDER STATE
    private static final int NULL_BATCH = 0;
    private static final int TEXT_BATCH = 1;
    private static final int SPRITE_BATCH = 2;

    // values are reset every 60 frame
    private int shader_swaps_max;
    private int shader_swaps;
    private int draw_calls_max;
    private int draw_calls;
    private int active_batch;
    private int frame_count;
    private int pixel_id;
    private boolean rendering;
    private boolean rendering_delayed;
    private boolean paused;

    private Framebuffer[] bloomBuffers;
    private Framebuffer framebuffer;
    private final FontsGUI fonts;
    private final SpriteBatchGUI spriteBatch;
    private final TextBatchGUI textBatch;
    private final ScissorStack scissorStack;
    private final LinkedList<Executor> delayed_calls;

    public RendererGUI(int width, int height) throws Exception {
        initializeFramebuffer(width, height);
        initializeBloomBuffers(width, height);
        fonts = new FontsGUI(FONT_UNIFORM_BUFFER_BINDING_POINT);
        textBatch = new TextBatchGUI(fonts,TEXT_BATCH_CAPACITY,width,height);
        spriteBatch = new SpriteBatchGUI(SPRITE_BATCH_CAPACITY,width,height);
        scissorStack = new ScissorStack(this);
        delayed_calls = new LinkedList<>();
        fonts.uploadDefaultFonts();
    }

    public void updateResolution(int width, int height) throws Exception {
        if (rendering) throw new IllegalStateException("Illegal attempt to update resolution while rendering");
        if (framebuffer.width() != width || framebuffer.height() != height) {
            initializeFramebuffer(width, height);
            initializeBloomBuffers(width, height);
            textBatch.updateResolution(width, height);
            spriteBatch.updateResolution(width, height);
        }
    }

    public void dispose() {
        Disposable.dispose(spriteBatch,textBatch,framebuffer,fonts);
        Disposable.dispose(bloomBuffers);
    }
    public FontsGUI fonts() { return fonts; }
    public Framebuffer framebuffer() { return framebuffer; }
    public TextBatchGUI textBatch() { return textBatch; }
    public SpriteBatchGUI spriteBatch() { return spriteBatch; }
    public Texture bloomTexture() { return bloomBuffers[0].texture(0); } // TODO: Temp **************************
    public Texture framebufferDiffuseTexture() { return framebuffer.texture(FRAMEBUFFER_SLOT_DIFFUSE); }
    public Texture framebufferNormalsTexture() { return framebuffer.texture(FRAMEBUFFER_SLOT_NORMALS); }
    public Texture framebufferEmissiveTexture() { return framebuffer.texture(FRAMEBUFFER_SLOT_EMISSIVE); }
    public int pixelID() { return pixel_id; }
    public long drawCallCount() { return draw_calls_max;  }
    public long shaderSwapCount() { return shader_swaps_max; }
    public void uploadFont(BitmapFont font, int slot) throws Exception { fonts.uploadFont(font,slot); }
    public boolean pushScissor(Rectanglef quad) { if (rendering &! paused) return scissorStack.push(quad);
        else throw new IllegalStateException("Cannot push/pop scissors while renderer is not rendering / paused");
    }public boolean pushScissor(float x1, float y1, float x2, float y2) {
        if (rendering &! paused) return scissorStack.push(x1, y1, x2, y2);
        else throw new IllegalStateException("Cannot push/pop scissors while renderer is not rendering / paused");
    }public void popScissor() { if (rendering &! paused) scissorStack.pop();
        else throw new IllegalStateException("Cannot push/pop scissors while renderer is not rendering / paused");
    }

    public void begin(Vector2f mouse) {
        if (!rendering) {
            if (frame_count == 60) {
                frame_count = 0;
                shader_swaps_max = shader_swaps;
                draw_calls_max = draw_calls;
            } shader_swaps = 0;
            int mouse_screen_x = round(mouse.x * framebuffer.width());
            int mouse_screen_y = round(mouse.y * framebuffer.height());
            Framebuffer.bind(framebuffer);
            Framebuffer.viewport();
            try (MemoryStack stack = MemoryStack.stackPush()){
                IntBuffer buffer = stack.callocInt(1);
                glReadPixels(mouse_screen_x,mouse_screen_y,1,1, GL_RED_INTEGER, GL_UNSIGNED_INT,buffer);
                pixel_id = buffer.get(0);
            }
            Framebuffer.drawBuffers(0,1,2,3);
            Framebuffer.clear();
            glEnable(GL_BLEND); // Blending diffuse and normals
            glDisable(GL_SCISSOR_TEST);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            //glBlendEquationi(2,GL_MAX); // Blending emissive
            //glBlendFunci(2,GL_ONE,GL_ONE);
            //glBlendFunci(2,GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
            rendering = true;
        }
    }

    public void end() {
        if (rendering) {
            while (!delayed_calls.isEmpty()) {
                // delayed calls has no id. And glow should be overridden. Not GL_MAX
                delayed_calls.removeLast().apply();
            } frame_count++;
            spriteBatch.flush();
            textBatch.flush();
            scissorStack.reset();
            draw_calls = spriteBatch.resetDrawCalls();
            draw_calls += textBatch.resetDrawCalls();
            draw_calls_max = max(draw_calls, draw_calls_max);
            shader_swaps_max = max(shader_swaps, shader_swaps_max);
            active_batch = NULL_BATCH;


            int bloom_iterations = U.clamp(GUI.variables.bloom_ping_pong_iterations,0,100);
            if (GUI.variables.bloom_enabled && bloom_iterations > 0) {
                // Clear and Fill the initial ping pong texture (index 0)
                Framebuffer.bind(bloomBuffers[0]);
                Framebuffer.drawBuffer(0);
                Framebuffer.viewport();
                Framebuffer.clear();
                ShaderProgram.bindProgram(GUI.shaders.bloom_threshold);
                Texture diffuse = framebuffer().texture(FRAMEBUFFER_SLOT_DIFFUSE);
                Texture emissive = framebuffer().texture(FRAMEBUFFER_SLOT_EMISSIVE);
                ShaderProgram.setUniform(ShaderProgram.UNIFORM_DIFFUSE,diffuse.bindTooAnySlot());
                ShaderProgram.setUniform(ShaderProgram.UNIFORM_EMISSIVE,emissive.bindTooAnySlot());
                ShaderProgram.setUniform("u_threshold",U.clamp(GUI.variables.bloom_threshold,0,2));

                glDisable(GL_BLEND);
                ShaderProgram.shaderPass().draw();
                ShaderProgram.bindProgram(GUI.shaders.bloom_ping_pong);
                for (int i = 0; i < bloom_iterations; i++) {
                    for (int j = 0; j < 2; j++) {
                        Framebuffer.bind(bloomBuffers[(j+1) % 2]);
                        Texture source = bloomBuffers[j].texture(0);
                        ShaderProgram.setUniform(ShaderProgram.UNIFORM_SAMPLER_2D,source.bindTooAnySlot());
                        ShaderProgram.setUniform("u_horizontal",(j+1) % 2);
                        ShaderProgram.shaderPass().drawRepeat();
                    }
                }
            }


            rendering = false;
            paused = false;
        }
    }



    public void pause() {
        if (rendering) {
            if (!paused) {
                spriteBatch.flush();
                textBatch.flush();
                scissorStack.pause();
                active_batch = NULL_BATCH;
                paused = true;
            }
        }
    }

    public void resume() {
        if (rendering) {
            if (paused) {
                Framebuffer.bind(framebuffer);
                Framebuffer.viewport();
                Framebuffer.drawBuffers(0,1,2,3);
                glEnable(GL_BLEND); // Blending diffuse and normals
                glBlendEquation(GL_FUNC_ADD);
                glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
                glBlendEquationi(2,GL_MAX); // Blending emissive
                glBlendFunci(2,GL_ONE,GL_ONE);
                scissorStack.resume();
                paused = false;
            }
        }
    }

    /** Use this when rendering items grabbed from and to containers*/
    public void drawDelayed(Executor function) { delayed_calls.addFirst(function); }
    public void drawTooltip(String string, Vector2f mouse_position) { GUI.tooltips.display(string, mouse_position); }
    public void drawTooltip(String string, Vector2f mouse_position, int color) { GUI.tooltips.display(string, mouse_position, color); }


    public void drawAsciiEditor(TextEditor text, Rectanglef bounds, Vector4f rgb, float y_offset, int font, float padding, float size, float glow, boolean wrap, boolean show_cursor) {
        if (rendering && !paused && size > 1f) {
            Rectanglef r = popSetRect(
                    bounds.minX + padding,
                    bounds.minY + padding,
                    bounds.maxX - padding,
                    bounds.maxY - padding
            ); if (r.isValid()) {
                if (active_batch != TEXT_BATCH) {
                    if (active_batch == SPRITE_BATCH) {
                        spriteBatch.flush();
                        shader_swaps++;
                    } active_batch = TEXT_BATCH;
                    Framebuffer.drawBuffers(0,1,2);
                } fonts.bindFontMetrics(font);
                if (scissorStack.push(r)) {
                    text.draw(textBatch,r,rgb,y_offset,size,glow,wrap,show_cursor);
                    scissorStack.pop();
                }
            } U.pushRect();
        }
    }


    public void drawText(Text text, Rectanglef bounds, int font, float size) { drawText(text,bounds,font,size,false); }
    public void drawText(Text text, Rectanglef bounds, int font, float size, boolean show_cursor) { drawText(text,bounds,font,0,size,show_cursor); }
    public void drawText(Text text, Rectanglef bounds, int font, float padding, float size, boolean show_cursor) { drawText(text,bounds,font,padding,size,false,show_cursor); }
    public void drawText(Text text, Rectanglef bounds, int font, float padding, float size, boolean wrap, boolean show_cursor) { drawText(text,bounds,0,font,padding,size,wrap,show_cursor); }
    public void drawText(Text text, Rectanglef bounds, float y_offset, int font, float padding, float size, boolean wrap, boolean show_cursor) { drawText(text,bounds,y_offset,font,padding,size,0,wrap,show_cursor); }
    public void drawText(Text text, Rectanglef bounds, float y_offset, int font, float padding, float size, float glow, boolean wrap, boolean show_cursor) {
        if (rendering && !paused && size > 1f && !text.isBlank()) {
            Rectanglef r = U.popSetRect(
                    bounds.minX + padding,
                    bounds.minY + padding,
                    bounds.maxX - padding,
                    bounds.maxY - padding
            ); if (r.isValid()) {
                if (active_batch != TEXT_BATCH) {
                    if (active_batch == SPRITE_BATCH) {
                        spriteBatch.flush();
                        shader_swaps++;
                    } active_batch = TEXT_BATCH;
                    Framebuffer.drawBuffers(0,1,2);
                } fonts.bindFontMetrics(font);
                if (scissorStack.push(r)) {
                    text.draw(textBatch,r,y_offset,size,glow,wrap,show_cursor);
                    scissorStack.pop();
                }
            } U.pushRect();
        }
    }

    public void drawText(Text text, Rectanglef bounds, Vector4f rgb, int font, float size) { drawText(text, bounds, rgb, font, size,false); }
    public void drawText(Text text, Rectanglef bounds, Vector4f rgb, int font, float size, boolean show_cursor) { drawText(text, bounds, rgb, font, 0, size, show_cursor); }
    public void drawText(Text text, Rectanglef bounds, Vector4f rgb, int font, float padding, float size, boolean show_cursor) { drawText(text, bounds, rgb, font, padding, size, false, show_cursor); }
    public void drawText(Text text, Rectanglef bounds, Vector4f rgb, int font, float padding, float size, boolean wrap, boolean show_cursor) { drawText(text, bounds, rgb, 0.0f, font, padding, size, wrap, show_cursor); }
    public void drawText(Text text, Rectanglef bounds, Vector4f rgb, float y_offset, int font, float padding, float size, boolean wrap, boolean show_cursor) { drawText(text, bounds, rgb, y_offset, font, padding, size, 0.0f, wrap, show_cursor); }
    public void drawText(Text text, Rectanglef bounds, Vector4f rgb, float y_offset, int font, float padding, float size, float glow, boolean wrap, boolean show_cursor) {
        if (rendering && !paused && size > 1f && !text.isBlank()) {
            Rectanglef r = popSetRect(
                    bounds.minX + padding,
                    bounds.minY + padding,
                    bounds.maxX - padding,
                    bounds.maxY - padding
            ); if (r.isValid()) {
                if (active_batch != TEXT_BATCH) {
                    if (active_batch == SPRITE_BATCH) {
                        spriteBatch.flush();
                        shader_swaps++;
                    } active_batch = TEXT_BATCH;
                    Framebuffer.drawBuffers(0,1,2);
                } fonts.bindFontMetrics(font);
                if (scissorStack.push(r)) {
                    text.draw(textBatch,r,rgb,y_offset,size,glow,wrap,show_cursor);
                    scissorStack.pop();
                }
            } U.pushRect();
        }
    }

    public void drawStringFixedSize(String string, int font, int abgr, float x, float y, float size) { drawStringFixedSize(string,font,abgr,x,y,Float.MAX_VALUE,size); }
    public void drawStringFixedSize(String string, int font, int abgr, float x, float y, float width, float size) { drawStringFixedSize(string,TextAlignment.LEFT,font,abgr,x,y,width,size); }
    public void drawStringFixedSize(String string, TextAlignment alignment, int font, int abgr, float x, float y, float width, float size) { drawStringFixedSize(string,alignment,font,abgr,x,y,width,size,0); }
    public void drawStringFixedSize(String string, TextAlignment alignment, int font, int abgr, float x, float y, float width, float size, float glow) {
        if (rendering && !paused && size > 1f && width > 0 && string != null && !string.isBlank()) {
            if (active_batch != TEXT_BATCH) {
                if (active_batch == SPRITE_BATCH) {
                    spriteBatch.flush();
                    shader_swaps++;
                } active_batch = TEXT_BATCH;
                Framebuffer.drawBuffers(0,1,2);
            } fonts.bindFontMetrics(font);
            textBatch.drawFixedSize(string,alignment,abgr,x,y,width,size,glow);
        }
    }

    public void drawStringFixedSize(String string, Rectanglef bounds, int font, int abgr) { drawStringFixedSize(string,TextAlignment.LEFT,bounds,font,abgr); }
    public void drawStringFixedSize(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr) { drawStringFixedSize(string,alignment,bounds,font,abgr,0); }
    public void drawStringFixedSize(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr, int padding) { drawStringFixedSize(string,alignment,bounds,font,abgr,padding,0); }
    public void drawStringFixedSize(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr, int padding, float glow) {
        final float x1 = bounds.minX + padding;
        final float y1 = bounds.minY + padding;
        final float x2 = bounds.maxX - padding;
        final float y2 = bounds.maxY - padding;
        if (x2 > x1 && y2 > y1) drawStringFixedSize(string,alignment,font,abgr,x1,y2,x2 - x1,y2 - y1,glow);
    }

    public void drawStringDynamicSize(String string, int font, int abgr, float x, float y, float size) { drawStringDynamicSize(string,font,abgr,x,y,Float.MAX_VALUE,size); }
    public void drawStringDynamicSize(String string, int font, int abgr, float x, float y, float width, float size) { drawStringDynamicSize(string,TextAlignment.LEFT,font,abgr,x,y,width,size); }
    public void drawStringDynamicSize(String string, TextAlignment alignment, int font, int abgr, float x, float y, float width, float size) { drawStringDynamicSize(string,alignment,font,abgr,x,y,width,size,0); }
    public void drawStringDynamicSize(String string, TextAlignment alignment, int font, int abgr, float x, float y, float width, float size, float glow) {
        if (rendering && !paused && size > 1f && width > 0 && string != null && !string.isBlank()) {
            if (active_batch != TEXT_BATCH) {
                if (active_batch == SPRITE_BATCH) {
                    spriteBatch.flush();
                    shader_swaps++;
                } active_batch = TEXT_BATCH;
                Framebuffer.drawBuffers(0,1,2);
            } fonts.bindFontMetrics(font);
            textBatch.drawDynamicSize(string,alignment,abgr,x,y,width,size,glow);
        }
    }

    public void drawStringDynamicSize(String string, Rectanglef bounds, int font, int abgr) { drawStringDynamicSize(string,TextAlignment.LEFT,bounds,font,abgr); }
    public void drawStringDynamicSize(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr) { drawStringDynamicSize(string,alignment,bounds,font,abgr,0); }
    public void drawStringDynamicSize(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr, int padding) { drawStringDynamicSize(string,alignment,bounds,font,abgr,padding,0); }
    public void drawStringDynamicSize(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr, int padding, float glow) {
        final float x1 = bounds.minX + padding;
        final float y1 = bounds.minY + padding;
        final float x2 = bounds.maxX - padding;
        final float y2 = bounds.maxY - padding;
        if (x2 > x1 && y2 > y1) drawStringDynamicSize(string,alignment,font,abgr,x1,y2,x2 - x1,y2 - y1,glow);
    }

    public void drawStringDynamicVerticalCentered(String string, Rectanglef bounds, int font, int abgr) { drawStringDynamicVerticalCentered(string,TextAlignment.LEFT,bounds,font,abgr); }
    public void drawStringDynamicVerticalCentered(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr) { drawStringDynamicVerticalCentered(string,alignment,bounds,font,abgr,0); }
    public void drawStringDynamicVerticalCentered(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr, int padding) { drawStringDynamicVerticalCentered(string,alignment,bounds,font,abgr,padding,0); }
    public void drawStringDynamicVerticalCentered(String string, TextAlignment alignment, Rectanglef bounds, int font, int abgr, int padding, float glow) {
        final float x1 = bounds.minX + padding;
        final float y1 = bounds.minY + padding;
        final float x2 = bounds.maxX - padding;
        final float y2 = bounds.maxY - padding;
        if (x2 > x1 && y2 > y1) {
            final float width = x2 - x1;
            final float height = y2 - y1;
            if (rendering && !paused && height > 1f && width > 0 && string != null && !string.isBlank()) {
                if (active_batch != TEXT_BATCH) {
                    if (active_batch == SPRITE_BATCH) {
                        spriteBatch.flush();
                        shader_swaps++;
                    } active_batch = TEXT_BATCH;
                    Framebuffer.drawBuffers(0,1,2);
                } fonts.bindFontMetrics(font);
                textBatch.drawDynamicVerticalCentered(string,alignment,abgr,x1,y2,width,height,glow);
            }
        }
    }

    public void drawSprite(Sprite sprite, Vector2f center) { drawSprite(sprite, center, 0xFFFFFFFF); }
    public void drawSprite(Sprite sprite, Vector2f center, int abgr) { drawSprite(sprite, center, abgr, 0); }
    public void drawSprite(Sprite sprite, Vector2f center, int abgr, int id) { drawSprite(sprite, center, 1f, abgr, id); }
    public void drawSprite(Sprite sprite, Vector2f center, float scale, int abgr, int id) { drawSprite(sprite, center, scale, abgr, id, 0f); }
    public void drawSprite(Sprite sprite, Vector2f center, float scale, int abgr, int id, float glow) { drawSprite(sprite, center, scale, abgr, id, glow,true); }
    public void drawSprite(Sprite sprite, Vector2f center, float scale, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering &! paused) {
            float wh = (sprite.width()  * scale) / 2f;
            float hh = (sprite.height() * scale) / 2f;
            Rectanglef quad = U.popRect();
            quad.minX = center.x - wh;
            quad.maxX = center.x + wh;
            quad.minY = center.y - hh;
            quad.maxY = center.y + hh;
            if (quad.isValid()) {
                Vector4f uv = sprite.uvCoordinates(U.popVec4());
                if (active_batch != SPRITE_BATCH) {
                    if (active_batch == TEXT_BATCH) {
                        textBatch.flush();
                        shader_swaps++;
                    } active_batch = SPRITE_BATCH;
                    Framebuffer.drawBuffers(0,1,2,3);
                } if (id == SKIP_ID) {
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(0,1,2);
                    spriteBatch.push(sprite.texture(),null,uv,quad,sprite.rotationRadians(),abgr,0,glow,invisible_id);
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(0,1,2,3);
                } else spriteBatch.push(sprite.texture(),null,uv,quad,sprite.rotationRadians(),abgr,id,glow,invisible_id);
                U.pushVec4();
            } U.pushRect();
        }
    }

    public void drawSprite(Sprite sprite, Rectanglef quad) { drawSprite(sprite, quad, 0xFFFFFFFF); }
    public void drawSprite(Sprite sprite, Rectanglef quad, int abgr) { drawSprite(sprite, quad, abgr, 0); }
    public void drawSprite(Sprite sprite, Rectanglef quad, int abgr, int id) { drawSprite(sprite, quad, abgr, id,true); }
    public void drawSprite(Sprite sprite, Rectanglef quad, int abgr, int id, boolean stretch) { drawSprite(sprite, quad, abgr, id, 0f, stretch); }
    public void drawSprite(Sprite sprite, Rectanglef quad, int abgr, int id, float glow, boolean stretch) { drawSprite(sprite, quad, abgr, id, glow, stretch,true); }
    public void drawSprite(Sprite sprite, Rectanglef quad, int abgr, int id, float glow, boolean stretch, boolean invisible_id) {
        if (rendering &! paused) {
            Rectanglef tmp = U.popRect();
            if (!stretch) {
                float box_width = quad.lengthX();
                float box_height = quad.lengthY();
                float aspect_ratio = sprite.width() / sprite.height();
                float aspect_width = box_width;
                float aspect_height = aspect_width / aspect_ratio;
                if (aspect_height > box_height) {
                    aspect_height = box_height;
                    aspect_width = aspect_height * aspect_ratio; }
                float x0 = (box_width / 2f) - (aspect_width / 2f) + quad.minX;
                float y0 = (box_height / 2f) - (aspect_height / 2f) + quad.minY;
                quad = U.rectSet(tmp,x0,y0,x0+aspect_width,y0+aspect_height);
            } if (quad.isValid()) {
                Vector4f uv = sprite.uvCoordinates(U.popVec4());
                if (active_batch != SPRITE_BATCH) {
                    if (active_batch == TEXT_BATCH) {
                        textBatch.flush();
                        shader_swaps++;
                    } active_batch = SPRITE_BATCH;
                    Framebuffer.drawBuffers(0,1,2,3);
                } if (id == SKIP_ID) {
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(0,1,2);
                    spriteBatch.push(sprite.texture(),null,uv,quad,abgr,0,glow,invisible_id,false);
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(0,1,2,3);
                } else spriteBatch.push(sprite.texture(),null,uv,quad,abgr,id,glow,invisible_id,false);
                U.pushVec4();
            } U.pushRect();
        }
    }

    public void drawElement(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad) { drawElement(diffuse, region, quad,0); }
    public void drawElement(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int id) { drawElement(diffuse, region, quad, Color.WHITE_BITS, id); }
    public void drawElement(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id) { drawElement(diffuse, region, quad, abgr, id, 0); }
    public void drawElement(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawElement(diffuse, region, quad, abgr, id, 0, invisible_id); }
    public void drawElement(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, float glow) { drawElement(diffuse, region, quad, abgr, id, glow, true); }
    public void drawElement(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, normals, region, quad, abgr, 0, glow, invisible_id,false);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, normals, region, quad, abgr, id, glow, invisible_id,false);
        }
    }




    public void drawElement(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad) { drawElement(diffuse, region, quad,0); }
    public void drawElement(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int id) { drawElement(diffuse, region, quad, Color.WHITE_BITS, id); }
    public void drawElement(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id) { drawElement(diffuse, region, quad, abgr, id, 0); }
    public void drawElement(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawElement(diffuse, region, quad, abgr, id, 0, invisible_id); }
    public void drawElement(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id, float glow) { drawElement(diffuse, region, quad, abgr, id, glow, true); }
    public void drawElement(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, normals, region, quad, abgr, 0, glow, invisible_id,false);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, normals, region, quad, abgr, id, glow, invisible_id,false);
        }
    }



    public void drawElement(Texture diffuse, TextureRegion region, Rectanglef quad) { drawElement(diffuse, region, quad,0); }
    public void drawElement(Texture diffuse, TextureRegion region, Rectanglef quad, int id) { drawElement(diffuse, region, quad, Color.WHITE_BITS, id); }
    public void drawElement(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id) { drawElement(diffuse, region, quad, abgr, id, 0); }
    public void drawElement(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawElement(diffuse, region, quad, abgr, id, 0, invisible_id); }
    public void drawElement(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id, float glow) { drawElement(diffuse, region, quad, abgr, id, glow, true); }
    public void drawElement(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, null, region, quad, abgr, 0, glow, invisible_id,false);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, null, region, quad, abgr, id, glow, invisible_id,false);
        }
    }



    public void drawElement(Texture diffuse, Vector4f region, Rectanglef quad) { drawElement(diffuse, region, quad,0); }
    public void drawElement(Texture diffuse, Vector4f region, Rectanglef quad, int id) { drawElement(diffuse, region, quad, Color.WHITE_BITS, id); }
    public void drawElement(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id) { drawElement(diffuse, region, quad, abgr, id, 0); }
    public void drawElement(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawElement(diffuse, region, quad, abgr, id, 0, invisible_id); }
    public void drawElement(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id, float glow) { drawElement(diffuse, region, quad, abgr, id, glow, true); }
    public void drawElement(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, null, region, quad, abgr, 0, glow, invisible_id,false);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, null, region, quad, abgr, id, glow, invisible_id,false);
        }
    }
    public void drawElement(Texture diffuse, Rectanglef quad) { drawElement(diffuse, quad,0); }
    public void drawElement(Texture diffuse, Rectanglef quad, int id) { drawElement(diffuse, quad, Color.WHITE_BITS, id); }
    public void drawElement(Texture diffuse, Rectanglef quad, int abgr, int id) { drawElement(diffuse, quad, abgr, id, 0); }
    public void drawElement(Texture diffuse, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawElement(diffuse, quad, abgr, id, 0, invisible_id); }
    public void drawElement(Texture diffuse, Rectanglef quad, int abgr, int id, float glow) { drawElement(diffuse,  quad, abgr, id, glow, true); }
    public void drawElement(Texture diffuse, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            Vector4f region = U.popSetVec4(0,0,1,1);
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, null, region, quad, abgr, 0, glow, invisible_id,false);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, null, region, quad, abgr, id, glow, invisible_id,false);
            U.pushVec4();
        }
    }


    public void drawElement(Rectanglef quad, int abgr) { drawElement(quad, abgr, 0); }
    public void drawElement(Rectanglef quad, int abgr, int id) { drawElement(quad, abgr, id,0f); }
    public void drawElement(Rectanglef quad, int abgr, float glow) { drawElement(quad, abgr, 0, glow); }
    public void drawElement(Rectanglef quad, int abgr, int id, float glow) { drawElement(quad, abgr, id, glow,true); }
    public void drawElement(Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(quad, abgr, 0, glow, invisible_id,false);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(quad, abgr, id, glow, invisible_id,false);
        }
    }


    public void drawRotated(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, float rotation) { drawRotated(diffuse, normals, region, quad, rotation, 0); }
    public void drawRotated(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, float rotation, int id) { drawRotated(diffuse, normals, region, quad, rotation, 0xFFFFFFFF, id); }
    public void drawRotated(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, float rotation, int abgr, int id) { drawRotated(diffuse, normals, region, quad, rotation, abgr, id, 0); }
    public void drawRotated(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, float rotation, int abgr, int id, float glow) { drawRotated(diffuse, normals, region, quad, rotation, abgr, id, glow, true); }
    public void drawRotated(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, float rotation, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, normals, region, quad, rotation, abgr, 0, glow, invisible_id);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, normals, region, quad, rotation, abgr, id, glow, invisible_id);
        }
    }

    public void drawRotated(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, float rotation) { drawRotated(diffuse, normals, region, quad, rotation, 0); }
    public void drawRotated(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, float rotation, int id) { drawRotated(diffuse, normals, region, quad, rotation, 0xFFFFFFFF, id); }
    public void drawRotated(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, float rotation, int abgr, int id) { drawRotated(diffuse, normals, region, quad, rotation, abgr, id, 0); }
    public void drawRotated(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, float rotation, int abgr, int id, float glow) { drawRotated(diffuse, normals, region, quad, rotation, abgr, id, glow, true); }
    public void drawRotated(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, float rotation, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, normals, region, quad, rotation, abgr, 0, glow, invisible_id);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, normals, region, quad, rotation, abgr, id, glow, invisible_id);
        }
    }

    public void drawRotated(Texture diffuse, TextureRegion region, Rectanglef quad, float rotation) { drawRotated(diffuse, region, quad, rotation, 0); }
    public void drawRotated(Texture diffuse, TextureRegion region, Rectanglef quad, float rotation, int id) { drawRotated(diffuse, region, quad, rotation, 0xFFFFFFFF, id); }
    public void drawRotated(Texture diffuse, TextureRegion region, Rectanglef quad, float rotation, int abgr, int id) { drawRotated(diffuse, region, quad, rotation, abgr, id, 0); }
    public void drawRotated(Texture diffuse, TextureRegion region, Rectanglef quad, float rotation, int abgr, int id, float glow) { drawRotated(diffuse, region, quad, rotation, abgr, id, glow, true); }
    public void drawRotated(Texture diffuse, TextureRegion region, Rectanglef quad, float rotation, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, null, region, quad, rotation, abgr, 0, glow, invisible_id);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, null, region, quad, rotation, abgr, id, glow, invisible_id);
        }
    }

    public void drawRotated(Texture diffuse, Vector4f region, Rectanglef quad, float rotation) { drawRotated(diffuse, region, quad, rotation, 0); }
    public void drawRotated(Texture diffuse, Vector4f region, Rectanglef quad, float rotation, int id) { drawRotated(diffuse, region, quad, rotation, 0xFFFFFFFF, id); }
    public void drawRotated(Texture diffuse, Vector4f region, Rectanglef quad, float rotation, int abgr, int id) { drawRotated(diffuse, region, quad, rotation, abgr, id, 0); }
    public void drawRotated(Texture diffuse, Vector4f region, Rectanglef quad, float rotation, int abgr, int id, float glow) { drawRotated(diffuse, region, quad, rotation, abgr, id, glow, true); }
    public void drawRotated(Texture diffuse, Vector4f region, Rectanglef quad, float rotation, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, null, region, quad, rotation, abgr, 0, glow, invisible_id);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, null, region, quad, rotation, abgr, id, glow, invisible_id);
        }
    }


    public void drawRotated(Rectanglef quad, float rotation, int abgr) { drawRotated(quad, rotation,abgr, 0); }
    public void drawRotated(Rectanglef quad, float rotation, int abgr, int id) { drawRotated(quad, rotation,abgr, id,0f); }
    public void drawRotated(Rectanglef quad, float rotation, int abgr, float glow) { drawRotated(quad, rotation,abgr, 0, glow); }
    public void drawRotated(Rectanglef quad, float rotation, int abgr, int id, float glow) { drawRotated(quad, rotation, abgr, id, glow,true); }
    public void drawRotated(Rectanglef quad, float rotation, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(quad, rotation, abgr, 0, glow, invisible_id);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(quad, rotation, abgr, id, glow, invisible_id);
        }
    }








    public void drawRound(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad) { drawRound(diffuse, region, quad,0); }
    public void drawRound(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int id) { drawRound(diffuse, region, quad, Color.WHITE_BITS, id); }
    public void drawRound(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id) { drawElement(diffuse, region, quad, abgr, id, 0); }
    public void drawRound(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawRound(diffuse, region, quad, abgr, id, 0, invisible_id); }
    public void drawRound(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, float glow) { drawRound(diffuse, region, quad, abgr, id, glow, true); }
    public void drawRound(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, normals, region, quad, abgr, 0, glow, invisible_id,true);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, normals, region, quad, abgr, id, glow, invisible_id,true);
        }
    }

    public void drawRound(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad) { drawRound(diffuse, region, quad,0); }
    public void drawRound(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int id) { drawRound(diffuse, region, quad, Color.WHITE_BITS, id); }
    public void drawRound(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id) { drawRound(diffuse, region, quad, abgr, id, 0); }
    public void drawRound(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawRound(diffuse, region, quad, abgr, id, 0, invisible_id); }
    public void drawRound(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id, float glow) { drawRound(diffuse, region, quad, abgr, id, glow, true); }
    public void drawRound(Texture diffuse, Texture normals, Vector4f region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, normals, region, quad, abgr, 0, glow, invisible_id,true);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, normals, region, quad, abgr, id, glow, invisible_id,true);
        }
    }

    public void drawRound(Texture diffuse, TextureRegion region, Rectanglef quad) { drawRound(diffuse, region, quad,0); }
    public void drawRound(Texture diffuse, TextureRegion region, Rectanglef quad, int id) { drawRound(diffuse, region, quad, Color.WHITE_BITS, id); }
    public void drawRound(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id) { drawRound(diffuse, region, quad, abgr, id, 0); }
    public void drawRound(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawRound(diffuse, region, quad, abgr, id, 0, invisible_id); }
    public void drawRound(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id, float glow) { drawRound(diffuse, region, quad, abgr, id, glow, true); }
    public void drawRound(Texture diffuse, TextureRegion region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, null, region, quad, abgr, 0, glow, invisible_id,true);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, null, region, quad, abgr, id, glow, invisible_id,true);
        }
    }

    public void drawRound(Texture diffuse, Vector4f region, Rectanglef quad) { drawRound(diffuse, region, quad,0); }
    public void drawRound(Texture diffuse, Vector4f region, Rectanglef quad, int id) { drawRound(diffuse, region, quad, Color.WHITE_BITS, id); }
    public void drawRound(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id) { drawRound(diffuse, region, quad, abgr, id, 0); }
    public void drawRound(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id, boolean invisible_id) { drawRound(diffuse, region, quad, abgr, id, 0, invisible_id); }
    public void drawRound(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id, float glow) { drawRound(diffuse, region, quad, abgr, id, glow, true); }
    public void drawRound(Texture diffuse, Vector4f region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(diffuse, null, region, quad, abgr, 0, glow, invisible_id,true);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(diffuse, null, region, quad, abgr, id, glow, invisible_id,true);
        }
    }


    public void drawRound(Rectanglef quad, int abgr) { drawRound(quad, abgr, 0); }
    public void drawRound(Rectanglef quad, int abgr, int id) { drawRound(quad, abgr, id,0f); }
    public void drawRound(Rectanglef quad, int abgr, float glow) { drawRound(quad, abgr, 0, glow); }
    public void drawRound(Rectanglef quad, int abgr, int id, float glow) { drawRound(quad, abgr, id, glow,true); }
    public void drawRound(Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.push(quad, abgr, 0, glow, invisible_id,true);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.push(quad, abgr, id, glow, invisible_id,true);
        }
    }

    public void drawScrollBar(Rectanglef scroll_bar, int abgr, int id) { drawScrollBar(scroll_bar, abgr, id,0.0f); }
    public void drawScrollBar(Rectanglef scroll_bar, int abgr, int id, float glow) { drawScrollBar(scroll_bar, abgr, id, glow, true); }
    public void drawScrollBar(Rectanglef scroll_bar, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && scroll_bar.isValid()) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
            } Rectanglef rect = U.popRect().set(scroll_bar);
            float sb_length_x = scroll_bar.lengthX();
            float sb_length_y = scroll_bar.lengthY();
            if (sb_length_x < sb_length_y) {
                float radius = sb_length_x / 2f;
                rect.minY = (rect.maxY - sb_length_x);
                drawRound(rect,abgr,id,glow,true);
                rect.minY = scroll_bar.minY;
                rect.maxY = rect.minY + sb_length_x;
                drawRound(rect,abgr,id,glow,true);
                rect.maxY = scroll_bar.maxY - radius;
                rect.minY += radius;
                drawElement(rect,abgr,id,glow,true);
            } else if (sb_length_x > sb_length_y) {
                float radius = sb_length_y / 2f;
                rect.maxX -= radius;
                rect.minX += radius;
                drawElement(rect,abgr,id,glow,true);
                rect.maxX += radius;
                rect.minX = rect.maxX - sb_length_y;
                drawRound(rect,abgr,id,glow,true);
                rect.minX = scroll_bar.minX;
                rect.maxX = rect.minX + sb_length_y;
                drawRound(rect,abgr,id,glow,true);
            } else { drawRound(rect,abgr,id,glow,true);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } U.pushRect();
        }
    }

    public void drawBorders(Rectanglef quad, float thickness, int abgr, float glow) { drawBorders(quad,thickness,abgr,0,glow,true); }
    public void drawBorders(Rectanglef quad, float thickness, int abgr, int id) { drawBorders(quad,thickness,abgr,id,0f,true); }
    public void drawBorders(Rectanglef quad, float thickness, int abgr, int id, boolean invisible_id) { drawBorders(quad, thickness, abgr, id,0,invisible_id); }
    public void drawBorders(Rectanglef quad, float thickness, int abgr, int id, float glow, boolean invisible_id) {
        Rectanglef outline = U.popSetRect(quad);
        outline.minY = quad.maxY - thickness;
        drawElement(outline, abgr, id, glow);
        outline.minY = quad.minY;
        outline.maxY = quad.minY + thickness;
        drawElement(outline, abgr, id, glow);
        outline.maxY = quad.maxY;
        outline.maxX = quad.minX + thickness;
        drawElement(outline, abgr, id, glow);
        outline.maxX = quad.maxX;
        outline.minX = quad.maxX - thickness;
        drawElement(outline,abgr,id,glow);
        U.pushRect();
    }



    public void drawGadgetButton(Rectanglef quad, float padding, int abgr, boolean pressed) { drawGadgetButton(quad, padding, abgr, 0, pressed); }
    public void drawGadgetButton(Rectanglef quad, float padding, int abgr, int id, boolean pressed) { drawGadgetButton(quad, padding, abgr, id, 0, pressed); }
    public void drawGadgetButton(Rectanglef quad, float padding, int abgr, int id, float glow, boolean pressed) { drawGadgetButton(quad, padding, abgr, id, glow, pressed,true); }
    public void drawGadgetButton(Rectanglef quad, float padding, int abgr, int id, float glow, boolean pressed, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            float box_width = quad.lengthX();
            float box_height = quad.lengthY();
            float padding_sum = padding * 2;
            if (box_width >= padding_sum && box_height >= padding_sum) {
                int iID = id;
                if (active_batch != SPRITE_BATCH) {
                    if (active_batch == TEXT_BATCH) {
                        textBatch.flush();
                        shader_swaps++;
                    } active_batch = SPRITE_BATCH;
                    Framebuffer.drawBuffers(0,1,2,3);
                } if (id == SKIP_ID) { iID = 0;
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(0,1,2);
                } Rectanglef rect = U.popRect();
                rect.maxY = quad.maxY;
                rect.minY = rect.maxY - padding;
                rect.minX = quad.minX;
                rect.maxX = rect.minX + padding;
                Texture texture = GUI.gadgets.atlas.texture(0);
                TextureRegion button_c; TextureRegion button_t;
                TextureRegion button_l; TextureRegion button_r;
                TextureRegion button_b; TextureRegion button_tl;
                TextureRegion button_tr;TextureRegion button_bl;
                TextureRegion button_br;
                if (pressed) {
                    button_c = GUI.gadgets.button_pressed_center;
                    button_t = GUI.gadgets.button_pressed_top;
                    button_l = GUI.gadgets.button_pressed_left;
                    button_r = GUI.gadgets.button_pressed_right;
                    button_b = GUI.gadgets.button_pressed_bottom;
                    button_tl = GUI.gadgets.button_pressed_top_left;
                    button_tr = GUI.gadgets.button_pressed_top_right;
                    button_bl = GUI.gadgets.button_pressed_bottom_left;
                    button_br = GUI.gadgets.button_pressed_bottom_right;
                } else {
                    button_c = GUI.gadgets.button_center;
                    button_t = GUI.gadgets.button_top;
                    button_l = GUI.gadgets.button_left;
                    button_r = GUI.gadgets.button_right;
                    button_b = GUI.gadgets.button_bottom;
                    button_tl = GUI.gadgets.button_top_left;
                    button_tr = GUI.gadgets.button_top_right;
                    button_bl = GUI.gadgets.button_bottom_left;
                    button_br = GUI.gadgets.button_bottom_right;
                }
                spriteBatch.push(texture, null, button_tl, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(box_width - padding,0f);
                spriteBatch.push(texture, null, button_tr, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(0,- (box_height - padding));
                spriteBatch.push(texture, null, button_br, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(- (box_width - padding),0f);
                spriteBatch.push(texture, null, button_bl, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(0,padding);
                rect.maxY = rect.minY + (box_height - padding_sum);
                spriteBatch.push(texture, null, button_l, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(box_width - padding,0);
                spriteBatch.push(texture, null, button_r, rect, abgr, iID, glow, invisible_id,false);
                rect.maxY += padding;
                rect.minY = rect.maxY - padding;
                rect.minX = quad.minX + padding;
                rect.maxX = quad.maxX - padding;
                spriteBatch.push(texture, null, button_t, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(0,-(box_height - padding));
                spriteBatch.push(texture, null, button_b, rect, abgr, iID, glow, invisible_id,false);
                rect.set(quad);
                rect.maxY -= padding;
                rect.minY += padding;
                rect.minX += padding;
                rect.maxX -= padding;
                spriteBatch.push(texture, null, button_c, rect, abgr, iID, glow, invisible_id,false);
                if (id == SKIP_ID) {
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(0,1,2,3);
                } U.pushRect();
            }
        }
    }

    public void drawGadgetBorders(Rectanglef quad, float thickness, int abgr) { drawGadgetBorders(quad, thickness, abgr, 0); }
    public void drawGadgetBorders(Rectanglef quad, float thickness, int abgr, float glow) { drawGadgetBorders(quad, thickness, abgr, 0,glow); }
    public void drawGadgetBorders(Rectanglef quad, float thickness, int abgr, int id) { drawGadgetBorders(quad, thickness, abgr, id,0); }
    public void drawGadgetBorders(Rectanglef quad, float thickness, int abgr, int id, float glow) { drawGadgetBorders(quad, thickness, abgr, id, glow, true); }
    public void drawGadgetBorders(Rectanglef quad, float thickness, int abgr, int id, float glow, boolean invisible_id) {
        if (rendering && !paused && quad.isValid()) {
            float box_width = quad.lengthX();
            float box_height = quad.lengthY();
            float padding_sum = thickness * 2;
            if (box_width >= padding_sum && box_height >= padding_sum) {
                int iID = id;
                if (active_batch != SPRITE_BATCH) {
                    if (active_batch == TEXT_BATCH) {
                        textBatch.flush();
                        shader_swaps++;
                    } active_batch = SPRITE_BATCH;
                    Framebuffer.drawBuffers(0,1,2,3);
                } if (id == SKIP_ID) { iID = 0;
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(0,1,2);
                } Rectanglef rect = U.popRect();
                rect.maxY = quad.maxY;
                rect.minY = rect.maxY - thickness;
                rect.minX = quad.minX;
                rect.maxX = rect.minX + thickness;
                Texture texture = GUI.gadgets.atlas.texture(0);
                TextureRegion border_t = GUI.gadgets.window_border_top;
                TextureRegion border_l = GUI.gadgets.window_border_left;
                TextureRegion border_r = GUI.gadgets.window_border_right;
                TextureRegion border_b = GUI.gadgets.window_border_bottom;
                TextureRegion border_tl = GUI.gadgets.window_border_top_left;
                TextureRegion border_tr = GUI.gadgets.window_border_top_right;
                TextureRegion border_bl = GUI.gadgets.window_border_bottom_left;
                TextureRegion border_br = GUI.gadgets.window_border_bottom_right;
                spriteBatch.push(texture, null, border_tl, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(box_width - thickness,0f);
                spriteBatch.push(texture, null, border_tr, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(0,- (box_height - thickness));
                spriteBatch.push(texture, null, border_br, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(- (box_width - thickness),0f);
                spriteBatch.push(texture, null, border_bl, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(0,thickness);
                rect.maxY = rect.minY + (box_height - padding_sum);
                spriteBatch.push(texture, null, border_l, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(box_width - thickness,0);
                spriteBatch.push(texture, null, border_r, rect, abgr, iID, glow, invisible_id,false);
                rect.maxY += thickness;
                rect.minY = rect.maxY - thickness;
                rect.minX = quad.minX + thickness;
                rect.maxX = quad.maxX - thickness;
                spriteBatch.push(texture, null, border_t, rect, abgr, iID, glow, invisible_id,false);
                rect.translate(0,-(box_height - thickness));
                spriteBatch.push(texture, null, border_b, rect, abgr, iID, glow, invisible_id,false);
                if (id == SKIP_ID) {
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(0,1,2,3);
                } U.pushRect();
            }
        }
    }






    private void initializeBloomBuffers(int width, int height) throws Exception {
        if (bloomBuffers != null) Disposable.dispose(bloomBuffers);
        bloomBuffers = new Framebuffer[2];
        for (int i = 0; i < 2; i++) {
            Framebuffer framebuffer = new Framebuffer(width, height);
            Framebuffer.bind(framebuffer);
            Framebuffer.setClearColor(0,0,0,0);
            Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
            Texture bloom_texture = Texture.generate2D(width, height);
            bloom_texture.bindToActiveSlot();
            bloom_texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
            bloom_texture.filterLinear();
            bloom_texture.clampToEdge();
            Framebuffer.attachColor(bloom_texture,0,true);
            Framebuffer.drawBuffer(0);
            Framebuffer.readBuffer(0);
            Framebuffer.checkStatus();
            bloomBuffers[i] = framebuffer;
        }
    }


    private void initializeFramebuffer(int width, int height) throws Exception {
        if (framebuffer != null) framebuffer.dispose();
        framebuffer = new Framebuffer(width,height);
        Framebuffer.bind(framebuffer);
        Framebuffer.setClearColor(0,0,0,0);
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        Texture diffuse_texture = Texture.generate2D(width, height);
        diffuse_texture.bindToActiveSlot();
        diffuse_texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
        diffuse_texture.filterLinear();
        diffuse_texture.clampToEdge();
        Framebuffer.attachColor(diffuse_texture,FRAMEBUFFER_SLOT_DIFFUSE,true);
        Texture normals_texture = Texture.generate2D(width, height);
        normals_texture.bindToActiveSlot();
        normals_texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        normals_texture.filterLinear();
        normals_texture.clampToEdge();
        Framebuffer.attachColor(normals_texture,FRAMEBUFFER_SLOT_NORMALS,true);
        Texture emissive_texture = Texture.generate2D(width, height);
        emissive_texture.bindToActiveSlot();
        emissive_texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED);
        emissive_texture.filterLinear();
        emissive_texture.clampToEdge();
        Framebuffer.attachColor(emissive_texture,FRAMEBUFFER_SLOT_EMISSIVE,true);
        Texture uid_texture = Texture.generate2D(width,height);
        uid_texture.bindToActiveSlot();
        uid_texture.filterNearest();
        uid_texture.clampToEdge();
        uid_texture.allocate(TextureFormat.R32_UNSIGNED_INTEGER);
        Framebuffer.attachColor(uid_texture,FRAMEBUFFER_SLOT_PIXEL_ID,true);
        Framebuffer.drawBuffers(0,1,2,3);
        Framebuffer.readBuffer(FRAMEBUFFER_SLOT_PIXEL_ID);
        Framebuffer.checkStatus();
    }


    private void flush() {
        if (rendering) {
            if (active_batch == TEXT_BATCH) {
                textBatch.flush();
            } else if (active_batch == SPRITE_BATCH) {
                spriteBatch.flush();
            }
        }
    }












    private static final class ScissorStack {
        private final RectanglePool scissor_pool = new RectanglePool();
        private final Stack<Rectanglei> scissor_stack = new Stack<>(8);
        private final RendererGUI renderer;
        ScissorStack(RendererGUI renderer) { this.renderer = renderer; }
        boolean push(Rectanglef quad) {
            return push(quad.minX,quad.minY,quad.maxX,quad.maxY);
        }
        boolean push(float x1, float y1, float x2, float y2) {
            Rectanglei scissor = scissor_pool.obtain();
            scissor.minX = floor(x1);
            scissor.minY = floor(y1);
            scissor.maxX = ceil(x2);
            scissor.maxY = ceil(y2);
            Rectanglei previous = scissor_stack.peak();
            if (previous == null) {
                Resolution resolution = GUI.resolution();
                Rectanglei res_scissor = scissor_pool.obtain();
                res_scissor.setMin(0,0);
                res_scissor.setMax(resolution.width(),resolution.height());
                scissor.intersection(res_scissor);
                scissor_pool.free(res_scissor);
            } else scissor.intersection(previous);
            if (scissor.isValid()) {
                int w = scissor.lengthX();
                int h = scissor.lengthY();
                renderer.flush();
                glEnable(GL_SCISSOR_TEST);
                glScissor(scissor.minX,scissor.minY,w,h);
                scissor_stack.push(scissor);
                return true;
            } else scissor_pool.free(scissor);
            return false;
        }

        void pop() {
            if (!scissor_stack.isEmpty()) { renderer.flush();
                Rectanglei current = scissor_stack.pop();
                Rectanglei scissor = scissor_stack.peak();
                if (scissor == null) {
                    glDisable(GL_SCISSOR_TEST);
                } else { int w = scissor.lengthX();
                    int h = scissor.lengthY();
                    glScissor(scissor.minX,scissor.minY,w,h);
                } scissor_pool.free(current);
            }
        }

        void reset() {
            while (!scissor_stack.isEmpty()) {
                scissor_pool.free(scissor_stack.pop());
            } glDisable(GL_SCISSOR_TEST);
        }

        void pause() {
            if (!scissor_stack.isEmpty()) {
                glDisable(GL_SCISSOR_TEST);
            }
        }

        void resume() {
            Rectanglei scissor = scissor_stack.peak();
            if (scissor != null) {
                int w = scissor.lengthX();
                int h = scissor.lengthY();
                glEnable(GL_SCISSOR_TEST);
                glScissor(scissor.minX,scissor.minY,w,h);
            }
        }
    }

    private static final class DelayedDrawCall {


        //public void drawElement(Texture diffuse, Texture normals, TextureRegion region, Rectanglef quad, int abgr, int id, float glow, boolean invisible_id) {

    }

    private static final class RectanglePool extends Pool<Rectanglei> {
        public RectanglePool() { super(4, 16); }
        protected Rectanglei newObject() { return new Rectanglei(); }
    }



}
