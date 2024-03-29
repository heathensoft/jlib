
#define TEXT_BINDING_POINT 0 // <------
#define NUM_CHARACTERS 95
#define NUM_FONTS 4

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
    uint glyphBits;  uint glowBits;
    uint sizeBits;  uint fontBits;  bool inverted;
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
struct Color { uint abgr; };

layout (std140, binding = TEXT_BINDING_POINT) uniform TextBlock {
    Font[NUM_FONTS] fonts;  // SIZE = 12352
} textBlock;

out VS_OUT {
    Char character;
} vs_out;

uniform vec2 u_resolution;

float getCharGlow(uint glowBits) { return float(glowBits) / 255.0; }

vec4 invertColor(vec4 color) {
    float r = 1.0 - color.r;
    float g = 1.0 - color.g;
    float b = 1.0 - color.b;
    return vec4(r,g,b,color.a);
}

vec4 getColor(vec4 aColor, bool inverted) {
    vec4 color = aColor;
    color.a *= (255.0/254.0);
    return inverted ? invertColor(color) : color;
}

/*
CharInfo getInfoBits(vec4 vertex) {
    CharInfo charInfo;
    uint iZ = floatBitsToUint(vertex.z);
    // ***************** // ***************** //
    // Vertex: GL_POINTS // 7 bit char  (128) //
    // ***************** // ----------------  //
    // Position: X       // 7 bit glow  (127) //
    //                   // 1 bit invert      //
    // ***************** // 8 bit size  (256) //
    // Position: Y       // 2 bit font  (4)   //
    //                   // ----------------- //
    // ***************** // MSB ************* //
    charInfo.glyphBits = (iZ      ) & 0x7F;     // 7 bit
    // -----------------------------------      // 6 bit
    charInfo.glowBits =  (iZ >> 13) & 0x7F;     // 7 bit
    charInfo.inverted =  ((iZ >> 20) & 0x01) == 1; // 7 bit
    charInfo.sizeBits =  (iZ >> 21) & 0xFF;     // 8 bit
    charInfo.fontBits =  (iZ >> 29) & 0x03;     // 2 bit
    // -----------------------------------      // 1 bit
    return charInfo;
} */

CharInfo getInfoBits(vec4 vertex) {
    CharInfo charInfo;
    uint iZ = floatBitsToUint(vertex.z);
    // -----------------------------------
    charInfo.glyphBits = (iZ      ) & 0x7F;     // 7 bit
    charInfo.inverted =  ((iZ >> 7) & 0x01) == 1; // 1 bit
    charInfo.sizeBits =  (iZ >>  8) & 0xFF;     // 8 bit
    charInfo.glowBits =  (iZ >> 16) & 0xFF;     // 7 bit
    charInfo.fontBits =  (iZ >> 24) & 0x03;     // 2 bit
    // -----------------------------------
    return charInfo;
}


Font getFont(uint fontBits) {
    return textBlock.fonts[fontBits%NUM_FONTS];
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
        region.pos[i] = (region.pos[i] / u_resolution) * 2.0 - 1.0;
    } return region;
}

Char buildCharacter(vec4 v_char, vec4 v_color) {
    Char charStruct;
    CharInfo charInfo = getInfoBits(v_char);
    Font font = getFont(charInfo.fontBits);
    charStruct.glow = getCharGlow(charInfo.glowBits);
    charStruct.color = getColor(v_color,charInfo.inverted);
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

void main() { vs_out.character = buildCharacter(a_vertex,a_color); }