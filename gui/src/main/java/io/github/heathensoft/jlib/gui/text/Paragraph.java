package io.github.heathensoft.jlib.gui.text;



import io.github.heathensoft.jlib.gui.gfx.Fonts;
import io.github.heathensoft.jlib.gui.gfx.TextBatchGUI;
import io.github.heathensoft.jlib.lwjgl.gfx.Color;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

import static io.github.heathensoft.jlib.common.utils.U.clamp;
import static io.github.heathensoft.jlib.common.utils.U.round;

/**
 * @author Frederik Dahl
 * 18/10/2023
 */


public class Paragraph implements Iterable<Word> {

    public enum Type {
        REGULAR(Color.hex_to_rgb("A9B7C6FF",new Vector4f()),"Regular"),
        COMMENT(Color.hex_to_rgb("808080FF",new Vector4f()),"Comment"),
        DEBUG(  Color.hex_to_rgb("6A8759FF",new Vector4f()),"Debug"),
        WARNING(Color.hex_to_rgb("FF0000FF",new Vector4f()),"Warning");
        public final Vector4f color;
        public final String name;
        Type(Vector4f color, String name) {
            this.color = color;
            this.name = name;
        }
    }

    protected Type type;
    protected LinkedList<Word> words;
    public Paragraph() { this((String) null); }
    public Paragraph(Type type) { this((String) null,type); }
    public Paragraph(String text) { this(text, Type.REGULAR); }
    public Paragraph(String text, Type type) {
        this.words = stringToWords(text,type);
        this.type = type;
    }

    protected Paragraph(LinkedList<Word> words, Type type) {
        this.words = words;
        this.type = type;
    }

    public Paragraph copy() {
        Paragraph copy = new Paragraph(type);
        for (Word word : words) {
            copy.words.addLast(word.copy());
        } return copy;
    }

    public Paragraph appendValue(String string) { return append(string, Keyword.Type.VALUE); }
    public Paragraph appendKeyword(String string) { return append(string, Keyword.Type.KEYWORD); }
    public Paragraph appendComment(String string) { return append(string, Keyword.Type.INLINE_COMMENT); }
    public Paragraph appendEntityPlayer(String string) { return append(string, Keyword.Type.ENTITY_PLAYER); }
    public Paragraph appendEntityOther(String string) { return append(string, Keyword.Type.ENTITY_OTHER); }
    public Paragraph appendLocation(String string) { return append(string, Keyword.Type.LOCATION); }
    public Paragraph appendObject(String string) { return append(string, Keyword.Type.OBJECT); }
    public Paragraph appendItem(String string) { return append(string, Keyword.Type.ITEM); }
    public Paragraph appendAction(String string) { return append(string, Keyword.Type.ACTION); }
    public Paragraph appendSuccess(String string) { return append(string, Keyword.Type.SUCCESS); }
    public Paragraph appendFailure(String string) { return append(string, Keyword.Type.FAILURE); }

    public Paragraph append(String string, Keyword.Type type) {
        if (string != null && !string.isBlank()) {
            String[] split = string.trim().split("\\s+");
            for (String s : split) words.addLast(new Keyword(s,type));
        } return this;
    }

    public Paragraph append(String string) {
        if (string != null && !string.isBlank()) {
            String[] split = string.trim().split("\\s+");
            for (String s : split) words.addLast(new Word(s));
        } return this;
    }

    public Paragraph append(Paragraph paragraph) {
        for (Word word : paragraph) {
            words.addLast(word);
        } return this;
    }

    public void setType(Type type) { this.type = type; }
    public LinkedList<Word> words() { return words; }
    public Type type() { return type; }
    public int wordCount() { return words.size(); }
    public int length() { return calculateLength(); }
    public boolean isBlank() { return words.isEmpty(); }

    public byte charAt(int index) throws IndexOutOfBoundsException {
        int p = 0;
        int num_words = words.size();
        for (Word w : words) {
            p += w.length();
            if (p > index) {
                return w.get(w.length() - p + index);
            } else if (p == index) {
                return 32;
            } else p++;
        } throw new IndexOutOfBoundsException("index: " + index + " length: " + length());
    }

