package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Defined;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_NEAREST;

/**
 * @author Frederik Dahl
 * 15/03/2023
 */


public class Palette implements Defined {

    public static final int DEFAULT_TEXTURE_SIZE = 64;
    public static final String DEFAULT_NAME = "Untitled Palette";

    private int name_size;
    private String name;
    public ArrayList<Color32> colors;

    public Palette() {
        this(DEFAULT_NAME);
    }

    public Palette(String name) {
        this.name = name;
        this.name_size = name.getBytes(StandardCharsets.US_ASCII).length;
        this.colors = new ArrayList<>();
    }

    public Palette(ByteBuffer buffer) {
        setProperties(buffer);
    }

    public Palette(List<String> hexList) {
        this(DEFAULT_NAME,hexList);
    }

    public Palette(String name, List<String> hexList) {
        this.name = name;
        this.name_size = name.getBytes(StandardCharsets.US_ASCII).length;
        this.colors = new ArrayList<>(hexList.size());
        for (String hex : hexList) {
            colors.add(new Color32(hex));
        }
    }

    public void setName(String name) {
        this.name = name;
        this.name_size = name.getBytes(StandardCharsets.US_ASCII).length;
    }

    public String name() {
        return name;
    }

    public Texture texture3D() {
        return texture3D(DEFAULT_TEXTURE_SIZE);
    }

    public Texture texture3D(int texture_size) {
        Texture texture = Texture.generate3D(texture_size, texture_size, texture_size);
        texture.bindToActiveSlot();
        texture.filter(GL_NEAREST,GL_NEAREST);
        texture.clampToEdge();
        texture.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED);
        int num_pixels = texture_size * texture_size * texture_size;
        int bytes = num_pixels * 3;
        ByteBuffer buffer = MemoryUtil.memAlloc(bytes);
        if (colors.isEmpty()) {
            byte r = (byte)Color32.ERROR.redBits();
            byte g = (byte)Color32.ERROR.greenBits();
            byte b = (byte)Color32.ERROR.blueBits();
            for (int i = 0; i < num_pixels; i++) {
                buffer.put(r).put(g).put(b);
            }
        } else {
            int num_palette_colors = colors.size();
            List<Vector3f> paletteLAB = new ArrayList<>(num_palette_colors);
            for (Color32 color : colors) {
                paletteLAB.add(color.lab(new Vector3f()));
            } Color32 closestColor = new Color32();
            Vector3f size = new Vector3f(texture_size);
            Vector3f sampleLAB = new Vector3f();
            for (int r = 0; r < texture_size; r++) {
                for (int g = 0; g < texture_size; g++) {
                    for (int b = 0; b < texture_size; b++) {
                        sampleLAB.set(b,g,r).div(size);
                        Color32.rgbToLab(sampleLAB);
                        float d_min = Float.MAX_VALUE;
                        for (int i = 0; i < num_palette_colors; i++) {
                            float d = paletteLAB.get(i).distance(sampleLAB);
                            if (d < d_min) {
                                closestColor.set(colors.get(i));
                                d_min = d;
                            }
                        } closestColor.getRGB(buffer);
                    }
                }
            }
        }
        texture.uploadData(buffer.flip());
        MemoryUtil.memFree(buffer);
        return texture;
    }

    public Texture texture1D() {
        Texture texture;
        if (colors.isEmpty()) {
            texture = Texture.generate1D(1);
            texture.bindToActiveSlot();
            texture.filter(GL_NEAREST,GL_NEAREST);
            texture.clampToEdge();
            texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
            try (MemoryStack stack = MemoryStack.stackPush()){
                IntBuffer buffer = stack.mallocInt(1);
                buffer.put(Color32.ERROR.intBits()).flip();
                texture.uploadData(buffer);
            }
        } else {
            int width = colors.size();
            texture = Texture.generate1D(width);
            texture.bindToActiveSlot();
            texture.filter(GL_NEAREST,GL_NEAREST);
            texture.clampToEdge();
            texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED);
            try (MemoryStack stack = MemoryStack.stackPush()){
                IntBuffer buffer = stack.mallocInt(width);
                for (Color32 color : colors) {
                    buffer.put(color.intBits());
                } texture.uploadData(buffer.flip());
            }
        } return texture;
    }

    @Override
    public void setProperties(ByteBuffer buffer) {
        int name_size = buffer.getShort();
        byte[] name = new byte[name_size];
        for (int i = 0; i < name_size; i++) {
            name[i] = buffer.get();
        } int num_colors = buffer.getShort();
        if (colors == null) {
            colors = new ArrayList<>(num_colors);
        } else {
            colors.clear();
            colors.ensureCapacity(num_colors);
        }for (int i = 0; i < num_colors; i++) {
            colors.add(new Color32(buffer.getInt()));
        } this.name = new String(name,StandardCharsets.US_ASCII);
        this.name_size = name_size;
    }

    @Override
    public void getProperties(ByteBuffer buffer) {
        buffer.putShort((short) name_size);
        buffer.put(this.name.getBytes(StandardCharsets.US_ASCII));
        int num_colors = colors.size();
        buffer.putShort((short) num_colors);
        for (Color32 color : colors) {
            buffer.putInt(color.intBits());
        }
    }

    @Override
    public int sizeOfProperties() {
        return 4 + name_size + colors.size() * Integer.BYTES;
    }

}
