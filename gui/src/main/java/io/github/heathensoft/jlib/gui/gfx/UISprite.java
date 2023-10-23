package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;

/**
 *
 * Textures disposed externally
 *
 * @author Frederik Dahl
 * 15/10/2023
 */


public class UISprite {

    private final TextureRegion textureRegion;
    private final Texture diffuseTexture;
    private final Texture normalsTexture;
    public UISprite(Texture diffuse) {
        this(diffuse,0,0, diffuse.width(), diffuse.height());
    }
    public UISprite(Texture diffuse, int x, int y, int width, int height) {
        this(diffuse,null,x,y,width,height);
    }
    public UISprite(Texture diffuse, Texture normals, int x, int y, int width, int height) {
        textureRegion = new TextureRegion(x,y,width,height,diffuse.width(),diffuse.height());
        diffuseTexture = diffuse;
        normalsTexture = normals;
    }

    boolean hasNormals() {
        return normalsTexture != null;
    }
    public TextureRegion textureRegion() {
        return textureRegion;
    }
    public Texture diffuseTexture() {
        return diffuseTexture;
    }
    public Texture normalsTexture() {
        return normalsTexture;
    }


}
