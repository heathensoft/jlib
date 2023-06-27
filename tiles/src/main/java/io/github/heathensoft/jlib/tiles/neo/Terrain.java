package io.github.heathensoft.jlib.tiles.neo;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureFormat;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 21/06/2023
 */

/* TODO: Read up on SRGB again: No need to use SRGB before i use lighting. Set diffuse to SRGB after that */
/* TODO: Generate mipmaps after altering the blend map?  */
/* TODO: I really should use mipmaps. I allocate mipmaps anyway */

public class Terrain implements Disposable {

    private final Texture blend_map;
    private final Texture texture_array_diffuse;
    private final int terrain_texture_size;


    public Terrain(MapSize mapSize, int terrain_texture_size) {
        blend_map = Texture.generate2D(mapSize.length_tiles);
        blend_map.bindToActiveSlot();
        blend_map.allocate(TextureFormat.RGBA4_UNSIGNED_NORMALIZED,true);
        blend_map.filter(GL_LINEAR_MIPMAP_LINEAR,GL_LINEAR);
        blend_map.clampToEdge();

        texture_array_diffuse = Texture.generate2DArray(terrain_texture_size,5);
        texture_array_diffuse.bindToActiveSlot();
        texture_array_diffuse.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED,true);
        texture_array_diffuse.filter(GL_LINEAR_MIPMAP_LINEAR,GL_NEAREST);
        texture_array_diffuse.repeat();

        this.terrain_texture_size = terrain_texture_size;
    }

    public Texture blend_map() {
        return blend_map;
    }

    public Texture texture_layers_diffuse() {
        return texture_array_diffuse;
    }

    public void upload_layer_diffuse(Bitmap layer_texture, TerrainType type) {
        texture_array_diffuse.bindToActiveSlot();
        texture_array_diffuse.uploadSubData(layer_texture.data(),0,terrain_texture_size,terrain_texture_size,1,0,0,type.id);
    }

    public void generate_mipmaps() {
        texture_array_diffuse.generateMipmap();
    }

    public void dispose() {
        Disposable.dispose(texture_array_diffuse,blend_map);
    }
}
