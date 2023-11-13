package io.github.heathensoft.jlib.gui.gfx;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 09/10/2023
 */


public class Fonts implements Disposable {

    public static final int FONT_SLOTS = 4;
    public static final int FONTS_NUM_CHARACTERS = 95;
    public static final int FONT_STD140_SIZE = 3088;
    public static final int FONTS_UNIFORM_BUFFER_SIZE = FONT_STD140_SIZE * FONT_SLOTS; //12352;

    private int currentFont;
    private int numFontsLoaded;
    private final int uniformsBindingPoint;
    private final BufferObject uniformBuffer;
    public final Texture[] font_texture;
    private final String[] font_name;
    private final boolean[] font_loaded;
    private final boolean[] font_monospaced;
    private final float[][] font_advance;
    private final float[] font_ascent;
    private final float[] font_descent;
    private final float[] font_lineGap;
    private final float[] font_maxAdvance;
    private final float[] font_avgAdvance;
    private final float[] font_sizePixels;


    public Fonts(int bindingPoint) {
        this.font_texture = new Texture[FONT_SLOTS];
        this.font_name = new String[FONT_SLOTS];
        this.font_loaded = new boolean[FONT_SLOTS];
        this.font_monospaced = new boolean[FONT_SLOTS];
        this.font_advance = new float[FONT_SLOTS][FONTS_NUM_CHARACTERS];
        this.font_ascent = new float[FONT_SLOTS];
        this.font_descent = new float[FONT_SLOTS];
        this.font_lineGap = new float[FONT_SLOTS];
        this.font_maxAdvance = new float[FONT_SLOTS];
        this.font_avgAdvance = new float[FONT_SLOTS];
        this.font_sizePixels = new float[FONT_SLOTS];
        this.uniformsBindingPoint = bindingPoint;
        this.uniformBuffer = new BufferObject(GL_UNIFORM_BUFFER,GL_STATIC_DRAW).bind();
        this.uniformBuffer.bufferData(FONTS_UNIFORM_BUFFER_SIZE);
        this.uniformBuffer.bindBufferBase(uniformsBindingPoint);
    }

    public void uploadFont(Font font, int slot) throws Exception {
        uploadFont(font.png,font.metricsString(),slot);
    }

    public void uploadFont(ByteBuffer png, String metrics, int slot) throws Exception {
        if (slot < 0 || slot >= FONT_SLOTS)
            throw new Exception("Invalid slot for fonts: " + slot + ". valid: [0-3]");
        FontMetrics fontMetrics = extractFontMetrics(metrics);
        boolean font_loaded = this.font_loaded[slot];
        Texture texture;
        {   // bake normalmap, create texture
            Bitmap font_alpha = new Bitmap(png);
            if (font_alpha.channels() > 1) {
                Bitmap grey = font_alpha.greyScale();
                font_alpha.dispose();
                font_alpha = grey;
            } Bitmap font_normals;
            if (fontMetrics.noise > 0) {
                Bitmap copy = font_alpha.copy();
                for (int r = 0; r < copy.height(); r++) {
                    for (int c = 0; c < copy.width(); c++) {
                        int index = r * copy.width() + c;
                        float cf = ((copy.pixels().get(index) & 0xFF) / 255f);
                        cf = lerp(cf,Rand.nextFloat(),fontMetrics.noise * cf);
                        byte color = (byte)(((int)(clamp(cf) * 255)) & 0xFF);
                        copy.pixels().put(index,color); }
                } font_normals = copy.normalMap(fontMetrics.protrusion);
                copy.dispose();
            } else font_normals = font_alpha.normalMap(fontMetrics.protrusion);
            Bitmap bitmap = Bitmap.combine(font_normals,font_alpha);
            font_normals.dispose();
            font_alpha.dispose();
            texture = bitmap.asTexture(fontMetrics.mipMap,false);
            texture.wrapST(fontMetrics.textureWrap); // invalid enum must be checked before this
            texture.filter(fontMetrics.minFilter,fontMetrics.magFilter);
            bitmap.dispose();
        } { // Upload Glyphs
            font_name[slot] = fontMetrics.name;
            this.font_loaded[slot] = true;
            font_monospaced[slot] = fontMetrics.monospaced;
            font_ascent[slot] = fontMetrics.ascent;
            font_descent[slot] = fontMetrics.descent;
            font_lineGap[slot] = fontMetrics.lineGap;
            font_maxAdvance[slot] = fontMetrics.maxAdvance;
            font_avgAdvance[slot] = fontMetrics.avgAdvance;
            font_sizePixels[slot] = fontMetrics.sizePixels;
            Glyph[] glyphs = fontMetrics.glyphs; // Missing glyphs replaced with space
            try (MemoryStack stack = MemoryStack.stackPush()){ // upload to GPU
                FloatBuffer fBuffer = stack.mallocFloat(FONT_STD140_SIZE / Float.BYTES);
                for (char c = 0; c < FONTS_NUM_CHARACTERS; c++) {
                    Glyph glyph = glyphs[c];
                    glyph.get(fBuffer);
                    font_advance[slot][c] = glyph.advance;
                } fontMetrics.cursor.get(fBuffer);
                fBuffer.put(slot).put(font_sizePixels[slot]);
                fBuffer.put(0).put(0).flip();
                uniformBuffer.bind();
                uniformBuffer.bufferSubData(fBuffer,slot * FONT_STD140_SIZE);
            }
        } // Dispose and switch
        if (font_loaded) Disposable.dispose(font_texture[slot]);
        else numFontsLoaded++;
        font_texture[slot] = texture;
    }

