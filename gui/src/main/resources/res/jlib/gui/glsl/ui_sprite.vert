
#define GUI_BINDING_POINT 0

layout (location=0) in vec2 a_pos;
layout (location=1) in vec2 a_uv;
layout (location=2) in vec4 a_color;
layout (location=3) in float a_data;

layout(std140, binding = GUI_BINDING_POINT) uniform GUIBlock {
    vec3 padding;
    float run_time;
    vec2 resolution;
    vec2 mouse;
} gui;

uniform uint u_window;

out VS_OUT {
    vec4 color;
    vec2 uv;
    vec2 elipse_coord;
    float glow;
    flat uint texture_slot;
    flat uint pixel_id;
    flat bool draw_alpha;
    flat bool draw_elipse;
    flat bool pixel_aa;
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
    vs_out.texture_slot = i_data & 0x0F;
    vs_out.pixel_aa = ((i_data >> 5) & 0x01) == uint(1);
    vs_out.draw_elipse = ((i_data >> 6) & 0x01) == uint(1);
    vs_out.draw_alpha = ((i_data >> 7) & 0x01) == uint(1);
    vs_out.glow = float((i_data >> 8) & 0xFF) / 255.0;
    vs_out.pixel_id = ((i_data >> 16) & 0xFFFF) | ((u_window << 16));
    gl_Position = vec4((a_pos / gui.resolution) * 2.0 - 1.0,0.0,1.0);
}



