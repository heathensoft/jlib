package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.ExternalFile;
import io.github.heathensoft.jlib.common.io.WorkingDirectory;
import io.github.heathensoft.jlib.common.utils.RectPacker;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.*;

/**
 *
 * @author Frederik Dahl
 * 27/07/2023
 */


public class TextureAtlas { // extend texture 

    private static final int DIFFUSE_SLOT = 0;
    private static final int NORMALS_SLOT = 1;
    private static final int LIGHTING_SLOT = 2; // I.e. height, emissive, specular

    private Texture[] textures;


    public TextureAtlas(Bitmap diffuse, Bitmap normals, Bitmap lighting, String atlasInfo) throws Exception {

    }


    public static AtlasData create(String name, String directory, int spacing, int wrap, int min_filter, int mag_filter, boolean mip_map) throws Exception {
        WorkingDirectory workingDirectory = new WorkingDirectory(directory,".png");
        int region_count = workingDirectory.validFileCount();
        if (region_count == 0) throw new Exception("No valid images in directory: " + directory);
        List<String> region_names = new ArrayList<>(region_count);
        workingDirectory.getValidFiles(region_names);
        Bitmap[] region_bitmaps = new Bitmap[region_count];
        StringBuilder builder;
        Bitmap bitmap;
        try {
            IntBuffer rectangles = IntBuffer.allocate(region_count * 5);
            name = name == null || name.isBlank() ? "atlas" : name;
            spacing = Math.max(0,spacing);
            int channels = 0;
            builder = new StringBuilder(8 * 1024);
            builder.append("# https://github.com/heathensoft\n");
            builder.append("# Atlas: <name> <width> <height> <channels> <sprites>\n");
            builder.append("# Texture: <minFilter> <magFilter> <textureWrap> <mipMap>\n");
            builder.append("# Region: <x> <y> <width> <height> <name>\n\n");
            for (int i = 0; i < region_count; i++) {
                Path path = workingDirectory.resolveFile(region_names.get(i));
                ByteBuffer image_data = new ExternalFile(path).readToBuffer();
                try { region_bitmaps[i] = new Bitmap(image_data);
                } catch (Exception exception) {
                    Logger.error("Unreadable png: " + path.toString());
                    throw exception;
                } channels = Math.max(channels,region_bitmaps[i].channels());
                rectangles.put(i).put(region_bitmaps[i].width() + spacing).put(region_bitmaps[i].height() + spacing);
                MemoryUtil.memFree(image_data);
            } IntBuffer bounds = IntBuffer.allocate(2);
            RectPacker.pack(rectangles.flip(),bounds);
            bitmap = new Bitmap(bounds.get(0),bounds.get(1),channels);
            builder.append("A: ");
            builder.append(name).append(' ');
            builder.append(bounds.get(0)).append(' ');
            builder.append(bounds.get(1)).append(' ');
            builder.append(channels).append(' ');
            builder.append(region_count).append('\n');
            builder.append("T: ");
            builder.append(min_filter).append(' ');
            builder.append(mag_filter).append(' ');
            builder.append(wrap).append(' ');
            builder.append(mip_map ? 1 : 0).append("\n\n");
            for (int i = 0; i < region_count; i++) {
                int id = rectangles.get();
                int w = rectangles.get() - spacing;
                int h = rectangles.get() - spacing;
                int x = rectangles.get();
                int y = rectangles.get();
                String region_name = region_names.get(id);
                int region_name_length = region_name.length();
                region_name = region_name.replaceAll("\\s*","");
                if (region_name.length() < region_name_length) {
                    Logger.warn("Found char space in atlas region string.");
                    Logger.warn("Changed name to: " + region_name);
                } TextureRegion region = new TextureRegion(x,y,w,h,bitmap.width(),bitmap.height());
                bitmap.drawNearest(region_bitmaps[id],x,y,w,h,0,0,1,1);
                builder.append("R: ");
                builder.append(x).append(' ');
                builder.append(y).append(' ');
                builder.append(w).append(' ');
                builder.append(h).append(' ');
                builder.append(region_names.get(id)).append('\n');
            }
        } finally { Disposable.dispose(region_bitmaps);
        }
        return new AtlasData(bitmap,builder.toString());
    }


    public static final class AtlasData implements Disposable {
        private final Bitmap bitmap;
        private final String info;
        public AtlasData(Bitmap bitmap, String info) {
            this.bitmap = bitmap;
            this.info = info;
        } public void dispose() { Disposable.dispose(bitmap); }
        public Bitmap bitmap() { return bitmap; }
        public String info() { return info; }
    }



}
