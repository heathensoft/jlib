#version 440 core
layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

struct TextureRegion {
    vec2[4] pos;    // x1,y1 | x2,y1 | x1,y2 | x2,y2
    vec2[4] uvs;    // u1,v2 | u2,v2 | u1,v1 | u2,v1
}; struct Char {
    TextureRegion region;   // scale and offset applied
    vec4 color;             // vert -> geom -> frag
    float glow;             // strength of glow-effect
    uint texSlot;           // 0 -> (NUM_FONTS - 1)
    bool cursor;            // Special case, no texture
    bool transparent;       //
};
in VS_OUT {
    Char character;
} gs_in[];
out GS_OUT {
    vec4 color;
    vec2 uv;
    float glow;
    flat uint texture_slot;
    flat bool cursor;
    flat bool transparent;
} gs_out;
void main() {
    Char charObject = gs_in[0].character;
    for(int i = 0; i < 4; i++) {
        gl_Position = vec4(charObject.region.pos[i], 0.0, 1.0);
        gs_out.uv = charObject.region.uvs[i];
        gs_out.glow = charObject.glow;
        gs_out.texture_slot = charObject.texSlot;
        gs_out.cursor = charObject.cursor;
        gs_out.color = charObject.color;
        gs_out.transparent = charObject.transparent;
        EmitVertex();
    } EndPrimitive();
}