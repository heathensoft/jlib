
layout (location=0) in vec2 a_pos;
layout (location=1) in vec2 a_uv;
layout (location=2) in vec4 a_color;
layout (location=3) in float a_data;

uniform vec2 u_resolution;

out VS_OUT {
    vec4 color;
    vec2 uv;
    vec2 elipse_coord;
    float glow;
    flat uint texture_slot_diffuse;
    flat uint texture_slot_normals;
    flat uint pixel_id;
    flat bool draw_alpha;
    flat bool draw_elipse;
} vs_out;

const vec2[4] elipse_coords = {
        vec2(-1.0, 1.0),
        vec2(-1.0,-1.0),
        vec2( 1.0,-1.0),
        vec2( 1.0, 1.0)
};

void main() {
    vs_out.color = a_color;
    vs_out.color.a *= (255.0/254.0);
    vs_out.uv = a_uv;
    vs_out.elipse_coord = elipse_coords[gl_VertexID % 4];
    uint i_data = floatBitsToUint(a_data);
    vs_out.texture_slot_diffuse = i_data & 0x0F;
    vs_out.texture_slot_normals = (i_data >> 4) & 0x0F;
    vs_out.glow = float((i_data >> 8) & 0x3F) / 63.0;
    vs_out.draw_elipse = ((i_data >> 14) & 0x01) == uint(1);
    vs_out.draw_alpha = ((i_data >> 15) & 0x01) == uint(1);
    vs_out.pixel_id = (i_data >> 16) & 0xFFFF;
    gl_Position = vec4((a_pos / u_resolution) * 2.0 - 1.0,0.0,1.0);
}



