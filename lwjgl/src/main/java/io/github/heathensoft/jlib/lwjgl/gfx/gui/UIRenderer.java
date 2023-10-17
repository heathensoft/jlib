package io.github.heathensoft.jlib.lwjgl.gfx.gui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Repository;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.common.utils.U.round;
import static java.lang.Math.max;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL15.GL_STREAM_READ;
import static org.lwjgl.opengl.GL15.glUnmapBuffer;
import static org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER;
import static org.lwjgl.opengl.GL30.GL_MAP_READ_BIT;
import static org.lwjgl.opengl.GL30.GL_RED_INTEGER;
import static org.lwjgl.opengl.GL30.glMapBufferRange;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.glBlendEquationi;
import static org.lwjgl.opengl.GL40.glBlendFunci;

/**
 * @author Frederik Dahl
 * 14/10/2023
 */


public class UIRenderer implements Disposable {

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
    protected FontCollection fonts;
    protected UISpriteBatch spriteBatch;
    protected UITextBatch textBatch;
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


    public void drawText(String text, int font, int font_size, int color_id, float x, float y, float glow, boolean transparent) {
        if (rendering) {
            if (active_batch != TEXT_BATCH) {
                if (active_batch == SPRITE_BATCH) {
                    spriteBatch.flush();
                    shader_swaps++;
                } active_batch = TEXT_BATCH;
                Framebuffer.drawBuffers(0,1,2);
            } textBatch.draw(text, font, font_size, color_id, x, y, glow, transparent);
        }
    }

