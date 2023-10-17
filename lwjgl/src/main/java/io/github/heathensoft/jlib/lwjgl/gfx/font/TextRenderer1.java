package io.github.heathensoft.jlib.lwjgl.gfx.font;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.gfx.Font;
import io.github.heathensoft.jlib.lwjgl.gfx.gui.FontCollection;
import io.github.heathensoft.jlib.lwjgl.utils.Repository;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.FloatBuffer;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.common.utils.U.round;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 10/10/2023
 */


public class TextRenderer1 implements Disposable {

    private static final String U_FONT_TEXTURES = "u_font_textures";
    private static final String U_RESOLUTION = "u_resolution";
    private static final String U_NUM_FONTS = "u_num_fonts";

    static final String DEFAULT_FONT = "Balla";

    private ScreenQuad screenQuad;
    private Framebuffer framebuffer;
    private ShaderProgram shader;
    private VertexBuffer vertexBuffer;
    private FontCollection fonts;

    private boolean rendering;

    public void dispose() {
        Disposable.dispose(vertexBuffer,fonts,shader,framebuffer,screenQuad);
    }

    public TextRenderer1(Resolution resolution, String vShader, String gSShader, String fShader ) throws Exception {
        shader = loadShaderProgram(vShader,gSShader,fShader,resolution);
        fonts = new FontCollection(0);
        fonts.uploadFont(loadDefaultFont(),0);
        vertexBuffer = new VertexBuffer(1024);
        initializeFramebuffer(resolution.width(),resolution.height());
        Framebuffer.bindDefault();
        Framebuffer.setClearColor(0.2f,0.1f,0.15f,1);
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        screenQuad = new ScreenQuad();
    }

    protected Font loadDefaultFont() throws Exception {
        String repo_name = DEFAULT_FONT + ".repo";
        String repo_location = "res/jlib/lwjgl/font/";
        Logger.debug("TextRenderer: loading default font: " + repo_name);
        Repository font_repo = Repository.loadFromResources(repo_location + repo_name);
        return font_repo.getFont(DEFAULT_FONT);
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
        Framebuffer.attachColor(diffuse_texture,0,true);

        Texture normals_texture = Texture.generate2D(width, height);
        normals_texture.bindToActiveSlot();
        normals_texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        normals_texture.nearest();
        normals_texture.clampToEdge();
        Framebuffer.attachColor(normals_texture,1,true);

        Texture emissive_texture = Texture.generate2D(width, height);
        emissive_texture.bindToActiveSlot();
        emissive_texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED);
        emissive_texture.nearest();
        emissive_texture.clampToEdge();
        Framebuffer.attachColor(emissive_texture,2,true);

        Texture uid_texture = Texture.generate2D(width,height);
        uid_texture.bindToActiveSlot();
        uid_texture.nearest();
        uid_texture.clampToEdge();
        uid_texture.allocate(TextureFormat.R32_UNSIGNED_INTEGER);
        Framebuffer.attachColor(uid_texture,3,true);

