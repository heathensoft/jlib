#version 440
#define NUM_TEXTURES 8
layout (location=0) out vec4 f_color;
//layout (location=1) out uint f_id;
//in flat uint texture_slot;
//in flat uint pixel_id;
in vec2 uv;
in vec4 color;
uniform sampler2D u_texture;

void main() {
    //vec2 texture_size = vec2(textureSize(u_texture, 0).xy);
    //vec2 pix = uv * texture_size;
    //pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;
    //f_color = texture(u_texture, pix / texture_size) * color;
    vec4 color = texture(u_texture,uv) * color;
    color += vec4(1.0,1.0,1.0,1.0);

    f_color = clamp(color,0.0,1.0);

    //f_id = pixel_id;
}
