#version 440
layout (location=0) out vec4 f_color;
in vec2 uv;
uniform sampler2D u_sampler;
void main() {
    f_color = texture(u_sampler,uv);
}