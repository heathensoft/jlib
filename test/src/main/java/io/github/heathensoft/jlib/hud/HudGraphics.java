package io.github.heathensoft.jlib.hud;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.hud.ui.text.AsciiFont;
import io.github.heathensoft.jlib.hud.ui.text.Glyph;
import io.github.heathensoft.jlib.lwjgl.graphics.Image;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;

/**
 * @author Frederik Dahl
 * 16/11/2022
 */


public class HudGraphics implements Disposable {

    public static final String WINDOW_CLOSE_ICON = "window_close_icon";
    public static final String WINDOW_RESTORE_ICON = "window_restore_icon";
    public static final String WINDOW_MAXIMIZE_ICON = "window_maximize_icon";
    public static final String WINDOW_BORDER_VERTICAL = "window_border_vertical";
    public static final String WINDOW_BORDER_BOTTOM = "window_border_bottom";
    public static final String WINDOW_BORDER_TOP = "window_border_top";
    public static final String ICON_SELECTOR = "icon_selector";
    public static final String ICON_OUTLINE = "icon_outline";
    public static final String SETTINGS_ICON = "settings_icon";
    public static final String SEARCH_ICON = "search_icon";
    public static final String STATS_ICON = "stats_icon";
    public static final String BUILD_ICON = "build_icon";
    public static final String AMIGA_FONT = "amiga_font";
    public static final String TINY_FONT = "tiny_font";
    public static final String BLANK = "blank";

    private static final String PRIMARY_ATLAS_PNG_PATH = "res/jlib/hud/atlas/hud.png";
    private static final String PRIMARY_ATLAS_TXT_PATH = "res/jlib/hud/atlas/hud.atlas";
    private static final String TINY_FONT_GLYPH_DATA_PATH = "res/jlib/hud/glyph/tiny_font.txt";

    private static final int amiga_font_rows = 6;
    private static final int amiga_font_cols = 16;
    private static final int amiga_font_width = 8;
    private static final int amiga_font_height = 16;
    private static final int amiga_font_characters = 95;
    private static final int amiga_font_first_char = 32;

    private final TextureAtlas atlas;
    private final Texture[] textures;
    private final TextureRegion blank;
    private final TextureRegion[] amiga_characters;
    private final Map<String,TextureRegion> user_assets;

    public final int NUM_TEXTURES = 8;

    public HudGraphics() throws Exception {
        user_assets = new HashMap<>(31);
        Resources resources = new Resources(HudGraphics.class);
        List<String> layout = resources.asLines(PRIMARY_ATLAS_TXT_PATH);
        Image img = resources.image(PRIMARY_ATLAS_PNG_PATH,4000);
        Texture atlas_texture = new Texture(GL_TEXTURE_2D);
        atlas_texture.bindToActiveSlot();
        atlas_texture.filter(GL_NEAREST,GL_NEAREST);
        atlas_texture.wrapST(GL_CLAMP_TO_BORDER);
        atlas_texture.image_2D(img);
        atlas = new TextureAtlas(atlas_texture,layout);
        TextureRegion font_region = atlas.get(AMIGA_FONT);
        blank = atlas.get(BLANK);
        if (font_region == null || blank == null)
            throw new Exception("unable to read atlas");
        font_region.subDivide(amiga_font_cols, amiga_font_rows);
        amiga_characters = new TextureRegion[amiga_font_characters];
        for (int i = 0; i < amiga_font_characters; i++)
            amiga_characters[i] = font_region.subRegion(i);
        textures = new Texture[NUM_TEXTURES];
        textures[0] = atlas_texture;
    }

    public void provideUserAsset(String key, TextureRegion asset) {
        user_assets.put(key, asset);
    }

    public Texture provideTexture(Texture texture, int slot) {
        if (slot < 1 || slot > (NUM_TEXTURES - 1))
            throw new IllegalStateException("possible slots: [1-"+(NUM_TEXTURES - 1)+"]");
        Texture existing = textures[slot];
        textures[slot] = texture;
        return existing;
    }

    public TextureRegion blank() {
        return blank;
    }
    
    public TextureRegion coreAsset(String name) {
        return atlas.regions().getOrDefault(name,blank);
    }

    public TextureRegion userAsset(String name) {
        return user_assets.get(name);
    }

    public AsciiFont tinyFont() {
        /* Todo: */
        return null;
    }

    public Glyph getCharTiny(char c) {
        /* Todo: */
        return null;
    }

    public Glyph getCharTiny(byte c) {
        /* Todo: */
        return null;
    }

    public TextureRegion getCharAmiga(char c) {
        return amiga_characters[Math.max(0,(c & 0x7F)- amiga_font_first_char)];
    }
    
    public TextureRegion getCharAmiga(byte c) {
        return amiga_characters[Math.max(0,(c & 0x7F)- amiga_font_first_char)];
    }

    public Texture texture(int slot) {
        return textures[slot];
    }

    public Texture primaryAtlasTexture() {
        return textures[0];
    }
    
    public int fontWidthPixels() {
        return amiga_font_width;
    }
    
    public int fontHeightPixels() {
        return amiga_font_height;
    }
    
    @Override
    public void dispose() {
        for (Texture t : textures)
            Disposable.dispose(t);
    }
}