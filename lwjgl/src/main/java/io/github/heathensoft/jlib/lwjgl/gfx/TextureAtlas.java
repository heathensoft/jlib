package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.ExternalFile;
import io.github.heathensoft.jlib.common.io.WorkingDirectory;
import io.github.heathensoft.jlib.common.utils.RectPacker;
import io.github.heathensoft.jlib.lwjgl.window.GLContext;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

/**
 *
 * @author Frederik Dahl
 * 27/07/2023
 */


public class TextureAtlas implements Disposable {

    private String atlas_name;
    private final Texture[] textures;
    private final Map<String,TextureRegion> texture_regions;
    private final List<String> texture_region_names;
    private final int atlas_width;
    private final int atlas_height;

    public TextureAtlas(String atlasInfo, Bitmap ... bitmaps) throws Exception {
        if (bitmaps == null || bitmaps.length == 0 ) {
            throw new Exception("Texture Atlas: Missing Bitmaps");
        } else if (atlasInfo == null || atlasInfo.isBlank()) {
            throw new Exception("Texture Atlas: Missing Atlas Info");
        }
        int texture_entry_min_filter = GL_LINEAR;
        int texture_entry_mag_filter = GL_LINEAR;
        int texture_entry_wrap = GL_CLAMP_TO_EDGE;
        boolean texture_entry_found = false;
        boolean texture_entry_allocate_mip_map = false;
        boolean texture_entry_srgb_to_linear = false;

        int atlas_entry_sprites = 0;
        int atlas_entry_texture_width = 0;
        int atlas_entry_texture_height = 0;
        boolean atlas_entry_found = false;
        String atlas_entry_name = null;

        List<TextureRegion> region_list = new LinkedList<>();
        List<String> region_names_list = new LinkedList<>();
        Set<String> region_names_set = new HashSet<>(128);

        Logger.debug("Texture Atlas: Extracting Sprite Data...");
        int line_index = 0;
        String parse_exception = "Texture Atlas: parse exception at line: ";
        try { List<String> lines = atlasInfo.lines().toList();
            for (String line : lines) {
            line = line.trim();
            if (line.isBlank() || line.startsWith("#")) {
                line_index++; continue;
            } String[] split = line.split("\\s+");
             if (line.startsWith("R")) {
                if (atlas_entry_found) {
                    if (atlas_entry_texture_width <= 0) {
                        Logger.warn("Texture Atlas: texture entry width <= 0");
                        atlas_entry_texture_width = 1;
                    } if (atlas_entry_texture_height <= 0) {
                        Logger.warn("Texture Atlas: texture entry height <= 0");
                        atlas_entry_texture_height = 1;
                    } if (split.length != 6) throw new Exception(parse_exception + line_index);
                    int x = Integer.parseInt(split[1]);
                    int y = Integer.parseInt(split[2]);
                    int w = Integer.parseInt(split[3]);
                    int h = Integer.parseInt(split[4]);
                    String region_name = split[5];
                    if (region_names_set.add(region_name)) {
                        region_names_list.add(split[5]);
                        region_list.add(new TextureRegion(x,y,w,h,atlas_entry_texture_width,atlas_entry_texture_height));
                    } else Logger.warn("Texture Atlas: Duplicate Region Name: {}, at line: {}, ignored", region_name,line_index);
                } else throw new Exception(parse_exception + line_index + ", Missing Atlas Entry");
            } else if (line.startsWith("T") &! texture_entry_found) {
                 if (split.length != 6) throw new Exception(parse_exception + line_index);
                 int nin_filter = Integer.parseInt(split[1]);
                 int nag_filter = Integer.parseInt(split[2]);
                 int texture_wrap = Integer.parseInt(split[3]);
                 texture_entry_allocate_mip_map = Integer.parseInt(split[4]) > 0;
                 texture_entry_srgb_to_linear = Integer.parseInt(split[5]) > 0;
                 if (!Texture.glFilterEnumIsValid(nin_filter)) {
                     Logger.warn("Texture Atlas: invalid min filter enum at line: {}",line_index);
                     Logger.warn("Using GL_LINEAR");
                 } else texture_entry_min_filter = nin_filter;
                 if (!Texture.glFilterEnumIsValid(nag_filter)) {
                     Logger.warn("Texture Atlas: invalid mag filter enum at line: {}",line_index);
                     Logger.warn("Using GL_LINEAR");
                 } else texture_entry_mag_filter = nag_filter;
                 if (!Texture.glWrapEnumIsValid(texture_wrap)) {
                     Logger.warn("Texture Atlas: invalid wrap enum at line: {}",line_index);
                     Logger.warn("Using GL_CLAMP_TO_EDGE");
                 } else texture_entry_wrap = texture_wrap;
                texture_entry_found = true;
            } else if (line.startsWith("A") &! atlas_entry_found) {
                 if (split.length != 5) throw new Exception(parse_exception + line_index);
                 atlas_entry_name = split[1];
                 atlas_entry_texture_width = Integer.parseInt(split[2]);
                 atlas_entry_texture_height = Integer.parseInt(split[3]);
                 atlas_entry_sprites = Integer.parseInt(split[4]);
                atlas_entry_found = true;
            } line_index++;
        }
        } catch (NumberFormatException e) {
            throw new Exception(parse_exception + line_index, e);
        }

        int num_sprites_extracted = region_list.size();
        if (num_sprites_extracted == 0) {
            throw new Exception("Texture Atlas: unable to extract any sprites");
        }
        this.texture_region_names = new ArrayList<>(num_sprites_extracted);
        this.texture_regions = new HashMap<>((int)(num_sprites_extracted / 0.7f));
        this.atlas_width = atlas_entry_texture_width;
        this.atlas_height = atlas_entry_texture_height;
        this.atlas_name = atlas_entry_name;

        Logger.debug("Texture Atlas: \"{}\", Extracted {}/{} sprites, Width: {}, Height: {}",atlas_name,num_sprites_extracted,atlas_entry_sprites,atlas_width,atlas_height);
        Logger.debug("Texture Wrap: {}, Min Filter: {}, Mag Filter: {}, Srgb to linear: {}", Texture.glWrapEnumToString(texture_entry_wrap),
                Texture.glFilterEnumToString(texture_entry_min_filter),Texture.glFilterEnumToString(texture_entry_mag_filter),texture_entry_srgb_to_linear);

        for (int i = 0; i < num_sprites_extracted; i++) {
            TextureRegion region = region_list.get(i);
            String name = region_names_list.get(i);
            Logger.debug("Region[{}]--> \"{}\": (x: {}, y: {}, w: {}, h: {})",i,name,region.x(),region.y(),region.w(),region.h());
            this.texture_region_names.add(name);
            this.texture_regions.put(name,region);
        } Collections.sort(this.texture_region_names);

        Logger.debug("Texture Atlas: Generating x{} Textures...",bitmaps.length);
        this.textures = new Texture[bitmaps.length];
        for (int i = 0; i < bitmaps.length; i++) {
            Bitmap bitmap = bitmaps[i];
            // Only 4 channel bitmaps are converted from srgb to linear if option is set
            boolean srgb = bitmap.channels() > 3 && texture_entry_srgb_to_linear;
            Logger.debug("Allocating Texture[{}]: Width: {}, Height: {}, Channels: {}, SRGB Format: {}",
                    i,bitmap.width(),bitmap.height(),bitmap.channels(),srgb);
            textures[i] = bitmap.asTexture(texture_entry_allocate_mip_map,srgb);
            if (texture_entry_allocate_mip_map) {
                Logger.debug("Generating Mip Map");
                textures[i].generateMipmap();
            }
            GLContext.checkError();
        }
        Logger.info("Texture Atlas: \"{}\" Extraction Complete", atlas_name);
    }

