package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Image;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class HudAssets implements Disposable {

    private static final String ATLAS_PNG_PATH = "hud/atlas/hud.png";
    private static final String ATLAS_TXT_PATH = "hud/atlas/hud.atlas";
    private static final String BACKGROUND_TXT_PATH = "hud/background/bg_";
    private static final int font_rows = 6;
    private static final int font_cols = 16;
    private static final int font_width = 8;
    private static final int font_height = 16;
    private static final int font_characters = 95;
    private static final int font_first_char = 32;
    private final TextureAtlas atlas;
    private final Texture[] textures;
    private final TextureRegion blank;
    private final TextureRegion[] characters;
    public final int NUM_TEXTURES = 4;
    public final int ATLAS_TEXTURE_SLOT = 0;
    public final int BACKGROUND_01_TEXTURE_SLOT = 1;
    public final int BACKGROUND_02_TEXTURE_SLOT = 2;
    public final int BACKGROUND_03_TEXTURE_SLOT = 3;

    public HudAssets() throws Exception {
        Resources resources = new Resources(HudAssets.class);
        List<String> layout = resources.asLines(ATLAS_TXT_PATH);
        Image img = resources.image(ATLAS_PNG_PATH,4000);
        Texture atlas_texture = new Texture(GL_TEXTURE_2D);
        atlas_texture.bindToActiveSlot();
        atlas_texture.nearest();
        atlas_texture.wrapST(GL_CLAMP_TO_BORDER);
        atlas_texture.image_2D(img);
        atlas = new TextureAtlas(atlas_texture,layout);
        TextureRegion font_region = atlas.get("font");
        blank = atlas.get("blank");
        if (font_region == null || blank == null)
            throw new Exception("unable to read atlas");
        font_region.subDivide(font_cols,font_rows);
        characters = new TextureRegion[font_characters];
        for (int i = 0; i < font_characters; i++)
            characters[i] = font_region.subRegion(i);
        textures = new Texture[NUM_TEXTURES];
        textures[0] = atlas_texture;
        for (int i = 1; i < NUM_TEXTURES; i++) {
            String path = BACKGROUND_TXT_PATH + i + ".png";
            img = resources.image(BACKGROUND_TXT_PATH);
            Texture background = new Texture(GL_TEXTURE_2D);
            background.bindToActiveSlot();
            background.nearest();
            background.wrapST(GL_REPEAT);
            background.image_2D(img);
            textures[i] = background;
        }
    }

    public TextureRegion blank() {
        return blank;
    }
    
    public TextureRegion getAsset(String name) {
        return atlas.regions().getOrDefault(name,blank);
    }
    
    public TextureRegion getChar(char c) {
        return characters[Math.max(0,(c & 0x7F)-font_first_char)];
    }
    
    public TextureRegion getChar(byte c) {
        return characters[Math.max(0,(c & 0x7F)-font_first_char)];
    }

    public Texture texture(int slot) {
        return textures[slot];
    }
    
    public int fontWidthPixels() {
        return font_width;
    }
    
    public int fontHeightPixels() {
        return font_height;
    }
    
    @Override
    public void dispose() {
        for (Texture t : textures)
            Disposable.dispose(t);
    }
}
