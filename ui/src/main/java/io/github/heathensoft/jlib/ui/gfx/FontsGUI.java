package io.github.heathensoft.jlib.ui.gfx;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Repository;
import io.github.heathensoft.jlib.ui.text.Paragraph;
import io.github.heathensoft.jlib.ui.text.Word;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 *
 * Fonts are stored in the shader
 * 4 fonts is max as I am using UBOs and not SSBOs
 * Could decide to change this later
 * You can upload fonts at runtime
 *
 *
 * @author Frederik Dahl
 * 09/10/2023
 */


public class FontsGUI implements Disposable {

    public static final int FONT_SLOTS = 4;
    public static final int FONTS_NUM_CHARACTERS = 95;
    public static final int FONT_STD140_SIZE = 3088;
    public static final int FONTS_UNIFORM_BUFFER_SIZE = FONT_STD140_SIZE * FONT_SLOTS; //12352 Bytes (16KB guaranteed available pr.ubo)

    public static final int SLOT_REGULAR = 0;
    public static final int SLOT_MONO = 1;
    public static final int SLOT_LORE = 2;
    public static final int SLOT_EDICT = 3;

    private static final String[] DEFAULT_FONTS = new String[FONT_SLOTS];
    private static final String DEFAULT_FONTS_PATH = "res/jlib/ui/fonts/";
    static {
        DEFAULT_FONTS[0] = "BaiJamjuree64";
        DEFAULT_FONTS[1] = "LiberationMono64";
        DEFAULT_FONTS[2] = "Gotu64";
        //DEFAULT_FONTS[3] = "Bokor64";
        //DEFAULT_FONTS[0] = "TradeWinds64";
        //DEFAULT_FONTS[1] = "UncialAntiquas64";
        //DEFAULT_FONTS[2] = "UnifrakturCook64";
        DEFAULT_FONTS[3] = "Play64";
    }

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

    private final Vector4f[][] font_colors;
    private Paragraph.Type last_pType = null;
    private Word.Type last_wType = null;
    private float last_alpha = 1.0f;
    private float last_color_float_bits = 0.0f;

    private final static Vector4f[] default_colors = initialize_default_colors();

    private static Vector4f[] initialize_default_colors() {
        Vector4f[] colors = new Vector4f[Paragraph.Type.values().length];
        colors[Paragraph.Type.DEFAULT.id] = Color.hex_to_rgb("A9B7C6FF");
        colors[Paragraph.Type.COMMENT.id] = Color.hex_to_rgb("808080FF");
        colors[Paragraph.Type.DEBUG.id]   = Color.hex_to_rgb("6A8759FF");
        colors[Paragraph.Type.WARNING.id] = Color.hex_to_rgb("FF0000FF");
        return colors;
    }

    public static Vector4f defaultColor(Paragraph.Type type) {
        return default_colors[type.id];
    }


    public FontsGUI(int bindingPoint) {
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
        this.font_colors = initialize_font_colors(colorDefault());
    }



    public void uploadDefaultFonts() throws Exception {
        for (int i = 0; i < FONT_SLOTS; i++) {
            String font_name = DEFAULT_FONTS[i];
            if (font_name != null) {
                String path = DEFAULT_FONTS_PATH + font_name + ".repo";
                Repository repo = Repository.loadFromResources(path,64 * 1024);
                BitmapFont font = repo.getFont(font_name);
                if (font != null) uploadFont(font,i);
                Disposable.dispose(font);
            }
        }
    }

    public void uploadFont(BitmapFont font, int slot) throws Exception {
        uploadFont(font.bitmap(),font.info(),slot);
    }

    public void uploadFont(Bitmap font_bitmap, String metrics, int slot) throws Exception {
        if (slot < 0 || slot >= FONT_SLOTS)
            throw new Exception("Invalid slot for fonts: " + slot + ". valid: [0-3]");
        FontMetrics fontMetrics = extractFontMetrics(metrics);
        boolean font_loaded = this.font_loaded[slot];
        Texture texture;
        {   // bake normalmap, create texture
            Bitmap font_alpha = font_bitmap;
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
            texture = bitmap.asTexture(fontMetrics.mipMap,false);
            if (fontMetrics.mipMap) { texture.generateMipmap(); }
            texture.textureWrap(fontMetrics.textureWrap); // invalid enum must be checked before this
            texture.textureFilter(fontMetrics.minFilter,fontMetrics.magFilter);
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
            if (this.currentFont != font) {
                this.currentFont = font;
                this.last_pType = null;
                this.last_wType = null;
            }
        } else Logger.warn("Font slot: " + font + "not loaded");
    }