    public Texture texture(int index) { return textures[index % textures.length]; }

    public Texture[] textures() { return textures; }

    public String name() { return atlas_name; }

    public int atlasWidth() { return atlas_width; }

    public int atlasHeight() { return atlas_height; }

    public int numTextures() { return textures.length; }

    public List<String> listOfRegionNames() { return texture_region_names; }

    public Map<String,TextureRegion> textureRegions() { return texture_regions; }

    public TextureRegion getRegion(String name) { return texture_regions.get(name); }

    public void setName(String name) { this.atlas_name = name; }

    public void dispose() { Disposable.dispose(textures); }

    public static AtlasData pack(String name, String directory, int spacing, int wrap, int min_filter, int mag_filter, boolean mip_map, boolean srgb) throws Exception {
        WorkingDirectory working_directory = new WorkingDirectory(directory);
        List<String> region_names = working_directory.getFileNames(new LinkedList<>(),"png");
        int region_count = region_names.size();
        if (region_count == 0) throw new Exception("No valid images in directory: " + directory);
        Bitmap[] region_bitmaps = new Bitmap[region_count];
        StringBuilder builder;
        Bitmap bitmap;
        try {
            IntBuffer rectangles = IntBuffer.allocate(region_count * 5);
            name = name == null || name.isBlank() ? "atlas" : name;
            spacing = Math.max(0,spacing);
            int channels = 0;

            builder = new StringBuilder(8 * 1024);
            builder.append("# https://github.com/heathensoft\n\n");
            builder.append("# GL_NEAREST = 9728\n");
            builder.append("# GL_LINEAR  = 9729\n");
            builder.append("# GL_NEAREST_MIPMAP_NEAREST = 9984\n");
            builder.append("# GL_LINEAR_MIPMAP_NEAREST  = 9985\n");
            builder.append("# GL_NEAREST_MIPMAP_LINEAR  = 9986\n");
            builder.append("# GL_LINEAR_MIPMAP_LINEAR   = 9987\n\n");
            builder.append("# GL_REPEAT = 10497\n");
            builder.append("# GL_CLAMP_TO_EDGE = 33071\n");
            builder.append("# GL_CLAMP_TO_BORDER = 33069\n");
            builder.append("# GL_MIRRORED_REPEAT = 33648\n\n");
            builder.append("# Atlas: <name> <width> <height> <sprites>\n");
            builder.append("# Texture: <minFilter> <magFilter> <textureWrap> <mipMap> <srgb>\n");
            builder.append("# Region: <x> <y> <width> <height> <name>\n\n");

            for (int i = 0; i < region_names.size(); i++) {
                Optional<ExternalFile> optional = working_directory.resolveFile(region_names.get(i));
                if (optional.isPresent()) {
                    ExternalFile file = optional.get();
                    ByteBuffer image_data = file.readToBuffer();
                    try { region_bitmaps[i] = new Bitmap(image_data);
                    } catch (Exception exception) {
                        String path = file.toString();
                        Logger.error("Unreadable png: " + path);
                        throw exception;
                    } channels = Math.max(channels,region_bitmaps[i].channels());
                    rectangles.put(i).put(region_bitmaps[i].width() + spacing).put(region_bitmaps[i].height() + spacing);
                    MemoryUtil.memFree(image_data);
                } else throw new Exception("missing file: " + region_names.get(i));
            }

            IntBuffer bounds = IntBuffer.allocate(2);
            RectPacker.pack(rectangles.flip(),bounds);
            bitmap = new Bitmap(bounds.get(0),bounds.get(1),channels);
            builder.append("A: ");
            builder.append(name).append(' ');
            builder.append(bounds.get(0)).append(' ');
            builder.append(bounds.get(1)).append(' ');
            builder.append(region_count).append('\n');
            builder.append("T: ");
            builder.append(min_filter).append(' ');
            builder.append(mag_filter).append(' ');
            builder.append(wrap).append(' ');
            builder.append(mip_map ? 1 : 0).append(' ');
            builder.append(srgb ? 1 : 0).append("\n\n");
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
                //builder.append(region_names.get(id)).append('\n');
                builder.append(region_name.replace(".png","")).append('\n');
            }
        } finally { Disposable.dispose(region_bitmaps);
        } return new AtlasData(bitmap,builder.toString(),name);
    }

    public static final class AtlasData implements Disposable {
        private final Bitmap bitmap;
        private final String info;
        private final String name;
        public AtlasData(Bitmap bitmap, String info, String name) {
            this.bitmap = bitmap;
            this.name = name;
            this.info = info;
        } public void dispose() { Disposable.dispose(bitmap); }
        public Bitmap bitmap() { return bitmap; }
        public String info() { return info; }
        public String name() { return name; }
        public void exportTo(String folderPath) throws IOException {
            ExternalFile folder =  new ExternalFile(folderPath);
            ExternalFile info_file = folder.resolve(name+".txt");
            ExternalFile png_file = folder.resolve(name+".png");
            info_file.write(info);
            bitmap.compressToDisk(png_file.toString());
        }
    }



}