    public Paragraph[] split(int index) {
        int length = length();
        if (index > length || index < 0) {
            throw new IndexOutOfBoundsException("index: " + index + " length: " + length);
        } else if (index == 0 || index == length) {
            return new Paragraph[] {this};
        } else { int p = 0;
            LinkedList<Word> l1 = new LinkedList<>();
            LinkedList<Word> l2 = new LinkedList<>();
            boolean split = false;
            for (Word word : words) {
                p += word.length();
                if (p > index) {
                    if (split) {
                        l2.addLast(word);
                    } else {
                        int local = word.length() - p + index;
                        Word[] split_words = word.split(local);
                        l1.addLast(split_words[0]);
                        l2.addLast(split_words[1]);
                        split = true;
                    }
                } else if (p == index) {
                    l1.addLast(word);
                    split = true;
                } else { p++;
                    l1.addLast(word);
                }
            }
            Paragraph[] result = new Paragraph[2];
            result[0] = new Paragraph(l1,type);
            result[1] = new Paragraph(l2,type);
            return result;
        }
    }

    public boolean insert(int c, int index) { return insert((byte)(c & 0xFF), index); }
    public boolean insert(char c, int index) { return insert((byte)(c & 0xFF), index); }
    public boolean insert(byte c, int index) {
        if (inRange(c)) {
            int length = length();
            if (index > length || index < 0)
                throw new IndexOutOfBoundsException("index: " + index + " length: " + length);
            if (index == length) return append(c);
            int word_count = wordCount();
            Word[] words = this.words.toArray(new Word[word_count]);
            for (int i = 0, p = 0; i < word_count; i++) {
                Word word = words[i];
                p += word.length();
                if (p > index) {
                    int local = word.length() - p + index;
                    if (c == 32) {
                        if (local == 0 || word.get(local) == 32)
                            return false;
                        Word[] split = word.split(local);
                        this.words.remove(i);
                        for (int j = 0; j < split.length; j++)
                            this.words.add(i + j, split[j]);
                    } else  word.insert(c,local);
                    return true;
                } else if (p == index) {
                    if (c == 32) return false;
                    word.append(c);
                    return true;
                } else p++;
            }
        } return false;
    }

    public boolean append(char c) { return append((byte) (c & 0xFF)); }
    public boolean append(int c) { return append((byte) (c & 0xFF)); }
    public boolean append(byte c) {
        if (inRange(c)) {
            if (words.size() > 0) {
                Word last = words.peekLast();
                if (c == 32) {
                    if (last.lastChar() != 32) {
                        last.append(c);
                        return true;
                    }
                } else {
                    if (last.lastChar() == 32) {
                        last.trim();
                        if (last.length() == 0) {
                            words.removeLast();
                        } words.addLast(new Word(c));
                    } else { last.append(c);
                    } return true;
                }
            } else if (c != 32){
                String s = Character.toString((char) c);
                words.addLast(new Word(s));
                return true;
            }
        } return false;
    }

    public void delete(int index) {
        int length = length();
        if (index >= length || index < 0) {
            throw new IndexOutOfBoundsException("index: " + index + " length: " + length);
        } int word_count = wordCount();
        Word[] words = this.words.toArray(new Word[word_count]);
        for (int i = 0, p = 0; i < word_count; i++) {
            Word word = words[i];
            p += word.length();
            if (p > index) {
                int local = word.length() - p + index;
                if (word.length() == 1) {
                    if (word_count == 1) {
                        this.words.clear();
                    } else {
                        if (i == (word_count - 1)) {
                            this.words.removeLast();
                            assert this.words.peekLast() != null;
                            this.words.peekLast().append((byte)' ');
                        } else this.words.remove(i);
                    }
                } else word.remove(local);
                 break;
            } else if (p == index) {
                word.append(words[i+1]);
                this.words.remove(i+1);
                break;
            } else p++;
        }
    }

