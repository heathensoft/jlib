#version 440
#define NUM_TEXTURES 8
layout (location=0) out vec4 f_color;
layout (location=1) out uint f_id;
in flat uint texture_slot;
in flat uint pixel_id;
in vec2 uv;
in vec4 color;
uniform sampler2D[NUM_TEXTURES] u_textures;
void main() {
    sampler2D _sampler = u_textures[texture_slot];
    vec2 texture_size = vec2(textureSize(_sampler, 0).xy);
    vec2 pix = uv * texture_size;
    pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;
    f_color = texture(_sampler, pix / texture_size) * color;
    f_id = pixel_id;
}