    private FontMetrics extractFontMetrics(String fontInfo) throws Exception {
        Exception exception = new Exception("Invalid font metrics descriptor");
        FontMetrics metrics = new FontMetrics();
        metrics.glyphs = new Glyph[FONTS_NUM_CHARACTERS];
        List<String> lines = fontInfo.lines().toList();
        boolean texture_entry_found = false;
        boolean font_entry_found = false;
        try { for (String line : lines) {
                line = line.trim();
                if (line.isBlank() || line.startsWith("#")) continue;
                if (line.startsWith("G")) {
                    // Font entry MUST come before glyphs
                    if (!font_entry_found) throw exception;
                    String[] split = line.split("\\s+");
                    if (split.length != 9) throw exception;
                    // G <ascii_value> <x> <y> <width> <height> <xOff> <yOff> <advance>
                    int ascii_value = Integer.parseInt(split[1]);
                    if (ascii_value < 32 || ascii_value > 126) continue;
                    int x = Integer.parseInt(split[2]);
                    int y = Integer.parseInt(split[3]);
                    int w = Integer.parseInt(split[4]);
                    int h = Integer.parseInt(split[5]);
                    TextureRegion reg = new TextureRegion(
                    x,y,w,h,metrics.width,metrics.height);
                    float offX = Float.parseFloat(split[6]);
                    float offY = Float.parseFloat(split[7]);
                    float adv = Float.parseFloat(split[8]);
                    metrics.glyphs[ascii_value - 32] = new Glyph(
                    reg,(char)ascii_value,offX,offY,adv);
                } else if (line.startsWith("F") &! font_entry_found) {
                    String[] split = line.split("\\s+");
                    if (split.length != 14) throw exception;
                    metrics.name = split[1];
                    metrics.sizePixels = Integer.parseInt(split[2]);
                    metrics.width = Integer.parseInt(split[4]);
                    metrics.height = Integer.parseInt(split[5]);
                    metrics.protrusion = Float.parseFloat(split[6]);
                    metrics.noise = Float.parseFloat(split[7]);
                    metrics.maxAdvance = Float.parseFloat(split[8]);
                    metrics.avgAdvance = Float.parseFloat(split[9]);
                    metrics.ascent = Float.parseFloat(split[10]);
                    metrics.descent = Float.parseFloat(split[11]);
                    metrics.lineGap = Float.parseFloat(split[12]);
                    metrics.monospaced = Integer.parseInt(split[13]) > 0;
                    font_entry_found = true;
                } else if (line.startsWith("T") &! texture_entry_found) {
                    String[] split = line.split("\\s+");
                    if (split.length != 5) throw exception;
                    metrics.minFilter = Integer.parseInt(split[1]);
                    metrics.magFilter = Integer.parseInt(split[2]);
                    metrics.textureWrap = Integer.parseInt(split[3]);
                    metrics.mipMap = Integer.parseInt(split[4]) > 0;
                    texture_entry_found = true;
                }
            } Glyph space = metrics.glyphs[0];
            if (space == null) throw exception;
            for (int i = 0; i < FONTS_NUM_CHARACTERS; i++) {
                if (metrics.glyphs[i] == null)
                    metrics.glyphs[i] = space;}
        } catch (NumberFormatException e) {
            throw exception;
        } // Experimental cursor (See if this works)
        TextureRegion cursorRegion;
        cursorRegion = new TextureRegion(
                0,0,round(metrics.avgAdvance),
                round(metrics.ascent + metrics.descent),
                metrics.width,metrics.height);
        metrics.cursor = new Glyph(cursorRegion,' ',
                0,-metrics.descent,metrics.avgAdvance);
        return metrics;
    }

