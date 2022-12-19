#version 440
#define NUM_TEXTURES 4
layout (location=0) out vec4 f_color;
layout (location=1) out uint f_id;
in flat int tex_slot;
in flat uint id;
in vec2 uv;
in vec4 color;
uniform sampler2D[NUM_TEXTURES] u_textures;
void main() {
    f_color = texture(u_textures[tex_slot],uv).rgba * color;
    f_id = id;
}
