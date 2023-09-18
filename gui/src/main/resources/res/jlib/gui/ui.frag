#version 440

layout (location=0) out vec4 f_color;

in vec2 uv;
in vec4 color;

uniform sampler2D u_texture;

void main() {

    vec2 texture_size = vec2(textureSize(u_texture, 0).xy);
    vec2 pix = uv * texture_size;
    pix = (floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5) / texture_size;
    vec4 color = texture(u_texture,pix) * color;
    f_color = clamp(color,0.0,1.0);

}