    public void bindFontMetrics(int font) {
        if (font >= 0 && font < numFontsLoaded && font_loaded[font]) {
            this.currentFont = font;
        } else Logger.warn("Font slot: " + font + "not loaded");
    }

    public void bindUploadTextures(ShaderProgram shader, String uniform) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(numFontsLoaded);
            for (int i = 0; i < numFontsLoaded; i++) {
                if (font_loaded[i]) buffer.put(i);
            } shader.setUniform1iv(uniform,buffer.flip());
            for (int i = 0; i < numFontsLoaded; i++) {
                if (font_loaded[i]) font_texture[i].bindToSlot(i);
            }
        }
    }

    public Texture texture() {
        return font_texture[currentFont];
    }

    public String name() {
        return font_name[currentFont];
    }

    public boolean isLoaded() {
        return font_loaded[currentFont];
    }

    public boolean isFontSlotLoaded(int font) {
        if (font < 0 || font >= FONT_SLOTS) return false;
        return font_loaded[font];
    }

    public boolean isMonospaced() {
        return font_monospaced[currentFont];
    }

    public float advance(char c) {
        int index = Math.max((c & 0x7F) - 32,0);
        return font_advance[currentFont][index % FONTS_NUM_CHARACTERS];
    }

    public float ascent() {
        return font_ascent[currentFont];
    }

    public float descent() {
        return font_descent[currentFont];
    }

    public float lineGap() {
        return font_lineGap[currentFont];
    }

    public float maxAdvance() {
        return font_maxAdvance[currentFont];
    }

    public float averageAdvance() { return font_avgAdvance[currentFont]; }

    public float sizePixels() {
        return font_sizePixels[currentFont];
    }

    public float relativeScale(float sizePixels) {
        return sizePixels / sizePixels();
    }

    public int uniformsBindingPoint() {
        return uniformsBindingPoint;
    }

    public int numFontsLoaded() {
        return numFontsLoaded;
    }

    public int currentFont() { return currentFont; }

    public void dispose() {
        Disposable.dispose(font_texture);
        Disposable.dispose(uniformBuffer);
    }

    // HELPER STRUCTURES

    private static final class Glyph extends TextureRegion {
        private final float offsetX;
        private final float offsetY;
        private final float advance;
        private final char character;
        public Glyph(TextureRegion region, char character, float offsetX, float offsetY, float advance) {
            super(region);
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.advance = advance;
            this.character = character;
        } public float offsetX() { return offsetX; }
        public float offsetY() { return offsetY; }
        public float advance() { return advance; }
        public char character() { return character; }
        public String toString() { return Character.toString(character); }
        public void get(FloatBuffer buffer) {
            buffer.put(u).put(v).put(u2).put(v2);
            buffer.put(width).put(height);
            buffer.put(offsetX).put(offsetY);
        }
    }

    private static final class FontMetrics {
        Glyph[] glyphs;
        Glyph cursor; String name;
        float ascent, descent, lineGap;
        float maxAdvance, protrusion;
        float avgAdvance, noise, sizePixels;
        int width, height;
        int minFilter, magFilter;
        int textureWrap;
        boolean mipMap;
        boolean monospaced;
    }

}
