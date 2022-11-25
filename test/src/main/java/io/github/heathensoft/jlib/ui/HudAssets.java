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
 * Font is mono_spaced and there is no space between lines;
 *
 * @author Frederik Dahl
 * 16/11/2022
 */


public class HudAssets implements Disposable {
    
    private static final String ATLAS_PNG_PATH = "hud/hud.png";
    private static final String ATLAS_TXT_PATH = "hud/hud.atlas";
    private static final int png_size = 4000; // approx
    private static final int font_rows = 6;
    private static final int font_cols = 16;
    private static final int font_width = 8;
    private static final int font_height = 16;
    private static final int font_characters = 95;
    private static final int font_first_char = 32;
    
    private final TextureAtlas atlas;
    private final TextureRegion blank;
    private final TextureRegion[] characters;

    public HudAssets() throws Exception {
        Resources resources = new Resources(HudAssets.class);
        List<String> layout = resources.asLines(ATLAS_TXT_PATH);
        Image img = resources.image(ATLAS_PNG_PATH,png_size);
        Texture atlas_texture = new Texture(GL_TEXTURE_2D);
        atlas_texture.bindToActiveSlot();
        atlas_texture.nearest();
        atlas_texture.wrapST(GL_CLAMP_TO_BORDER);
        atlas_texture.image_2D(img);
        atlas = new TextureAtlas(atlas_texture,layout);
        TextureRegion font_region = atlas.get("font");
        blank = atlas.get("blank");
        if (font_region == null || blank == null) {
            throw new Exception("unable to read atlas");
        }
        font_region.subDivide(font_cols,font_rows);
        characters = new TextureRegion[font_characters];
        for (int i = 0; i < font_characters; i++) {
            characters[i] = font_region.subRegion(i);
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
    
    public Texture assets_texture() {
        return atlas.texture();
    }
    
    public int fontWidthPixels() {
        return font_width;
    }
    
    public int fontHeightPixels() {
        return font_height;
    }
    
    @Override
    public void dispose() {
        Disposable.dispose(atlas);
    }
}