    public Paragraph trim() {
        if (words.size() > 0) {
            Word first = words.peekFirst();
            first.trim();
            if (first.length() == 0) {
                words.removeFirst();
                if (words.size() > 0) {
                    Word last = words.peekLast();
                    last.trim();
                    if (last.length() == 0) {
                        words.removeLast();
                    }
                }
            } else {
                if (words.size() > 1) {
                    Word last = words.peekLast();
                    last.trim();
                    if (last.length() == 0) {
                        words.removeLast();
                    }
                }
            }
        }
        return this;
    }

    public void toString(StringBuilder sb) {
        if (!isBlank()) {
            for (Word word : words) {
                sb.append(word).append(' ');
            } sb.deleteCharAt(sb.length() - 1);
        }
    }

    public String toString() {
        if (isBlank()) return "";
        StringBuilder sb = new StringBuilder(length() + 1);
        for (Word word : words) {
            sb.append(word).append(' ');
        } return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public float desiredWidth(Fonts fonts, float height) {
        return widthPixels(fonts) * fonts.relativeScale(height);
    }

    public float widthPixels(Fonts fonts) {
        if (!isBlank()) {
            float space = fonts.advance(' ');
            float width = 0;
            for (Word w : words) {
                width += w.widthPixels(fonts);
                width += space;
            } width -= space;
            return width;
        } return 0;
    }

    public Iterator<Word> iterator() { return words.iterator(); }


    public void drawFixedSize(TextBatchGUI batch, float x, float y, float width, float size, float alpha) {
        Fonts fonts = batch.fonts();
        float scale = fonts.relativeScale(size);
        float width_pixels = widthPixels(fonts);
        float desired_width = width_pixels * scale;
        float space = scale * fonts.advance(' ');
        float bounds_x = x + width;

        int info_bits = (fonts.currentFont() << 29);
        info_bits |= (((round(size) - 1) & 0xFF) << 21);
        if (this instanceof ColoredLine custom) {
            float glow = clamp(custom.glow());
            info_bits |= ((round(glow * 127.0f) & 0x7F) << 13);
        }
        Vector4f color_rgb = MathLib.vec4();
        Vector4f prev_color = null;
        y -= fonts.ascent() * scale;
        float color_floatBits = 0;
        for (Word word : words) {
            Vector4f word_color = colorOf(word);
            if (word_color != prev_color) {
                color_rgb.set(word_color);
                color_rgb.w *= alpha;
                prev_color = word_color;
                color_floatBits = Color.rgb_to_floatBits(color_rgb);
            } byte[] bytes = word.get();
            for (byte b : bytes) {
                char character = (char) (b & 0xFF);
                float x2 = x + fonts.advance(character) * scale;
                if (x2 <= bounds_x) batch.pushVertex(x, y, color_floatBits,info_bits | character);
                else return;
                x = x2;
            } x += space;
        }
    }

    public void drawDynamicSize(TextBatchGUI batch, float x, float y, float width, float size, boolean centered, float alpha) {
        Fonts fonts = batch.fonts();
        float scale = fonts.relativeScale(size);
        float width_pixels = widthPixels(fonts);
        float desired_width = width_pixels * scale;
        float ratio = width / desired_width;
        y -= fonts.ascent() * scale;
        if (ratio < 1) { size = size * ratio;
            scale = fonts.relativeScale(size);
            desired_width = width_pixels * scale;
        } if (centered) x += TextUtils.centeredOffsetX(fonts,desired_width,width);
        if (size >= 1f) { float space = scale * fonts.advance(' ');

            int info_bits = (fonts.currentFont() << 29);
            info_bits |= (((round(size) - 1) & 0xFF) << 21);
            if (this instanceof ColoredLine custom) {
                float glow = clamp(custom.glow());
                info_bits |= ((round(glow * 127.0f) & 0x7F) << 13);
            }
            Vector4f color_rgb = MathLib.vec4();
            Vector4f prev_color = null;
            float color_floatBits = 0;
            for (Word word : words) {
                Vector4f word_color = colorOf(word);
                if (word_color != prev_color) {
                    color_rgb.set(word_color);
                    color_rgb.w *= alpha;
                    prev_color = word_color;
                    color_floatBits = Color.rgb_to_floatBits(color_rgb);
                } byte[] bytes = word.get();
                for (byte b : bytes) {
                    char character = (char) (b & 0xFF);
                    batch.pushVertex(x, y, color_floatBits,info_bits | character);
                    x += fonts.advance(character) * scale;
                } x += space;
            }
        }

    }

    public float calculateHeight(Fonts fonts, int size, float width, boolean wrap) {
        if (!isBlank()) {
            float scale = fonts.relativeScale(size);
            float ascent = fonts.ascent() * scale;
            float descent = fonts.descent() * scale;
            if (wrap) {
                float gap = fonts.lineGap() * scale;
                float space = fonts.advance(' ') * scale;
                float line_height = ascent + descent;
                float x = 0, y = line_height;
                for (Word word : words) {
                    float word_width = word.length() * scale;
                    x += word_width;
                    if (x > width) {
                        y += (gap + line_height);
                        x = 0;
                    } x += space;
                }
            } else return (ascent + descent);
        } return 0;
    }

    protected LinkedList<Word> stringToWords(String line, Type type) {
        LinkedList<Word> list = new LinkedList<>();
        if (line != null && !line.isBlank()) {
            String[] split_paragraph = line.trim().split("\\s+");
            switch (type) {
                case REGULAR, COMMENT -> {
                    for (String word : split_paragraph)
                        list.add(new Word(word));
                } case DEBUG, WARNING -> {
                    for (String word : split_paragraph) {
                        if (Character.isDigit(word.charAt(0))) {
                            // Possible Hexadecimal
                            if (word.charAt(0) == '0' && word.length() > 3) {
                                if (word.charAt(1) == 'x') {
                                    String hex = word.substring(2,word.length()-1);
                                    if (TextUtils.isHexadecimal(hex)) {
                                        hex = "0x"+hex.toUpperCase();
                                        list.add(new Keyword(hex, Keyword.Type.VALUE));
                                        continue;
                                    }
                                }
                            }
                            String number = word;
                            Pattern p = Pattern.compile("[\\S]+[\\.,:%]");
                            if (p.matcher(word).matches()) { // ends on . , : or %
                                number = word.substring(0,word.length() - 1);
                                if (number.endsWith("%")) {
                                    number = number.substring(0,number.length() - 1);
                                }
                            }
                            if (TextUtils.isNumeric(number)) {
                                list.add(new Keyword(word, Keyword.Type.VALUE));
                                continue;
                            }
                        }
                        list.add(new Word(word));
                    }
                }
            }
        } return list;
    }

    protected boolean inRange(byte c) { return (c >= 32 && c < 127); }


    protected int calculateLength() {
        if (isBlank()) return 0;
        int length = 0;
        for (Word word : words) {
            length += (word.length() + 1);
        } return length - 1;
    }


    protected Vector4f colorOf(Word word) {
        if (this instanceof ColoredLine custom)
            return custom.color();
        Word.Type wordType = word.type();
        if (wordType == Word.Type.REGULAR) {
            return type.color;
        } else return wordType.color;
    }


    /*

    public void drawWrap(TextBatch batch, float x, float y, float width, int size) {
        Fonts fonts = batch.fonts();
        float scale = fonts.relativeScale(size);
        float space = scale * fonts.advance(' ');
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float gap = fonts.lineGap() * scale;
        float line_height = ascent + descent + gap;
        float pointer_y = ascent;
        float pointer_x = 0;
        float glow = 0;
        boolean transparent = false;
        if (this instanceof CustomColored custom) {
            transparent = custom.transparent();
            glow = clamp(custom.glow());
        } int base_info = transparent ? 0x8000_0000 : 0;
        base_info |= (fonts.currentFont() << 29);
        base_info |= (((size - 1) & 0xFF) << 21);
        //base_info |= (1 << 13); //invert color here
        base_info |= ((round(glow * 127.0f) & 0x7F) << 12);
        for (Word word : words) {
            float word_width = word.widthPixels(fonts) * scale;
            float pointer_x_next = pointer_x + word_width;
            if (pointer_x_next > width) {
                pointer_y += line_height;
                pointer_x = 0;
                if (word_width > width) continue;
            } int info = base_info;
            info |= ((colorIndexOf(word) & 0x3F) << 7);
            byte[] bytes = word.get();
            for (byte b : bytes) {
                char c = (char) (b & 0xFF);
                batch.pushVertex(x + pointer_x, y - pointer_y, info | c);
                pointer_x += fonts.advance(c) * scale;
            } pointer_x += space;

        }

    }
    */



        /*
    public void drawNoWrap(TextBatch batch, float x, float y, float width, int size) {
        Fonts fonts = batch.fonts();
        float desired_width = widthPixels(fonts) * fonts.relativeScale(size);
        if (desired_width > width) {
            float scale = fonts.relativeScale(size);
            float space = scale * fonts.advance(' ');
            float glow = 0;
            boolean transparent = false;
            if (this instanceof CustomColored custom) {
                transparent = custom.transparent();
                glow = clamp(custom.glow());
            } int base_info = transparent ? 0x8000_0000 : 0;
            base_info |= (fonts.currentFont() << 29);
            base_info |= (((size - 1) & 0xFF) << 21);
            //base_info |= (1 << 13); //invert color here
            base_info |= ((round(glow * 127.0f) & 0x7F) << 12);
            y -= fonts.ascent() * scale;
            float bounds = x + width;
            for (Word word : words) {
                int info = base_info;
                info |= ((colorIndexOf(word) & 0x3F) << 7);
                byte[] bytes = word.get();
                for (byte b : bytes) {
                    char c = (char) (b & 0xFF);
                    float x2 = x + fonts.advance(c) * scale;
                    if (x2 <= bounds) {
                        batch.pushVertex(x, y, info | c);
                    } else return;
                    x = x2;
                } x += space;
            }
        } else draw(batch, x, y, size);
    }


    public void drawWrap(TextBatch batch, Rectanglef quad, int size) {
        Fonts fonts = batch.fonts();
        float scale = fonts.relativeScale(size);
        float space = scale * fonts.advance(' ');
        float ascent = fonts.ascent() * scale;
        float descent = fonts.descent() * scale;
        float gap = fonts.lineGap() * scale;
        float line_height = ascent + descent + gap;
        float width = quad.lengthX();
        float height = quad.lengthY();
        float x = quad.minX;
        float y = quad.maxY;
        float pointer_y = ascent;
        float pointer_x = 0;
        float glow = 0;
        boolean transparent = false;
        if (this instanceof CustomColored custom) {
            transparent = custom.transparent();
            glow = clamp(custom.glow());
        } int base_info = transparent ? 0x8000_0000 : 0;
        base_info |= (fonts.currentFont() << 29);
        base_info |= (((size - 1) & 0xFF) << 21);
        //base_info |= (1 << 13); //invert color here
        base_info |= ((round(glow * 127.0f) & 0x7F) << 12);
        for (Word word : words) {
            if ((pointer_y + descent) > height) return;
            float word_width = word.widthPixels(fonts) * scale;
            float pointer_x_next = pointer_x + word_width;
            if (pointer_x_next > width) {
                if (pointer_x > 0) {
                    pointer_y += line_height;
                    if ((pointer_y + descent) > height) return;
                    pointer_x = 0;
                }
            } int info = base_info;
            info |= ((colorIndexOf(word) & 0x3F) << 7);
            byte[] bytes = word.get();
            for (byte b : bytes) {
                char c = (char) (b & 0xFF);
                batch.pushVertex(x + pointer_x, y - pointer_y, info | c);
                pointer_x += fonts.advance(c) * scale;
            } pointer_x += space;
        }
    }
     */



}
