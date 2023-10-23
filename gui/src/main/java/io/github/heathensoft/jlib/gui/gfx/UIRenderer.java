package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.text.Paragraph;
import io.github.heathensoft.jlib.gui.text.Text;
import io.github.heathensoft.jlib.gui.text.Word;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Repository;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static java.lang.Math.max;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL15.GL_STREAM_READ;
import static org.lwjgl.opengl.GL15.glUnmapBuffer;
import static org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER;
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
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.glBlendEquationi;
import static org.lwjgl.opengl.GL40.glBlendFunci;

/**
 *
 * @author Frederik Dahl
 * 14/10/2023
 */


public class UIRenderer implements Disposable {

    public static final int SKIP_ID = -1;
    public static final String DEFAULT_FONT_NAME = "Balla";
    public static final String DEFAULT_FONT_PATH = "res/jlib/lwjgl/font/";
    public static final int FONT_UNIFORM_BUFFER_BINDING_POINT = 0;

    protected static final int TEXT_BATCH_CAPACITY = 2048;
    protected static final int SPRITE_BATCH_CAPACITY = 512;
    protected static final int FRAMEBUFFER_SLOT_DIFFUSE = 0;
    protected static final int FRAMEBUFFER_SLOT_NORMALS = 1;
    protected static final int FRAMEBUFFER_SLOT_EMISSIVE = 2;
    protected static final int FRAMEBUFFER_SLOT_PIXEL_ID = 3;

    // RENDER STATE
    protected static final int NULL_BATCH = 0;
    protected static final int TEXT_BATCH = 1;
    protected static final int SPRITE_BATCH = 2;

    protected long render_time_total;
    protected long render_time_max;
    protected long render_time_avg;
    protected long render_time;
    protected long shader_swaps_total;
    protected long shader_swaps_avg;
    protected int shader_swaps_max;
    protected int shader_swaps;
    protected long draw_calls_total;
    protected long draw_calls_avg;
    protected int draw_calls_max;
    protected int draw_calls;
    protected int active_batch;
    protected int frame_count;
    protected boolean rendering;

    // PIXEL UID BUFFER
    protected IntBuffer syncBuffer;
    protected ByteBuffer readPixelBuffer;
    protected BufferObject pixelPackBuffer;
    protected long syncObject;
    protected int syncStatus;
    protected int pixelID;

    // 
    protected Fonts fonts;
    protected SpriteBatch spriteBatch;
    protected TextBatch textBatch;
    protected Framebuffer framebuffer;


    public UIRenderer(int width, int height) throws Exception {
        initializeFramebuffer(width, height);
        initializePixelBuffer(width,height);
        initializeFontCollection();
        initializeRenderBatches(width,height);
    }

    public void begin(Vector2f mouse) {
        if (!rendering) {
            render_time = System.currentTimeMillis();
            shader_swaps = 0;
            draw_calls = 0;
            rendering = true;
            int mouse_screen_x = round(mouse.x * framebuffer.width());
            int mouse_screen_y = round(mouse.y * framebuffer.height());
            Framebuffer.bind(framebuffer);
            Framebuffer.viewport();
            pixelReadOperation(mouse_screen_x,mouse_screen_y);
            Framebuffer.drawBuffers(0,1,2,3);
            Framebuffer.clear();
            // Blending diffuse and normals
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            // Blending emissive
            glBlendEquationi(2,GL_MAX);
            glBlendFunci(2,GL_ONE,GL_ONE);
        }
    }

    public void end() {
        if (rendering) {
            frame_count++;
            spriteBatch.flush();
            textBatch.flush();
            draw_calls += spriteBatch.resetDrawCalls();
            draw_calls += textBatch.resetDrawCalls();
            draw_calls_total += draw_calls;
            draw_calls_max = max(draw_calls, draw_calls_max);
            draw_calls_avg = draw_calls_total / frame_count;
            shader_swaps_total += shader_swaps;
            shader_swaps_max = max(shader_swaps, shader_swaps_max);
            shader_swaps_avg = shader_swaps_total / frame_count;
            render_time = System.currentTimeMillis() - render_time;
            render_time_total += render_time;
            render_time_max = max(render_time,render_time_max);
            render_time_avg = render_time_total / frame_count;
            active_batch = NULL_BATCH;
            rendering = false;
        }
    }

