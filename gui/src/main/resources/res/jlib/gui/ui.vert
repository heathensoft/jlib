#version 440
layout (location=0) in vec2 a_pos;
layout (location=1) in vec2 a_uv;
layout (location=2) in vec4 a_color;
layout (location=3) in float a_custom;
uniform vec2 u_resolution;
out flat uint texture_slot;
out vec2 uv;
out vec4 color;
void main() {
    texture_slot = uint(a_custom) & 0xFF;
    color = a_color;
    color.a *= (255.0/254.0);
    uv = a_uv;
    vec2 position = (a_pos / u_resolution) * 2.0 - 1.0;
    gl_Position = vec4(position,0.0,1.0);
}
