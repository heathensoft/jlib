#version 440

layout (location=0) in vec4 a_pos;

void main() {

    gl_Position = a_pos;
}