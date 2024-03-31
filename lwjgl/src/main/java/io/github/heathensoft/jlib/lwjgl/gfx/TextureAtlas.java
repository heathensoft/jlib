package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.ExternalFile;
import io.github.heathensoft.jlib.common.io.WorkingDirectory;
import io.github.heathensoft.jlib.common.utils.RectPacker;
import io.github.heathensoft.jlib.lwjgl.window.GLContext;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.InvalidPathException;
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
    private final Map<String, TextureRegion> texture_regions;
    private final List<String> texture_region_names;
    private final int atlas_width;
    private final int atlas_height;

    /**
     * @param atlas_info A string of a text file with atlas info from packing
     * @param bitmaps One or more bitmaps of the same atlas. I.e. Diffuse, Normals, Depth etc.
     * @throws Exception If the TextureAtlas could not be generated
     */
    public TextureAtlas(String atlas_info, Bitmap ... bitmaps) throws Exception {
        if (bitmaps == null || bitmaps.length == 0 ) {
            throw new Exception("Texture Atlas: Missing Bitmaps");
        } else if (atlas_info == null || atlas_info.isBlank()) {
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
        try { List<String> lines = atlas_info.lines().toList();
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
            textures[i].textureWrap(texture_entry_wrap); // invalid enum must be checked before this
            textures[i].textureFilter(texture_entry_min_filter,texture_entry_mag_filter);
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

    public Map<String, TextureRegion> textureRegions() { return texture_regions; }

    public TextureRegion getRegion(String name) { return texture_regions.get(name); }

    public void setName(String name) { this.atlas_name = name; }

    public void dispose() { Disposable.dispose(textures); }


    /**
     * @param name Name of the Texture Atlas
     * @param directory A directory to extract all png-files from
     * @param spacing The spacing between atlas images
     * @param wrap Texture generation: texture wrap (gl_enum)
     * @param min_filter Texture generation: min-filter (gl_enum)
     * @param mag_filter Texture generation: mag-filter (gl_enum)
     * @param mip_map Texture generation: generate mip map
     * @param srgb Texture generation: srgb color space enabled
     * @return TextureAtlas Data. (Bitmap and Atlas descriptor string)
     * @throws Exception If none of the paths can be resolved as png-files.
     */
    public static AtlasData pack(String name, WorkingDirectory directory, int spacing, int wrap, int min_filter, int mag_filter, boolean mip_map, boolean srgb) throws Exception {
        return pack(name, directory.getFilePaths(new LinkedList<>(),".png"), spacing, wrap, min_filter, mag_filter, mip_map, srgb);
    }
    /**
     * @param name Name of the Texture Atlas
     * @param paths A list of paths
     * @param spacing The spacing between atlas images
     * @param wrap Texture generation: texture wrap (gl_enum)
     * @param min_filter Texture generation: min-filter (gl_enum)
     * @param mag_filter Texture generation: mag-filter (gl_enum)
     * @param mip_map Texture generation: generate mip map
     * @param srgb Texture generation: srgb color space enabled
     * @return TextureAtlas Data. (Bitmap and Atlas descriptor string)
     * @throws Exception If none of the paths can be resolved as png-files.
     */
    public static AtlasData pack(String name, List<String> paths, int spacing, int wrap, int min_filter, int mag_filter, boolean mip_map, boolean srgb) throws Exception {
        name = name == null || name.isBlank() ? "atlas" : name;
        Logger.debug("TextureAtlas Packing: \"{}\"",name);
        Set<String> unique_names = new HashSet<>();
        List<ExternalFile> region_files = new ArrayList<>(paths.size());
        List<String> region_names = new ArrayList<>(paths.size());
        for (String path : paths) {
            ExternalFile file = valid_png_or_null(path);
            if (file != null) {
                String file_name = file.name();
                int length = file_name.length();
                file_name = file_name.substring(0,length - 4); // .png
                if (unique_names.add(file_name)) {
                    region_files.add(file);
                    region_names.add(file_name);
                } else {
                    Logger.info("Name clash for image name: \"{}\"",file_name);
                    Logger.info("Skipping: {}",file.toString());
                }
            }
        }
        int region_count = region_names.size();
        if (region_count == 0) throw new Exception("No valid images to pack");
        Bitmap[] region_bitmaps = new Bitmap[region_count];
        StringBuilder builder;
        Bitmap bitmap;

        try {
            IntBuffer rectangles = IntBuffer.allocate(region_count * 5);
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

            for (int i = 0; i < region_count; i++) {
                ExternalFile file = region_files.get(i);
                ByteBuffer image_data = file.readToBuffer();
                try { region_bitmaps[i] = new Bitmap(image_data);
                } catch (Exception exception) {
                    String path = file.toString();
                    Logger.warn(exception,"unreadable png: " + path);
                    region_bitmaps[i] = new Bitmap(8,8,1);
                } channels = Math.max(channels,region_bitmaps[i].channels());
                rectangles.put(i).put(region_bitmaps[i].width() + spacing).put(region_bitmaps[i].height() + spacing);
                MemoryUtil.memFree(image_data);
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
        } finally { Disposable.dispose(region_bitmaps); }
        return new AtlasData(bitmap,builder.toString(),name);
    }

    @Deprecated
    public static AtlasData pack(String name, String directory, int spacing, int wrap, int min_filter, int mag_filter, boolean mip_map, boolean srgb) throws Exception {
        name = name == null || name.isBlank() ? "atlas" : name;
        Logger.debug("Packing TextureAtlas: \"{}\"",name);
        WorkingDirectory working_directory = new WorkingDirectory(directory);
        List<String> region_names = working_directory.getFileNames(new LinkedList<>(),"png");
        int region_count = region_names.size();
        if (region_count == 0) throw new Exception("No valid images in directory: " + directory);
        Bitmap[] region_bitmaps = new Bitmap[region_count];
        StringBuilder builder;
        Bitmap bitmap;
        try {
            IntBuffer rectangles = IntBuffer.allocate(region_count * 5);
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

    private static ExternalFile valid_png_or_null(String file_path_string) {
        ExternalFile file;
        try { file = new ExternalFile(file_path_string);
        } catch (InvalidPathException invalidPathException) {
            Logger.warn(invalidPathException);
            Logger.info("Resuming texture packing without image");
            return null;
        } if (file.isFile()) {
            String filename = file.name();
            if (filename.endsWith(".png")) { return file;
            } else { Logger.warn("Path is not a .png file: \"{}\"",file.toString());
                Logger.info("Resuming texture packing without image"); }
        } else { Logger.warn("Path is not a file: \"{}\"",file.toString());
            Logger.info("Resuming texture packing without image");
        } return null;
    }

}
