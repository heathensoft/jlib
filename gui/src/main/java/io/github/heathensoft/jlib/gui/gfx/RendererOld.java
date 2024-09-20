package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.generic.Pool;
import io.github.heathensoft.jlib.common.storage.generic.Stack;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;
import org.joml.primitives.Rectanglei;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static java.lang.Math.max;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL30.GL_RED_INTEGER;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 03/06/2024
 */


public class RendererOld implements Disposable {

    // CONSTANTS
    public static final int GUI_UNIFORM_BUFFER_BINDING_POINT = 0;
    public static final int FONTS_UNIFORM_BUFFER_BINDING_POINT = 1;

    private static final int GUI_UNIFORM_BUFFER_SIZE = 8 * Float.BYTES;

    public static final int BLOOM_MAX_PING_PONG_ITERATIONS = 100;
    public static final int BLOOM_DEFAULT_PING_PONG_ITERATIONS = 10;
    public static final float BLOOM_MAX_BLOOM_THRESHOLD = 2.0f;
    public static final float BLOOM_DEFAULT_THRESHOLD = 0.2f;
    public static final boolean BLOOM_ENABLED_DEFAULT = true;

    private static final int TEXT_BATCH_CAPACITY = 2048;
    private static final int SPRITE_BATCH_CAPACITY = 512;
    private static final int LINE_BATCH_CAPACITY = 256;

    private static final int FRAMEBUFFER_SLOT_DIFFUSE = 0;
    private static final int FRAMEBUFFER_SLOT_EMISSIVE = 1;
    private static final int FRAMEBUFFER_SLOT_PIXEL_ID = 2;

    // todo: put batch code here instead


    // Debugging. values reset every 60 frame
    private int batch_swaps_max;
    private int batch_swaps;
    private int draw_calls_max;
    private int draw_calls;
    private int frame_count;

    private int active_batch;           // Sprites / Text / Lines (Sprites by Default)
    private int pixel_read_value;       // Element and Window under current Mouse Position
    private int active_window_id;       // Current "Window" being rendered (Optional)
    private boolean rendering;          // Currently Rendering GUI Elements
    private boolean draw_sprite_id;     // Render to sprite-batch id buffer
    private boolean paused;

    private float bloom_threshold;      // Custom tuned bloom brightness filter
    private int bloom_iterations;       // Bloom Iterations.
    private boolean bloom_enabled;      // Bloom On / Off

    private final BufferObject gui_uniform_buffer;
    private Framebuffer[] bloomBuffers;
    private Framebuffer framebuffer;
    private final ScissorStack scissorStack;
    private final SpriteBatch spriteBatch;
    private final TextBatch textBatch;
    private final LineBatch lineBatch;
    private Batch activeBatch;
    private final Fonts fonts;

    public RendererOld(int width, int height) throws Exception {
        initializeFramebuffer(width, height);
        initializeBloomBuffers(width, height);
        textBatch = new TextBatch(TEXT_BATCH_CAPACITY);
        lineBatch = new LineBatch(LINE_BATCH_CAPACITY);
        spriteBatch = new SpriteBatch(SPRITE_BATCH_CAPACITY);
        fonts = new Fonts(FONTS_UNIFORM_BUFFER_BINDING_POINT);
        scissorStack = new ScissorStack(this);
        gui_uniform_buffer = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW);
        gui_uniform_buffer.bufferData(GUI_UNIFORM_BUFFER_SIZE);
        gui_uniform_buffer.bindBufferBase(GUI_UNIFORM_BUFFER_BINDING_POINT);
        fonts.uploadDefaultFonts();

