#version 440
#define NUM_TEXTURES 8
layout (location=0) out vec4 f_color;
in vec2 uv;
in vec4 color;
in flat uint texture_slot;
uniform sampler2D[NUM_TEXTURES] u_textures;

void main() {
    vec4 color = color;
    if(texture_slot != 0xFF) {
        vec2 texture_size = vec2(textureSize(u_textures[texture_slot], 0).xy);
        vec2 pix = uv * texture_size;
        pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;
        //float d = texture(u_textures[texture_slot], pix / texture_size).r;
        //float aaf = fwidth(d);
        //float alpha = smoothstep(0.5-aaf,0.5+aaf,d);
        color = vec4(texture(u_textures[texture_slot], uv).rgb,1.0) * color;
    } f_color = color;
}