    public void bindUploadTextures(String uniform) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(numFontsLoaded);
            for (int i = 0; i < numFontsLoaded; i++) {
                if (font_loaded[i]) buffer.put(i);
            } ShaderProgram.setUniform(uniform,buffer.flip());
            for (int i = 0; i < numFontsLoaded; i++) {
                if (font_loaded[i]) font_texture[i].bindToSlot(i);
            }
        }
    }



    private Vector4f[][] initialize_font_colors(Vector4f color) {
        Vector4f[][] colors = new Vector4f[FONT_SLOTS][Word.Type.values().length];
        for (int font = 0; font < colors.length; font++) {
            for (int type = 0; type < colors[font].length; type++)
                colors[font][type] = new Vector4f(color);
        } return colors;
    }

    public void setColor(Word.Type type,Vector4f color) {
        font_colors[currentFont][type.id].set(color);
        last_wType = null;
    }

    public void setDefaultColor(Paragraph.Type type, Vector4f color) {
        default_colors[type.id].set(color);
        last_pType = null;
    }

    public Vector4f colorDefault() {
        return default_colors[Paragraph.Type.DEFAULT.id];
    }

    public Vector4f colorRegular() {
        return font_colors[currentFont][Word.Type.REGULAR.id];
    }

    public Vector4f colorRGB(Paragraph paragraph, Word word) {
        return colorRGB(paragraph.type(),word.type());
    }

    public Vector4f colorRGB(Paragraph.Type pType, Word.Type wType) {
        if (pType == Paragraph.Type.DEFAULT) {
            return font_colors[currentFont][wType.id];
        } else if (wType == Word.Type.REGULAR) {
            return default_colors[pType.id];
        } else return font_colors[currentFont][wType.id];
    }

    public float colorRegularFloatBits() {
        return Color.rgb_to_floatBits(colorRegular());
    }

    public float colorDefaultFloatBits() {
        return Color.rgb_to_floatBits(colorDefault());
    }

    public float colorFloatBits(Paragraph paragraph, Word word) {
        return colorFloatBits(paragraph.type(),word.type());
    }

    public float colorFloatBits(Paragraph paragraph, Word word, float alpha) {
        return colorFloatBits(paragraph.type(),word.type(),alpha);
    }

    public float colorFloatBits(Paragraph.Type pType, Word.Type wType) {
        return colorFloatBits(pType,wType,1.0f);
    }

    public float colorFloatBits(Paragraph.Type pType, Word.Type wType, float alpha) {
        if (last_pType == pType && last_wType == wType && U.floatEquals(last_alpha,alpha,1e-4)) {
            return last_color_float_bits;
        } last_pType = pType;
        last_wType = wType;
        last_alpha = alpha;
        Vector4f rgb = colorRGB(pType,wType);
        if (alpha < 1.0f) {
            rgb = U.vec4(rgb);
            rgb.w *= alpha;
        } last_color_float_bits = Color.rgb_to_floatBits(rgb);
        return last_color_float_bits;
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

    public float desiredFontSize(String line, float desired_width) {
        return desiredFontSize(advanceSum(line),desired_width);
    }

    public float desiredFontSize(float sum_advance, float desired_width) {
        if (desired_width > 0 && sum_advance > 0) {
            float size_pixels = sizePixels(); // 32
            float ratio = desired_width / sum_advance;
            return size_pixels * ratio;
        } return 0;
    }

    public float advanceSum(String line) {
        if (line == null || line.isBlank()) return 0;
        float num_characters = line.length();
        float width = 0;
        if (isMonospaced()) {
            width = advance('x') * num_characters;
        } else {
            for (int i = 0; i < num_characters; i++) {
                width += advance(line.charAt(i));
            }
        } return width;
    }

    public float advanceSumSized(String line, float size) {
        return advanceSum(line) * relativeScale(size);
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

    public float lineGap() { return font_lineGap[currentFont]; }

    public float lineHeight() { return ascent() + descent() + lineGap(); }

    public float maxAdvance() {
        return font_maxAdvance[currentFont];
    }

    public float averageAdvance() { return font_avgAdvance[currentFont]; }

    public float sizePixels() {
        return font_sizePixels[currentFont];
    }

    public float relativeScale(float size) {
        return size / sizePixels();
    }

    public int uniformsBindingPoint() {
        return uniformsBindingPoint;
    }

    public int numFontsLoaded() {
        return numFontsLoaded;
    }

    public int currentFont() { return currentFont; }

    public Rectanglef cursorBounds(float x, float y, float size, Rectanglef dst) {
        float scale = relativeScale(size);
        dst.minX = x; // x = cursor left
        dst.maxX = x + (averageAdvance() * scale);
        dst.minY = y - ((ascent() + descent()) * scale);
        dst.maxY = y; // y = cursor top
        return dst;
    }

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
            buffer.put(w).put(h);
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

    /*
     * 00000000 00000000 00000000 01111111 char
     * 00000000 00000000 00000000 10000000 inverted color
     * 00000000 00000000 11111111 00000000 size
     * 00000000 11111111 00000000 00000000 glow
     * 00000011 00000000 00000000 00000000 font
     * 11111100 00000000 00000000 00000000 unused (6-bit)
     */

    public static int bits_set_char(int bits, int c) {
        return bits | (c & 0x7F);
    }
    public static int bits_invert_color(int bits) { return bits | 0x80; }
    public static int bits_set_size(int bits, float size) { return bits | ((round(clamp(size,1,256)) - 1) & 0xFF) << 8; }
    public static int bits_set_glow(int bits, float glow) {
        return bits | (round(clamp(glow) * 255.0f) & 0xFF) << 16;
    }
    public static int bits_set_font(int bits, int font) {
        return bits | (font & 0x03) << 24;
    }
    public static int bits_font(int font) {
        return (font & 0x03) << 24;
    }


}
