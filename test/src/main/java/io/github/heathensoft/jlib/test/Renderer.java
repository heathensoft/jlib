package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.primitive.IntArray2D;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;

/**
 * @author Frederik Dahl
 * 13/11/2022
 */


public class Renderer implements Disposable {
    
    private static final String SPRITE_VS_PATH = "sprite_vert.glsl";
    private static final String SPRITE_FS_PATH = "sprite_frag.glsl";
    private static final String DEFAULT_VS_PATH = "screen_vert.glsl";
    private static final String DEFAULT_FS_PATH = "screen_frag.glsl";
    private static final String U_SAMPLER = "u_sampler";
    private static final String U_COMBINED = "u_combined";
    
    
    private ShaderProgram defaultShader;
    private ShaderProgram spriteShader;
    private SpriteBatch spriteBatch;
    private SpriteFBO spriteFBO;
    private ScreenQuad screenQuad;
    private Texture spriteTexture;
    
    public Renderer(int width, int height) throws Exception {
        spriteBatch = new SpriteBatch(128);
        spriteFBO = new SpriteFBO(width, height);
        screenQuad = new ScreenQuad();
        createTexture();
        createShaders();
        spriteBatch.setShader(spriteShader);
    }
    
    public void begin(Matrix4f combined) {
        Framebuffer.bindDraw(spriteFBO);
        Framebuffer.viewport();
        Framebuffer.clear();
        glDisable(GL_DEPTH);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        spriteShader.use();
        spriteShader.setUniform(U_COMBINED,combined);
        spriteShader.setUniform1i(U_SAMPLER,0);
        spriteTexture.bindToSlot(0);
        spriteBatch.begin();
    }
    
    public void draw(Sprite sprite) {
        spriteBatch.draw(sprite);
    }
    
    public void end() {
        spriteBatch.end();
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        glDisable(GL_BLEND);
        defaultShader.use();
        defaultShader.setUniform1i(U_SAMPLER,0);
        spriteFBO.diffuseTexture().bindToSlot(0);
        screenQuad.render();
    }
    
    public Texture spriteTexture() {
        return spriteTexture;
    }
    
    private void createShaders() throws Exception {
        String vs_shader;
        String fs_shader;
        Resources resources = new Resources();
        vs_shader = resources.asString(SPRITE_VS_PATH);
        fs_shader = resources.asString(SPRITE_FS_PATH);
        spriteShader = new ShaderProgram(vs_shader,fs_shader);
        spriteShader.createUniform(U_COMBINED);
        spriteShader.createUniform(U_SAMPLER);
        vs_shader = resources.asString(DEFAULT_VS_PATH);
        fs_shader = resources.asString(DEFAULT_FS_PATH);
        defaultShader = new ShaderProgram(vs_shader,fs_shader);
        defaultShader.createUniform(U_SAMPLER);
    }
    
    private void createTexture() {
        int yellow = new Color(1,1,0,1).toIntBits();
        int red = new Color(1,0,0,1).toIntBits();
        int size = 16;
        IntArray2D data = new IntArray2D(size);
        data.write(yellow);
        data.write(red,3,3,12,12);
        spriteTexture = new Texture(GL_TEXTURE_2D);
        spriteTexture.bindToActiveSlot();
        spriteTexture.wrapST(GL_CLAMP_TO_BORDER);
        spriteTexture.nearest();
        spriteTexture.RGBA8_2D(data);
    }
    
    @Override
    public void dispose() {
        Disposable.dispose(
        spriteTexture, spriteFBO,spriteBatch,
        screenQuad,spriteShader,defaultShader);
    }
}
