#version 440

layout (location=0) in vec4 a_pos;
layout (location=1) in vec2 a_uv;


out vec2 uv;

void main() {

    uv = a_uv;
    gl_Position = a_pos;

}
