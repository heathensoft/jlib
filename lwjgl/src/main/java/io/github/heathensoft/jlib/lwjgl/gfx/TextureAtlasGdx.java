package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates TextureRegions from a .atlas file.
 * And stores them in a map with the region-name as key.
 * I am using gdx-texture-packer (I like it a lot)
 * https://github.com/crashinvaders/gdx-texture-packer-gui/releases
 * But you could also write a .atlas manually.
 * I am not using all the values in the .atlas.
 * The important values are the filename,
 * the x and y start of the region in pixels (y0 at top),
 * the regions width and height,
 * and the region name.
 * we skip the rest.
 *
 * the format:
 *
 * [file-name]
 * [region-name]
 * xy:[x],[y]
 * size:[w],[h]
 * [region-name]
 * xy:[x],[y]
 * size:[w],[h]
 * ...
 *
 * NOTE:
 *
 * When using texture arrays, i.e. one atlas-texture for each: diffuse, normals etc. it is assumed that
 * the atlas-regions match exactly for each texture. (the layouts should match)
 *
 * @author Frederik Dahl
 * 23/06/2022
 */


public class TextureAtlasGdx implements Disposable {
    
    private final Texture texture;
    private final String filename;
    private final List<String> regionNames;
    private final Map<String, TextureRegion> regions;
    //private final Map<String,TextureRegion> regions;
    
    public TextureAtlasGdx(Texture texture, List<String> layout) throws Exception {
        /* Todo: figure out proper size of map */
        this.regionNames = new ArrayList<>();
        this.regions = new HashMap<>();
        this.texture = texture;
        int p = 0;
        while (layout.get(p).isBlank()) p++;
        filename = layout.get(p++);
        List<String> keys = new ArrayList<>();
        List<Vector2i> xyList = new ArrayList<>();
        List<Vector2i> whList = new ArrayList<>();
        String[] tokens;
        String[] sub;
        for (int i = p; i < layout.size() ; i++, p++) {
            String line = layout.get(i);
            if (line.isBlank()) continue;
            tokens = line.split(":");
            if (tokens.length == 1) break;
        }
        try {
            for (int i = p; i < layout.size(); i++) {
                String line = layout.get(i).replace(" ", "");
                tokens = line.split(":");
                if (tokens[0].isBlank()) continue;
                if (tokens.length == 1)
                    keys.add(tokens[0]);
                switch (tokens[0]){
                    case "xy":
                        sub = tokens[1].split(",");
                        int x = Integer.parseInt(sub[0]);
                        int y = Integer.parseInt(sub[1]);
                        xyList.add(new Vector2i(x,y));
                        break;
                    case "size":
                        sub = tokens[1].split(",");
                        int w = Integer.parseInt(sub[0]);
                        int h = Integer.parseInt(sub[1]);
                        whList.add(new Vector2i(w,h));
                        break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new Exception(e);
        }
        if (keys.size() != xyList.size() && keys.size() != whList.size())
            throw new Exception("Atlas formatting Error");
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Vector2i xy = xyList.get(i);
            Vector2i wh = whList.get(i);
            TextureRegion region = new TextureRegion(xy.x, xy.y, wh.x, wh.y,texture.width(),texture.height());
            regions.put(key,region);
            regionNames.add(key);
        }
    }
    
    public Map<String, TextureRegion> regions() {
        return regions;
    }
    
    public TextureRegion get(String key) {
        return regions.get(key);
    }

    public void put(String key, TextureRegion region) {
        regions.put(key,region);
    }
    
    public List<String> regionNames() {
        return regionNames;
    }
    
    public String filename() {
        return filename;
    }
    
    public Texture texture() {
        return texture;
    }
    
    @Override
    public void dispose() {
        Disposable.dispose(texture);
    }
}
