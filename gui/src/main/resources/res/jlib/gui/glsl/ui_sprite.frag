#version 440 core

#define NO_TEXTUE 15
#define TEXTURE_SLOTS 15

layout (location=0) out vec4 f_diffuse;
layout (location=1) out vec4 f_normals;
layout (location=2) out float f_emissive;
layout (location=3) out uint f_pixel_id;

in VS_OUT {
    vec4 color;
    vec2 uv;
    float glow;
    flat uint texture_slot_diffuse;
    flat uint texture_slot_normals;
    flat uint pixel_id;
    flat bool draw_alpha;
} fs_in;

uniform sampler2D[TEXTURE_SLOTS] u_diffuse_textures;
uniform sampler2D[TEXTURE_SLOTS] u_normals_textures;
const float ALPHA_THRESHOLD = 0.01f;
const vec3 FLAT_NORMAL = vec3(0.5, 0.5, 1.0);

/*
vec2 texture_size = vec2(textureSize(u_textures[texture_slot], 0).xy);
vec2 pix = uv * texture_size;
pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;
*/

void main() {
    vec4 diffuse = fs_in.color;
    vec3 normals = FLAT_NORMAL;
    uint id = fs_in.pixel_id;
    float glow = fs_in.glow;
    if(fs_in.texture_slot_diffuse != NO_TEXTUE) {
        uint slot = fs_in.texture_slot_diffuse;
        vec4 t_sample = texture(u_diffuse_textures[slot],fs_in.uv);
        diffuse *= t_sample;
    } if(fs_in.texture_slot_normals != NO_TEXTUE) {
        uint slot = fs_in.texture_slot_normals;
        normals = texture(u_normals_textures[slot],fs_in.uv).rgb;
    } if(diffuse.a < ALPHA_THRESHOLD) {
        if(!fs_in.draw_alpha) {
            discard;
            //id = uint(0);
        } // discard
    } f_diffuse = diffuse;
    f_normals = vec4(normals,diffuse.a);

    float c = glow * diffuse.a;
    f_emissive = c;
    f_pixel_id = id;
}