    public void drawElement(UISprite sprite, Vector4f quad) { drawElement(sprite, quad,0); }
    public void drawElement(UISprite sprite, Vector4f quad, int id) { drawElement(sprite, quad, id,0f); }
    public void drawElement(UISprite sprite, Vector4f quad, Color32 tint) { drawElement(sprite, quad, tint, 0); }
    public void drawElement(UISprite sprite, Vector4f quad, int id, float glow) { drawElement(sprite, quad, Color32.WHITE, id, glow); }
    public void drawElement(UISprite sprite, Vector4f quad, Color32 tint, int id) { drawElement(sprite, quad, tint, id, 0); }
    public void drawElement(UISprite sprite, Vector4f quad, Color32 tint, float glow) { drawElement(sprite, quad, tint, 0, glow); }
    public void drawElement(UISprite sprite, Vector4f quad, Color32 tint, int id, float glow) { drawElement(sprite, quad, tint, id, glow,false); }
    public void drawElement(UISprite sprite, Vector4f quad, Color32 tint, int id, float glow, boolean invisible_id) {
        if (rendering) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } spriteBatch.draw(sprite, quad, tint, id, glow, invisible_id);
        }
    }


    public void drawElement(Vector4f quad, Color32 color) { drawElement(quad, color, 0); }
    public void drawElement(Vector4f quad, Color32 color, int id) { drawElement(quad, color, id,0f); }
    public void drawElement(Vector4f quad, Color32 color, float glow) { drawElement(quad, color, 0, glow); }
    public void drawElement(Vector4f quad, Color32 color, int id, float glow) { drawElement(quad, color, id, glow,false); }
    public void drawElement(Vector4f quad, Color32 color, int id, float glow, boolean invisible_id) {
        if (rendering) {
            if (active_batch != SPRITE_BATCH) {
                if (active_batch == TEXT_BATCH) {
                    textBatch.flush();
                    shader_swaps++;
                } active_batch = SPRITE_BATCH;
                Framebuffer.drawBuffers(0,1,2,3);
            } spriteBatch.draw(quad, color, id, glow, invisible_id);
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
            fonts = new FontCollection(FONT_UNIFORM_BUFFER_BINDING_POINT);
            fonts.uploadFont(font,0);
        }
    }

    protected void initializeRenderBatches(int width, int height) throws Exception {
        if (textBatch == null) textBatch = new UITextBatch(fonts,TEXT_BATCH_CAPACITY,width,height);
        if (spriteBatch == null) spriteBatch = new UISpriteBatch(SPRITE_BATCH_CAPACITY,width,height);
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

    private static abstract class UIBatchADT implements Disposable {
        protected Vao vertexAttribArray;
        protected ShaderProgram shaderProgram; // disposed externally
        protected BufferObject vertexBuffer;
        protected FloatBuffer vertices;
        protected int buffer_capacity;
        protected int draw_calls;
        protected int count;
        abstract void flush();
        void setResolutionUniform(int width, int height) {
            shaderProgram.use().setUniform("u_resolution",width,height);
        } int resetDrawCalls() {
            int calls = draw_calls;
            draw_calls = 0;
            return calls;
        } public void dispose() {
            if (vertices != null) MemoryUtil.memFree(vertices);
            Disposable.dispose(vertexAttribArray,vertexBuffer,shaderProgram);
        }
    }


    private static final class UISpriteBatch extends UIBatchADT {

        public static final String SHADER_VERT = "res/jlib/lwjgl/glsl/ui_sprite.vert";
        public static final String SHADER_FRAG = "res/jlib/lwjgl/glsl/ui_sprite.frag";
        public static final int NUM_TEXTURE_SLOTS = 15;
        static final int NO_TEXTURE = 255;
        private final BufferObject indices;
        private final SamplerArray samplersDiffuse;
        private final SamplerArray samplersNormals;

        UISpriteBatch(int capacity, int width, int height) throws Exception {
            int vertex_size = 6;
            int sprite_size = vertex_size * 4;
            int vertex_size_bytes = vertex_size * Float.BYTES;
            samplersDiffuse = new SamplerArray(NUM_TEXTURE_SLOTS,GL_TEXTURE_2D,0);
            samplersNormals = new SamplerArray(NUM_TEXTURE_SLOTS,GL_TEXTURE_2D,NUM_TEXTURE_SLOTS);
            buffer_capacity = capacity;
            vertices = MemoryUtil.memAllocFloat(buffer_capacity * vertex_size);
            indices = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
            vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
            vertexAttribArray = new Vao().bind();
            indices.bind().bufferData(generateIndices(buffer_capacity));
            vertexBuffer.bind().bufferData((long) sprite_size * buffer_capacity * Float.BYTES); int pointer = 0;
            glVertexAttribPointer(0,2,GL_FLOAT,false,vertex_size_bytes,pointer);
            glEnableVertexAttribArray(0); pointer += 2 * Float.BYTES;
            glVertexAttribPointer(1,2,GL_FLOAT,false,vertex_size_bytes,pointer);
            glEnableVertexAttribArray(1); pointer += 2 * Float.BYTES;
            glVertexAttribPointer(2,4,GL_UNSIGNED_BYTE,true,vertex_size_bytes,pointer);
            glEnableVertexAttribArray(2); pointer += Float.BYTES;
            glVertexAttribPointer(3,1,GL_FLOAT,false,vertex_size_bytes,pointer);
            glEnableVertexAttribArray(3);
            String vShader = Resources.asString(SHADER_VERT);
            String fShader = Resources.asString(SHADER_FRAG);
            shaderProgram = new ShaderProgram(vShader,fShader);
            shaderProgram.createUniform("u_resolution");
            shaderProgram.createUniform("u_diffuse_textures");
            shaderProgram.createUniform("u_normals_textures");
            setResolutionUniform(width, height);
        }

        void draw(Vector4f quad, Color32 color, int id, float glow, boolean drawAlpha) {
            if (count == buffer_capacity) flush();
            int shader_bits = 255 | (round(clamp(glow) * 127.0f) << 8);
            shader_bits |= drawAlpha ? 1 << 15 : 0;
            shader_bits |= (id & 0xFFFF) << 16;
            float shader_ = Float.intBitsToFloat(shader_bits);
            vertices.put(quad.x).put(quad.w).put(0).put(0).put(color.floatBits()).put(shader_);
            vertices.put(quad.x).put(quad.y).put(0).put(0).put(color.floatBits()).put(shader_);
            vertices.put(quad.z).put(quad.y).put(0).put(0).put(color.floatBits()).put(shader_);
            vertices.put(quad.z).put(quad.w).put(0).put(0).put(color.floatBits()).put(shader_);
            count++;
        }

        void draw(UISprite sprite, Vector4f quad, Color32 tint, int id, float glow, boolean drawAlpha) {
            if (count == buffer_capacity) flush();
            TextureRegion region = sprite.textureRegion();
            // split into diffuse and normals. 15 texture slots (0-14). 15 = NO TEXTURE
            int diffuse_slot = samplersDiffuse.assignSlot(sprite.diffuseTexture());
            int normals_slot = sprite.hasNormals() ? samplersNormals.assignSlot(sprite.normalsTexture()) : 15;
            int shader_bits = (diffuse_slot | (normals_slot << 4)) & 0xFF;
            shader_bits |= (round(clamp(glow) * 127.0f) << 8);
            shader_bits |= drawAlpha ? 1 << 15 : 0;
            shader_bits |= (id & 0xFFFF) << 16;
            float shader_ = Float.intBitsToFloat(shader_bits);
            vertices.put(quad.x).put(quad.w).put(region.u()).put(region.v()).put(tint.floatBits()).put(shader_);
            vertices.put(quad.x).put(quad.y).put(region.u()).put(region.v2()).put(tint.floatBits()).put(shader_);
            vertices.put(quad.z).put(quad.y).put(region.u2()).put(region.v2()).put(tint.floatBits()).put(shader_);
            vertices.put(quad.z).put(quad.w).put(region.u2()).put(region.v()).put(tint.floatBits()).put(shader_);
            count++;
        }

        void flush() {
            if (count > 0) {
                vertices.flip();
                vertexAttribArray.bind();
                vertexBuffer.bind();
                vertexBuffer.bufferSubData(vertices, 0);
                shaderProgram.use();
                samplersDiffuse.uploadUniform(shaderProgram,"u_diffuse_textures");
                samplersNormals.uploadUniform(shaderProgram,"u_normals_textures");
                glDrawElements(GL_TRIANGLES,6 * count,GL_UNSIGNED_SHORT,0);
                vertices.clear();
                draw_calls++;
                count = 0;
            }
        }



        public void dispose() {
            super.dispose();
            Disposable.dispose(indices);
        }

        private short[] generateIndices(int sprites) {
            int len = sprites * 6;
            short[] indices = new short[len];
            short j = 0;
            for (int i = 0; i < len; i += 6, j += 4) {
                indices[i] = j;
                indices[i + 1] = (short)(j + 1);
                indices[i + 2] = (short)(j + 2);
                indices[i + 3] = (short)(j + 2);
                indices[i + 4] = (short)(j + 3);
                indices[i + 5] = j;
            } return indices;
        }

    }

    private static final class UITextBatch extends UIBatchADT {

        public static final String SHADER_VERT = "res/jlib/lwjgl/glsl/ui_text.vert";
        public static final String SHADER_GEOM = "res/jlib/lwjgl/glsl/ui_text.geom";
        public static final String SHADER_FRAG = "res/jlib/lwjgl/glsl/ui_text.frag";
        private final FontCollection fonts;

        UITextBatch(FontCollection fonts, int capacity, int width, int height) throws Exception {
            int vertex_size = 3;
            int vertex_size_bytes = vertex_size * Float.BYTES;
            this.fonts = fonts;
            buffer_capacity = capacity;
            vertexAttribArray = new Vao().bind();
            vertices = MemoryUtil.memAllocFloat(capacity * vertex_size);
            vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
            vertexBuffer.bind().bufferData((long) vertex_size_bytes * capacity);
            glVertexAttribPointer(0, vertex_size, GL_FLOAT, false, vertex_size_bytes, 0);
            glEnableVertexAttribArray(0);
            String vShader = Resources.asString(SHADER_VERT);
            String gShader = Resources.asString(SHADER_GEOM);
            String fShader = Resources.asString(SHADER_FRAG);
            shaderProgram = new ShaderProgram(vShader,gShader,fShader);
            shaderProgram.createUniform("u_resolution");
            shaderProgram.createUniform("u_font_textures");
            setResolutionUniform(width, height);
        }

        void draw(String text, int font, int size, int color_index, float x, float y, float glow, boolean transparent) {
            if (!text.isBlank() || size < 1) {
                fonts.bindFontMetrics(font);
                float scale = fonts.relativeScale(size);
                int info = transparent ? 0x8000_0000 : 0;
                info |= (fonts.currentFont() << 29);
                info |= (((size - 1) & 0xFF) << 21);
                info |= ((round(clamp(glow) * 255f) & 0xFF) << 13);
                info |= ((color_index & 0x3F) << 7);
                for (int i = 0; i < text.length(); i++) {
                    char c = (char)(text.charAt(i) & 0xFF);
                    pushVertex(x,y,info | c);
                    x += (fonts.advance(c) * scale);
                }
            }
        }

        private void pushVertex(float x, float y, int i) {
            if (count == buffer_capacity) flush();
            vertices.put(x).put(y).put(Float.intBitsToFloat(i));
            count++;
        }

        void flush() {
            if (count > 0) {
                shaderProgram.use();
                fonts.bindUploadTextures(shaderProgram,"u_font_textures");
                vertices.flip();
                vertexAttribArray.bind();
                vertexBuffer.bind();
                vertexBuffer.bufferSubData(vertices, 0);
                glDrawArrays(GL_POINTS, 0, count);
                vertices.clear();
                draw_calls++;
                count = 0;
            }
        }


    }


}
