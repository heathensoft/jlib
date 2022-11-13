#version 440

layout (location=0) in vec4 a_pos;
layout (location=1) in vec2 a_uv;
layout (location=2) in vec4 a_color;
layout (location=3) in float a_id;

uniform mat4 u_combined;

out flat uint id;
out vec2 uv;
out vec4 color;


void main() {

    id = uint(a_id);
    uv = a_uv;
    color = a_color;
    color.a *= (255.0/254.0);
    gl_Position = u_combined * a_pos;
}