        Framebuffer.drawBuffers(0,1,2,3);
        Framebuffer.readBuffer(3);
        Framebuffer.checkStatus();
    }

    public void start() {
        if (!rendering) {
            vertexBuffer.start();
            glEnable(GL11C.GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
            Framebuffer.bind(framebuffer);
            Framebuffer.drawBuffer(0);
            Framebuffer.viewport();
            Framebuffer.clear();
            shader.use();
            fonts.bindUploadTextures(shader,U_FONT_TEXTURES);
            //shader.setUniform1i(U_FONT_TEXTURES,0);
            //fonts.font_texture[0].bindToSlot(0);
            rendering = true;
        } else throw new IllegalStateException();

    }

    public void end() {
        if (rendering) {
            vertexBuffer.end();
            glDisable(GL11C.GL_BLEND);
            Framebuffer.bindDefault();
            Framebuffer.viewport();
            Framebuffer.clear();
            screenQuad.render(framebuffer.texture(0));
            // post-processing ?
            rendering = false;
        } else throw new IllegalStateException();
    }

    public void drawWord(String word, float x, float y, int size, int color, float bloom, boolean translucent) {
        //word += (char) 0;
        if (rendering) {
            if (!word.isBlank() || size < 1) {
                float scale = fonts.relativeScale(size);
                int info = translucent ? 0x8000_0000 : 0;
                info |= (fonts.currentFont() << 29);
                info |= (((size - 1) & 0xFF) << 21);
                info |= ((round(clamp(bloom) * 255f) & 0xFF) << 13);
                info |= ((color & 0x3F) << 7);
                for (int i = 0; i < word.length(); i++) {
                    char c = (char)(word.charAt(i) & 0xFF);
                    int g = (info | c);
                    float info_bits = Float.intBitsToFloat(info | c);
                    vertexBuffer.push(x,y,info_bits);
                    x += (fonts.advance(c) * scale);
                }

            }
        }
    }

    public void updateResolution(Resolution resolution) throws Exception {
        /*
        if(framebuffer != null) {
            int w = resolution.width();
            int h = resolution.height();
            if (framebuffer.width() != w || framebuffer.height() != h) {
                framebuffer.dispose();
                framebuffer = loadFramebuffer(w, h);
                shader.setUniform(U_RESOLUTION,new Vector2f(w,h));
            }
        }

         */
    }

    private ShaderProgram loadShaderProgram(String vShader, String gShader, String fShader, Resolution resolution) throws Exception {
        ShaderProgram shaderProgram = new ShaderProgram(vShader,gShader,fShader).use();
        shaderProgram.createUniform(U_RESOLUTION);
        shaderProgram.createUniform(U_FONT_TEXTURES);
        int w = resolution.width();
        int h = resolution.height();
        Vector2f res_uniform = new Vector2f(w,h);
        shaderProgram.setUniform(U_RESOLUTION,res_uniform);
        return shaderProgram;
    }



    private static final class VertexBuffer implements Disposable {
        private int count;
        private int draw_calls;
        private int draw_calls_total;
        private final int capacity;
        private final Vao vertexAttribArray;
        private final BufferObject vertexBuffer;
        private final FloatBuffer vertices;
        // ***************** // ***************** //
        // Vertex: GL_POINTS // ****** LSB ****** //
        // SIZE: 12 BYTES    // 7 bit char  (128) //
        // ***************** // 6 bit color (64)  //
        // ***************** // 8 bit bloom (256) //
        // 32 bit pos_x	     // 7 bit size  (128) //
        // ***************** // 2 bit font  (4)   //
        // 32 bit pos_y      // 1 bit N/A      	  //
        // ***************** // ****** MSB ****** //
        VertexBuffer(int capacity) {
            this.vertexAttribArray = new Vao().bind();
            this.vertexBuffer = new BufferObject(GL_ARRAY_BUFFER, GL_DYNAMIC_DRAW);
            this.vertices = MemoryUtil.memAllocFloat(3 * capacity);
            this.vertexBuffer.bind().bufferData((long) capacity * Float.BYTES * 3);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
            glEnableVertexAttribArray(0);
            this.capacity = capacity;
        }
        void start() { draw_calls = 0; }
        void end() { flush(); }
        void push(float x, float y, float i) {
            if (count == capacity) flush();
            vertices.put(x).put(y).put(i);
            count++;
        }
        void flush() {
            if (count > 0) {
                vertices.flip();
                vertexAttribArray.bind();
                vertexBuffer.bind();
                vertexBuffer.bufferSubData(vertices, 0);
                glDrawArrays(GL_POINTS, 0, count);
                vertices.clear();
                count = 0;
                draw_calls++;
                draw_calls_total++;
            }
        }
        public int drawCalls() { return draw_calls; }
        public int drawCallsTotal() { return draw_calls_total; }
        public void dispose() {
            if (vertices != null)
                MemoryUtil.memFree(vertices);
            Disposable.dispose(vertexAttribArray, vertexBuffer);
        }
    }
}
