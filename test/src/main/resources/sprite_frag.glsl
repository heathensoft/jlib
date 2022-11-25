#version 440
layout (location=0) out vec4 f_color;
layout (location=1) out uint f_id;
in flat uint id;
in vec2 uv;
in vec4 color;
uniform sampler2D u_sampler;
void main() {
    f_color = texture(u_sampler,uv).rgba * color;
    f_id = id;
    if(f_color.a > 0.5) {
        f_id = id;
    }
}
