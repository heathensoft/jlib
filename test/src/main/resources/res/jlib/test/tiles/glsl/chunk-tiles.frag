#version 460 core

layout (location=0) out vec4 f_color;

in GS_OUT {
    vec2 uv;
    float tile_damage;
    flat bool tile_is_block;
} fs_in;

/*
layout (std140, binding = 1) uniform TilesBlock {
    vec3 std_padding;
    float tile_size;
};
*/

uniform sampler2D u_block_atlas;
uniform sampler2D u_terrain_texture;

vec4 sampleBlockAtlas(vec2 uv) {
    return texture(u_block_atlas,uv);
}

vec4 sampleTerrain(vec2 uv) {
    return texture(u_terrain_texture,uv);
}

/*
vec4 sampleTerrain(vec2 uv, float type) {
    return texture(u_terrain_texture_array,vec3(uv,type));
} */

vec2 pixel_art_antialiasing_uv_adjust(vec2 uv, vec2 tex_size) {
    // Not sure if i need to do this at every shader stage. I actually think i do.
    // Use Linear Sampling with this!!
    vec2 pix = uv * tex_size;
    pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;
    return vec2(pix / tex_size);
}

void main() {

    f_color = vec4(1.0);

    if(fs_in.tile_is_block) {

        f_color = sampleBlockAtlas(fs_in.uv);
        //f_color.z = 1.0;

    }

    else {

        f_color = sampleTerrain(fs_in.uv);
        //f_color.y = 1.0;

    }

    f_color = clamp(f_color,0.0,1.0);
    f_color.a = 1.0;
    //f_color.x = 1.0;
}