    public void drawText(Text text, Rectanglef quad, int font, int size) {
        if (rendering && size > 0 && !text.isBlank() && quad.isValid()) {
            if (active_batch != TEXT_BATCH) {
                if (active_batch == SPRITE_BATCH) {
                    spriteBatch.flush();
                    shader_swaps++;
                } active_batch = TEXT_BATCH;
                Framebuffer.drawBuffers(0,1,2);
            } fonts.bindFontMetrics(font);
            textBatch.flush();
            scissor(quad);
            text.draw(textBatch,quad,font,size);
            textBatch.flush();
            glDisable(GL_SCISSOR_TEST);
        }
    }

    public void drawLine(Paragraph line, float x, float y, float width, int font, int size, boolean centered) {
        if (rendering && size > 0 && !line.isBlank() && width > 0) {
            if (active_batch != TEXT_BATCH) {
                if (active_batch == SPRITE_BATCH) {
                    spriteBatch.flush();
                    shader_swaps++;
                } active_batch = TEXT_BATCH;
                Framebuffer.drawBuffers(0,1,2);
            } fonts.bindFontMetrics(font);
            line.draw(textBatch,x,y,width,size,centered);
        }
    }

    public void drawElement(UISprite sprite, Rectanglef quad) { drawElement(sprite, quad,0); }
    public void drawElement(UISprite sprite, Rectanglef quad, int id) { drawElement(sprite, quad, id,0f); }
    public void drawElement(UISprite sprite, Rectanglef quad, Color32 tint) { drawElement(sprite, quad, tint, 0); }
    public void drawElement(UISprite sprite, Rectanglef quad, int id, float glow) { drawElement(sprite, quad, Color32.WHITE, id, glow); }
    public void drawElement(UISprite sprite, Rectanglef quad, Color32 tint, int id) { drawElement(sprite, quad, tint, id, 0); }
    public void drawElement(UISprite sprite, Rectanglef quad, Color32 tint, float glow) { drawElement(sprite, quad, tint, 0, glow); }
    public void drawElement(UISprite sprite, Rectanglef quad, Color32 tint, int id, float glow) { drawElement(sprite, quad, tint, id, glow,false); }
    public void drawElement(UISprite sprite, Rectanglef quad, Color32 tint, int id, float glow, boolean invisible_id) {
        if (rendering) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.draw(sprite, quad, tint, 0, glow, invisible_id);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.draw(sprite, quad, tint, id, glow, invisible_id);
        }
    }


    public void drawElement(Rectanglef quad, Color32 color) { drawElement(quad, color, 0); }
    public void drawElement(Rectanglef quad, Color32 color, int id) { drawElement(quad, color, id,0f); }
    public void drawElement(Rectanglef quad, Color32 color, float glow) { drawElement(quad, color, 0, glow); }
    public void drawElement(Rectanglef quad, Color32 color, int id, float glow) { drawElement(quad, color, id, glow,false); }
    public void drawElement(Rectanglef quad, Color32 color, int id, float glow, boolean invisible_id) {
        if (rendering) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } if (id == SKIP_ID) {
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2);
                spriteBatch.draw(quad, color, 0, glow, invisible_id);
                spriteBatch.flush();
                Framebuffer.drawBuffers(0,1,2,3);
            } else spriteBatch.draw(quad, color, id, glow, invisible_id);
        }
    }

    public void uploadFont(Font font, int slot) throws Exception { fonts.uploadFont(font,slot); }
    public void uploadFont(ByteBuffer png, String metrics, int slot) throws Exception { fonts.uploadFont(png, metrics, slot); }
    public void uploadTextColors(IntBuffer colorBuffer) { fonts.uploadColors(colorBuffer); }

    public void updateResolution(int width, int height) throws Exception {
        if (rendering) throw new IllegalStateException("Illegal attempt to update resolution while rendering");
        if (framebuffer.width() != width || framebuffer.height() != height) {
            initializePixelBuffer(width, height);
            initializeFramebuffer(width, height);
            textBatch.setResolutionUniform(width, height);
            spriteBatch.setResolutionUniform(width, height);
        }
    }

    public void dispose() {
        if (syncBuffer != null) MemoryUtil.memFree(syncBuffer);
        if (readPixelBuffer != null) MemoryUtil.memFree(readPixelBuffer);
        Disposable.dispose(pixelPackBuffer);
        Disposable.dispose(spriteBatch,textBatch);
        Disposable.dispose(framebuffer,fonts);
    }

    public Fonts fonts() { return fonts; }
    public Framebuffer framebuffer() { return framebuffer; }
    public Texture framebufferDiffuseTexture() { return framebuffer.texture(FRAMEBUFFER_SLOT_DIFFUSE); }
    public Texture framebufferNormalsTexture() { return framebuffer.texture(FRAMEBUFFER_SLOT_NORMALS); }
    public Texture framebufferEmissiveTexture() { return framebuffer.texture(FRAMEBUFFER_SLOT_EMISSIVE); }

    public int pixelID() { return pixelID; }
    public int drawCallCount() { return draw_calls; }
    public int drawCallCountMax() { return draw_calls_max; }
    public long drawCallCountAverage() { return draw_calls_avg; }
    public int shaderSwapCount() { return shader_swaps; }
    public int shaderSwapCountMax() { return shader_swaps_max; }
    public long shaderSwapCountAverage() { return shader_swaps_avg; }
    public long renderTimeMs() { return render_time; }
    public long renderTimeMsMax() { return render_time_max; }
    public long renderTimeMsAverage() { return render_time_avg; }

    protected void initializeFontCollection() throws Exception {
        if (fonts == null) {
            Repository font_repo = Repository.loadFromResources(DEFAULT_FONT_PATH + DEFAULT_FONT_NAME + ".repo");
            Font font = font_repo.getFont(DEFAULT_FONT_NAME);
            fonts = new Fonts(FONT_UNIFORM_BUFFER_BINDING_POINT);
            fonts.uploadFont(font,0);
        }
    }

    protected void initializeRenderBatches(int width, int height) throws Exception {
        if (textBatch == null) textBatch = new TextBatch(fonts,TEXT_BATCH_CAPACITY,width,height);
        if (spriteBatch == null) spriteBatch = new SpriteBatch(SPRITE_BATCH_CAPACITY,width,height);
    }

    protected void initializeFramebuffer(int width, int height) throws Exception {
        if (framebuffer != null) framebuffer.dispose();
        framebuffer = new Framebuffer(width,height);
        Framebuffer.bind(framebuffer);
        Framebuffer.setClearColor(0,0,0,0);
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        Texture diffuse_texture = Texture.generate2D(width, height);
        diffuse_texture.bindToActiveSlot();
        diffuse_texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
        diffuse_texture.nearest();
        diffuse_texture.clampToEdge();
        Framebuffer.attachColor(diffuse_texture,FRAMEBUFFER_SLOT_DIFFUSE,true);
        Texture normals_texture = Texture.generate2D(width, height);
        normals_texture.bindToActiveSlot();
        normals_texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        normals_texture.nearest();
        normals_texture.clampToEdge();
        Framebuffer.attachColor(normals_texture,FRAMEBUFFER_SLOT_NORMALS,true);
        Texture emissive_texture = Texture.generate2D(width, height);
        emissive_texture.bindToActiveSlot();
        emissive_texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED);
        emissive_texture.nearest();
        emissive_texture.clampToEdge();
        Framebuffer.attachColor(emissive_texture,FRAMEBUFFER_SLOT_EMISSIVE,true);
        Texture uid_texture = Texture.generate2D(width,height);
        uid_texture.bindToActiveSlot();
        uid_texture.nearest();
        uid_texture.clampToEdge();
        uid_texture.allocate(TextureFormat.R32_UNSIGNED_INTEGER);
        Framebuffer.attachColor(uid_texture,FRAMEBUFFER_SLOT_PIXEL_ID,true);
        Framebuffer.drawBuffers(0,1,2,3);
        Framebuffer.readBuffer(FRAMEBUFFER_SLOT_PIXEL_ID);
        Framebuffer.checkStatus();
    }

    protected void initializePixelBuffer(int width, int height) {
        if (readPixelBuffer != null) MemoryUtil.memFree(readPixelBuffer);
        if (syncBuffer != null) MemoryUtil.memFree(syncBuffer);
        syncBuffer = MemoryUtil.memAllocInt(1);
        readPixelBuffer = MemoryUtil.memAlloc(Integer.BYTES);
        if (pixelPackBuffer != null) pixelPackBuffer.dispose();
        pixelPackBuffer = new BufferObject(GL_PIXEL_PACK_BUFFER, GL_STREAM_READ);
        pixelPackBuffer.bind().bufferData((long) width * height * Integer.BYTES);
        BufferObject.bindZERO(GL_PIXEL_PACK_BUFFER); // very important
        syncStatus = GL_UNSIGNALED;
        syncObject = 0L;
        pixelID = 0;
    }

    protected void pixelReadOperation(int x, int y) {
        if (syncStatus == GL_SIGNALED) {
            syncStatus = GL_UNSIGNALED;
            glDeleteSync(syncObject);
            syncObject = 0L;
            pixelPackBuffer.bind();
            ByteBuffer pixel = glMapBufferRange(GL_PIXEL_PACK_BUFFER,0,Integer.BYTES,GL_MAP_READ_BIT, readPixelBuffer);
            if (pixel != null) {
                pixelID = (pixel.get(0)) | (pixel.get(1) << 8) | (pixel.get(2) << 16) | (pixel.get(3) << 24);
                glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
            } Framebuffer.bindRead(framebuffer);
            Framebuffer.readBuffer(FRAMEBUFFER_SLOT_PIXEL_ID); // bind uid buffer for read ops
            glReadPixels(x, y, 1,1, GL_RED_INTEGER, GL_UNSIGNED_INT,0);
            syncObject = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            BufferObject.bindZERO(GL_PIXEL_PACK_BUFFER); // very important
        } else { if (syncObject == 0L) {
                Framebuffer.bindRead(framebuffer);
                Framebuffer.readBuffer(FRAMEBUFFER_SLOT_PIXEL_ID);
                pixelPackBuffer.bind();
                glReadPixels(x, y, 1,1, GL_RED_INTEGER, GL_UNSIGNED_INT,0);
                syncObject = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
                BufferObject.bindZERO(GL_PIXEL_PACK_BUFFER);
            } else { glGetSynciv(syncObject,GL_SYNC_STATUS,null,syncBuffer);
                syncStatus = syncBuffer.get(0);
            }
        }
    }

    private void scissor(Rectanglef quad) {
        int x = floor(quad.minX);
        int y = floor(quad.minY);
        int w = ceil(quad.maxX) - x;
        int h = ceil(quad.maxY) - y;
        glEnable(GL_SCISSOR_TEST);
        glScissor(x,y,w,h);
    }

    private boolean validBounds(float x1, float y1, float x2, float y2) {
        return x2 > x1 && y2 > y1;
    }

    public int colorIndexOf(Paragraph paragraph, Word word) {
        Word.Type wordType = word.type();
        if (wordType == Word.Type.REGULAR) {
            return paragraph.type().colorIndex;
        } else return wordType.colorIndex;
    }


}
