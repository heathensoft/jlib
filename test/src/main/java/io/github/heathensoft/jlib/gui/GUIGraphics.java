package io.github.heathensoft.jlib.gui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.gui.text.AsciiFont;
import io.github.heathensoft.jlib.lwjgl.graphics.Image;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.graphics.surface.DepthMap16;
import io.github.heathensoft.jlib.lwjgl.graphics.surface.DepthMap8;
import io.github.heathensoft.jlib.lwjgl.graphics.surface.NormalMap;
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


public class GUIGraphics implements Disposable {


    private static final String PRIMARY_ATLAS_PNG_PATH = "res/jlib/hud/atlas/hud.png";
    private static final String PRIMARY_ATLAS_TXT_PATH = "res/jlib/hud/atlas/hud.atlas";
    private static final String TINY_FONT_GLYPH_DATA_PATH = "res/jlib/hud/glyphs/pixuf.txt";

    public static final String WINDOW_CLOSE_ICON = "window_close_icon";
    public static final String WINDOW_RESTORE_ICON = "window_restore_icon";
    public static final String WINDOW_MAXIMIZE_ICON = "window_maximize_icon";
    public static final String WINDOW_BORDER_VERTICAL = "window_border_vertical";
    public static final String WINDOW_BORDER_BOTTOM = "window_border_bottom";
    public static final String WINDOW_BORDER_TOP = "window_border_top";
    public static final String SCROLLBAR_CONTAINER_BOTTOM = "scrollbar_container_bottom";
    public static final String SCROLLBAR_CONTAINER_TOP = "scrollbar_container_top";
    public static final String SCROLLBAR_BOTTOM = "scrollbar_bottom";
    public static final String SCROLLBAR_TOP = "scrollbar_top";
    public static final String ICON_SELECTOR = "icon_selector";
    public static final String ICON_OUTLINE = "icon_outline";
    public static final String SETTINGS_ICON = "settings_icon";
    public static final String SEARCH_ICON = "search_icon";
    public static final String STATS_ICON = "stats_icon";
    public static final String BUILD_ICON = "build_icon";
    public static final String AMIGA_FONT = "amiga_font";
    public static final String TINY_FONT = "tiny_font";
    public static final String BLANK = "blank";

    private final Texture[] textures;
    private final AsciiFont tiny_font;
    private final AsciiFont amiga_font;
    private final TextureRegion blank;
    private final TextureAtlas core_atlas;
    private final Map<String,TextureRegion> user_assets;

    public final int NUM_TEXTURES = 8;

    public GUIGraphics() throws Exception {

        Resources resources = new Resources(GUIGraphics.class);

        // core atlas
        List<String> layout = resources.asLines(PRIMARY_ATLAS_TXT_PATH);
        Image img = resources.image(PRIMARY_ATLAS_PNG_PATH);

        // normal map

        DepthMap8 depthMap8 = new DepthMap8(img);
        depthMap8.toPNG("depthmap8.png");
        NormalMap normalMap = new NormalMap(depthMap8,1);
        normalMap.toPNG("normalmap.png");

        Texture atlas_texture = new Texture(GL_TEXTURE_2D);
        atlas_texture.bindToActiveSlot();
        atlas_texture.filter(GL_LINEAR,GL_NEAREST);
        atlas_texture.wrapST(GL_CLAMP_TO_BORDER);
        atlas_texture.image_2D(img);
        core_atlas = new TextureAtlas(atlas_texture,layout);
        blank = core_atlas.get(BLANK);
        textures = new Texture[NUM_TEXTURES];
        textures[0] = atlas_texture;
        user_assets = new HashMap<>(31);

        // duplicate -> rotate and store selected regions
        TextureRegion scrollbar_top = core_atlas.get(SCROLLBAR_TOP);
        TextureRegion scrollbar_container_top = core_atlas.get(SCROLLBAR_CONTAINER_TOP);
        TextureRegion scrollBar_bottom = scrollbar_top.cpy();
        TextureRegion scrollbar_container_bottom = scrollbar_container_top.cpy();
        scrollBar_bottom.flipY();
        scrollbar_container_bottom.flipY();
        core_atlas.put(SCROLLBAR_BOTTOM,scrollBar_bottom);
        core_atlas.put(SCROLLBAR_CONTAINER_BOTTOM,scrollbar_container_bottom);

        // amiga font
        int amiga_font_rows = 6;
        int amiga_font_cols = 16;
        int amiga_font_width = 8;
        int amiga_font_height = 16;
        int amiga_font_characters = 95;
        TextureRegion font_region = core_atlas.get(AMIGA_FONT);
        font_region.subDivide(amiga_font_cols, amiga_font_rows);
        TextureRegion[] amiga_regions = new TextureRegion[amiga_font_characters];
        for (int i = 0; i < amiga_font_characters; i++)
            amiga_regions[i] = font_region.subRegion(i);
        amiga_font = new AsciiFont(
                "amiga",
                amiga_regions,
                amiga_font_height,
                amiga_font_width
        );

        // tiny font
        font_region = core_atlas.get(TINY_FONT);
        layout = resources.asLines(TINY_FONT_GLYPH_DATA_PATH);
        tiny_font = new AsciiFont(font_region,layout);
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
        return core_atlas.regions().getOrDefault(name,blank);
    }

    public TextureRegion userAsset(String name) {
        return user_assets.get(name);
    }

    public AsciiFont amigaFont() {
        return amiga_font;
    }

    public AsciiFont tinyFont() {
        return tiny_font;
    }

    public Texture texture(int slot) {
        return textures[slot];
    }

    public Texture primaryAtlasTexture() {
        return textures[0];
    }

    public void dispose() {
        /* Todo: Don't dispose user provided textures */
        for (Texture t : textures)
            Disposable.dispose(t);
    }
}