        bloom_enabled = BLOOM_ENABLED_DEFAULT;
        bloom_threshold = BLOOM_DEFAULT_THRESHOLD;
        bloom_iterations = BLOOM_DEFAULT_PING_PONG_ITERATIONS;
    }


    public void begin(Vector2f mouse) {
        if (rendering) throw new IllegalStateException("Illegal call to begin(). GUI Renderer already rendering");

        // Performance Debugging
        if (frame_count == 60) {
            frame_count = 0;
            batch_swaps_max = batch_swaps;
            draw_calls_max = draw_calls;
        } batch_swaps = 0;

        float run_time = (float) Engine.get().time().runTimeSeconds();
        float resolution_w = framebuffer.width();
        float resolution_h = framebuffer.height();
        float mouse_screen_x = mouse.x * resolution_w;
        float mouse_screen_y = mouse.y * resolution_h;

        // Upload GUI Uniform Buffer
        try (MemoryStack stack = MemoryStack.stackPush()){
            final int BLOCK_SIZE = 8; // Float
            FloatBuffer buffer = stack.mallocFloat(BLOCK_SIZE);
            buffer.position(3).put(run_time);
            buffer.put(resolution_w).put(resolution_h);
            buffer.put(mouse_screen_x).put(mouse_screen_y);
            gui_uniform_buffer.bind();
            gui_uniform_buffer.bufferSubData(buffer.flip(),0);
        }

        // Pixel Read operation (Sprite ID Buffer is set for read-ops)
        Framebuffer.bind(framebuffer);
        Framebuffer.viewport();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.callocInt(1);
            glReadPixels(round(mouse_screen_x),round(mouse_screen_y),1,1, GL_RED_INTEGER, GL_UNSIGNED_INT,buffer);
            pixel_read_value = buffer.get(0);
        }

        // Clear All Render Buffer Targets
        Framebuffer.drawBuffers(
                FRAMEBUFFER_SLOT_DIFFUSE,
                FRAMEBUFFER_SLOT_EMISSIVE,
                FRAMEBUFFER_SLOT_PIXEL_ID
        ); Framebuffer.clear();

        // Correct Alpha Blending / Disable Scissor by Default
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFuncSeparate(
                GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA,
                GL_ONE,GL_ONE_MINUS_SRC_ALPHA
        ); glDisable(GL_SCISSOR_TEST);

        // Upload Textures for All Uploaded Fonts
        ShaderProgram.bindProgram(textBatch.shaderProgram());
        fonts.bindUploadTextures("u_font_textures");

        // Sprite Batch as Default Batch
        activeBatch = spriteBatch;
        if (!draw_sprite_id) {
            Framebuffer.drawBuffers(
                    FRAMEBUFFER_SLOT_DIFFUSE,
                    FRAMEBUFFER_SLOT_EMISSIVE
            );
        }

        // Enter GUI Elements Rendering state
        rendering = true;
    }

    public void end() {
        if (!rendering) throw new IllegalStateException("Illegal call to end(). GUI Renderer not rendering");

        // Final Flush of Active Batch. Pop scissor stack.
        activeBatch.flush();
        scissorStack.reset();

        // Performance Debugging
        frame_count++;
        draw_calls = spriteBatch.resetDrawCalls();
        draw_calls += textBatch.resetDrawCalls();
        draw_calls += lineBatch.resetDrawCalls();
        draw_calls_max = max(draw_calls, draw_calls_max);
        batch_swaps_max = max(batch_swaps, batch_swaps_max);

        // Custom Bloom for GUI
        if (isBloomEnabled()) {
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
            ShaderProgram.setUniform("u_threshold",bloom_threshold);

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

        // Exiting GUI Rendering State
        rendering = false;
    }

    public void updateResolution(int width, int height) throws Exception {
        if (rendering) throw new IllegalStateException("Illegal attempt to update resolution while rendering");
        if (framebuffer.width() != width || framebuffer.height() != height) {
            initializeFramebuffer(width, height);
            initializeBloomBuffers(width, height);
        }
    }

    public void enableSpriteBatchID(boolean enable) {
        if (rendering && activeBatch == spriteBatch) {
            if (enable) {
                if (!draw_sprite_id) {
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(
                            FRAMEBUFFER_SLOT_DIFFUSE,
                            FRAMEBUFFER_SLOT_EMISSIVE,
                            FRAMEBUFFER_SLOT_PIXEL_ID);
                }
            } else {
                if (draw_sprite_id) {
                    spriteBatch.flush();
                    Framebuffer.drawBuffers(
                            FRAMEBUFFER_SLOT_DIFFUSE,
                            FRAMEBUFFER_SLOT_EMISSIVE
                    );
                }
            }
        }
        draw_sprite_id = enable;
    }

    public void flushActiveBatch() {
        if (rendering) {
            activeBatch.flush();
        }
    }

    public void setActiveWindowID(int id) {
        id = id & 0xFFFF;
        if (active_window_id != id) {
            if (rendering) {
                ShaderProgram.bindProgram(GUI.shaders.sprite_program);
                ShaderProgram.setUniform("u_window",id);
            } active_window_id = id;
        }
    }

    public Fonts fonts() { return fonts; }
    public SpriteBatch spriteBatch() { return spriteBatch; }
    public TextBatch textBatch() { return textBatch; }
    public LineBatch lineBatch() { return lineBatch; }
    public Framebuffer framebuffer() { return framebuffer; }
    public Texture framebufferDiffuseTexture() { return framebuffer.texture(FRAMEBUFFER_SLOT_DIFFUSE); }
    public Texture framebufferEmissiveTexture() { return framebuffer.texture(FRAMEBUFFER_SLOT_EMISSIVE); }
    public Texture bloomBufferTexture() { return bloomBuffers[0].texture(0); }

    public int mousePixelReadValue() { return pixel_read_value; }
    public long drawCallCount() { return draw_calls_max;  }
    public long batchSwapCount() { return batch_swaps_max; }

    public boolean pushScissor(Rectanglef quad) { if (rendering) return scissorStack.push(quad);
    else throw new IllegalStateException("Cannot push/pop scissors while renderer is not rendering");
    }public boolean pushScissor(float x1, float y1, float x2, float y2) {
        if (rendering) return scissorStack.push(x1, y1, x2, y2);
        else throw new IllegalStateException("Cannot push/pop scissors while renderer is not rendering");
    }public void popScissor() { if (rendering) scissorStack.pop();
    else throw new IllegalStateException("Cannot push/pop scissors while renderer is not rendering"); }

    public void enableBloom(boolean enable) { bloom_enabled = enable; }
    public boolean isBloomEnabled() { return bloom_enabled && bloom_iterations > 0; }
    public void setBloomPingPongIterations(int iterations) {
        bloom_iterations = U.clamp(iterations,0,BLOOM_MAX_PING_PONG_ITERATIONS);
    } public int bloomPingPongIterationsCount() { return bloom_iterations; }
    public void setBloomThreshold(float threshold) {
        bloom_threshold = U.clamp(threshold,0f,BLOOM_MAX_BLOOM_THRESHOLD);
    }

    public void dispose() {

    }


    private void setActiveBatch(Batch batch) {
        // rendering must be true
        if (batch != activeBatch) {
            activeBatch.flush();
            batch_swaps++;
            if (activeBatch == spriteBatch) {
                if (batch == textBatch) {
                    // Sprite -> Text
                    if (draw_sprite_id) {
                        Framebuffer.drawBuffers(
                                FRAMEBUFFER_SLOT_DIFFUSE,
                                FRAMEBUFFER_SLOT_EMISSIVE
                        );
                    }
                } else {
                    // Sprite -> Line
                    Framebuffer.drawBuffer(
                            FRAMEBUFFER_SLOT_DIFFUSE
                    );
                }
            } else if (activeBatch == textBatch) {
                if (batch == spriteBatch) {
                    // Text -> Sprite
                    if (draw_sprite_id) {
                        Framebuffer.drawBuffers(
                                FRAMEBUFFER_SLOT_DIFFUSE,
                                FRAMEBUFFER_SLOT_EMISSIVE,
                                FRAMEBUFFER_SLOT_PIXEL_ID
                        );
                    }
                } else {
                    // Text -> Line
                    Framebuffer.drawBuffer(
                            FRAMEBUFFER_SLOT_DIFFUSE
                    );
                }
            } else {
                if (batch == spriteBatch) {
                    // Line -> Sprite
                    if (draw_sprite_id) {
                        Framebuffer.drawBuffers(
                                FRAMEBUFFER_SLOT_DIFFUSE,
                                FRAMEBUFFER_SLOT_EMISSIVE,
                                FRAMEBUFFER_SLOT_PIXEL_ID
                        );
                    } else {
                        Framebuffer.drawBuffers(
                                FRAMEBUFFER_SLOT_DIFFUSE,
                                FRAMEBUFFER_SLOT_EMISSIVE
                        );
                    }
                } else {
                    // Line -> Text
                    Framebuffer.drawBuffers(
                            FRAMEBUFFER_SLOT_DIFFUSE,
                            FRAMEBUFFER_SLOT_EMISSIVE
                    );
                }
            }
            activeBatch = batch;
        }
    }

    private void initializeFramebuffer(int width, int height) throws Exception{
        if (framebuffer != null) framebuffer.dispose();
        framebuffer = new Framebuffer(width,height);
        Framebuffer.bind(framebuffer);
        Framebuffer.setClearColor(0,0,0,0);
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        // Diffuse Buffer
        Texture diffuse_texture = Texture.generate2D(width, height);
        diffuse_texture.bindToActiveSlot();
        diffuse_texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
        diffuse_texture.filterLinear();
        diffuse_texture.clampToEdge();
        Framebuffer.attachColor(diffuse_texture,FRAMEBUFFER_SLOT_DIFFUSE,true);
        // Emissive Buffer
        Texture emissive_texture = Texture.generate2D(width, height);
        emissive_texture.bindToActiveSlot();
        emissive_texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED);
        emissive_texture.filterLinear();
        emissive_texture.clampToEdge();
        Framebuffer.attachColor(emissive_texture,FRAMEBUFFER_SLOT_EMISSIVE,true);
        // Sprite ID Buffer
        Texture uid_texture = Texture.generate2D(width,height);
        uid_texture.bindToActiveSlot();
        uid_texture.filterNearest();
        uid_texture.clampToEdge();
        uid_texture.allocate(TextureFormat.R32_UNSIGNED_INTEGER);
        Framebuffer.attachColor(uid_texture,FRAMEBUFFER_SLOT_PIXEL_ID,true);
        // Set ID Buffer to Read Buffer
        Framebuffer.drawBuffers(0,1,2);
        Framebuffer.readBuffer(FRAMEBUFFER_SLOT_PIXEL_ID);
        Framebuffer.checkStatus();
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

    private static final class ScissorStack {
        private static final class RectanglePool extends Pool<Rectanglei> {
            public RectanglePool() { super(4, 16); }
            protected Rectanglei newObject() { return new Rectanglei(); }
        } private final RectanglePool scissor_pool = new RectanglePool();
        private final Stack<Rectanglei> scissor_stack = new Stack<>(8);
        private final RendererOld renderer;
        ScissorStack(RendererOld renderer) { this.renderer = renderer; }
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
                renderer.flushActiveBatch();
                glEnable(GL_SCISSOR_TEST);
                glScissor(scissor.minX,scissor.minY,w,h);
                scissor_stack.push(scissor);
                return true;
            } else scissor_pool.free(scissor);
            return false;
        }

        void pop() {
            if (!scissor_stack.isEmpty()) { renderer.flushActiveBatch();
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

}
