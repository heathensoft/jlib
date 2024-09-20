
#define GUI_BINDING_POINT 0
#define TEXT_BINDING_POINT 1

#define NUM_CHARACTERS 95
#define NUM_FONTS 5
#define MARKED_ADJUST 0.25

layout (location = 0) in vec4 a_vertex;
layout (location = 1) in vec4 a_color;

struct TextureRegion {      // NDC
    vec2[4] pos;            // x1,y1 | x2,y1 | x1,y2 | x2,y2
    vec2[4] uvs;            // u1,v2 | u2,v2 | u1,v1 | u2,v1
};

struct Char {               // Vertex Shader -> Geometry Shader
    TextureRegion region;   // scale and offset applied
    vec4 color;             // vert -> geom -> frag
    float glow;             // color emission
    uint texSlot;           // 0 -> (NUM_FONTS - 1)
    bool cursor;            // Special case, no texture
};

struct CharInfo {           // Helper structure
    uint glyphBits;
    uint glowBits;
    uint sizeBits;
    uint fontBits;
    bool inverted;
    bool marked;
};

struct Glyph {              // SIZE: 32
    vec4 uvs;               // u1, v1, u2, v2
    vec2 size;              // width, height
    vec2 offset;            // offX, offY
};

struct Font {               // SIZE: 3088
    Glyph[NUM_CHARACTERS] glyphs; // set cursor here instead (as index 95)
    Glyph cursor;           // Cursor
    float texSlot;          // 0 -> (NUM_FONTS - 1)
    float size;             // Font natural size in pixels
    float padding0;         //
    float padding1;         //
};

layout (std140, binding = TEXT_BINDING_POINT) uniform TextBlock {
    Font[NUM_FONTS] fonts;  // 15,440 of 16 KB guaranteed available pr.ubo
} textBlock;

layout(std140, binding = GUI_BINDING_POINT) uniform GUIBlock {
    vec3 padding;
    float run_time;
    vec2 resolution;
    vec2 mouse;
} gui;

out VS_OUT {
    Char character;
} vs_out;

float getCharGlow(uint glowBits) { return float(glowBits) / 255.0; }

vec4 invertColor(vec4 color) {
    color.r = 1.0 - color.r;
    color.g = 1.0 - color.g;
    color.b = 1.0 - color.b;
    return color;
}

vec4 lowerProportional(vec4 color, float amount) {
    color.r = color.r - color.r * amount;
    color.g = color.g - color.g * amount;
    color.b = color.b - color.b * amount;
    return color;
}

vec4 adjustColor(vec4 color, bool inverted, bool marked) {
    color = inverted ? invertColor(color) : color;
    color = marked ? lowerProportional(color,MARKED_ADJUST) : color;
    return color;
}

CharInfo getCharInfo(vec4 vertex) {
    CharInfo charInfo;
    uint iZ = floatBitsToUint(vertex.z);
    // -----------------------------------
    charInfo.glyphBits = (iZ      ) & 0x7F;         // 7 bit
    charInfo.inverted =  ((iZ >> 7) & 0x01) == 1;   // 1 bit
    charInfo.sizeBits =  (iZ >>  8) & 0xFF;         // 8 bit
    charInfo.glowBits =  (iZ >> 16) & 0xFF;         // 8 bit
    charInfo.marked =    ((iZ >> 24) & 0x01) == 1;  // 1 bit
    charInfo.fontBits =  (iZ >> 25) & 0x07;         // 3 bit (but 5 fonts)
    // -----------------------------------
    return charInfo;
}


Font getFont(uint fontBits) {
    return textBlock.fonts[fontBits % NUM_FONTS];
}

TextureRegion calculateRegion(Glyph glyph, vec2 vertexPos, float scale) {
    TextureRegion region;
    float u1 = glyph.uvs.x;
    float v1 = glyph.uvs.y;
    float u2 = glyph.uvs.z;
    float v2 = glyph.uvs.w;
    vec2 size = glyph.size * scale;
    vec2 offset = glyph.offset * scale;
    region.pos[0] = vertexPos + offset;                 // bl
    region.uvs[0] = vec2(u1,v2);                        // bl
    region.pos[1] = region.pos[0] + vec2(size.x,0.0);   // br
    region.uvs[1] = vec2(u2,v2);                        // br
    region.pos[2] = region.pos[0] + vec2(0.0,size.y);   // tl
    region.uvs[2] = vec2(u1,v1);                        // tl
    region.pos[3] = region.pos[0] + size;               // tr
    region.uvs[3] = vec2(u2,v1);                        // tr
    for(int i = 0; i < 4; i++) {
        region.pos[i] = (region.pos[i] / gui.resolution) * 2.0 - 1.0;
    } return region;
}

Char buildCharacter(vec4 v_char, vec4 v_color) {
    Char charStruct;
    CharInfo charInfo = getCharInfo(v_char);
    Font font = getFont(charInfo.fontBits);
    charStruct.glow = charInfo.marked ? 0.0 : getCharGlow(charInfo.glowBits);
    charStruct.color = adjustColor(v_color,charInfo.inverted,charInfo.marked);
    charStruct.texSlot = uint(font.texSlot);
    float scale = float(charInfo.sizeBits + 1) / (font.size); // adjust back
    if(charInfo.glyphBits < 32 || charInfo.glyphBits > 126) {
        charStruct.cursor = true;
        Glyph glyph = font.cursor;
        charStruct.region = calculateRegion(glyph, v_char.xy, scale);
    } else { charStruct.cursor = false;
             Glyph glyph = font.glyphs[charInfo.glyphBits - 32];
             charStruct.region = calculateRegion(glyph, v_char.xy, scale);
    } return charStruct;
}

void main() {
    a_color.a *= (255.0/254.0);
    vs_out.character = buildCharacter(a_vertex,a_color);
}