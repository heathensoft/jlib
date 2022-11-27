#version 440
#define num_textures 4
layout (location=0) out vec4 f_color;
layout (location=1) out uint f_id;
in flat uint id;
in flat int texture_slot;
in vec2 uv;
in vec4 color;
uniform sampler2D[num_textures] u_textures;
void main() {
    sampler2D sampler = u_textures[texture_slot];
    f_color = texture(sampler,uv).rgba * color;
    f_id = id;
}
