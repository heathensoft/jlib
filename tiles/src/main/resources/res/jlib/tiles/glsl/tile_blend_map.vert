#version 440 core
layout (location = 0) in vec2 a_uv;
out vec2 uv;
void main() {
    uv = a_uv;
    vec2 gl_pos = vec2(uv.x,1.0 - uv.y);
    gl_pos = gl_pos * 2.0 - 1.0;
    gl_Position = vec4(gl_pos,0.0,1.0);
}