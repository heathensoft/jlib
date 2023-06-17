#version 440 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

in VS_OUT {
    uint tile_uv_index;
    bool tile_is_block;
    bool tile_is_damaged;
} gs_in;

out GS_OUT {
    vec2 uv;
    bool tile_is_block;
    bool tile_is_damaged;
} gs_out;

layout (std140, binding = 0) uniform CameraBlock {
    mat4 proj_view;
};

layout (std140, binding = 1) uniform TilesBlock {
    vec3 std_padding;
    float tile_size;
};

uniform sampler2D u_block_atlas;
uniform sampler2D u_terrain_texture;

void main() {

    vec2 tile_coord = gl_in[0].gl_Position.xy;

    // x1 -> u1
    // y1 -> v2
    // x2 -> u2
    // y2 -> v1

    float x1 = tile_coord.x;
    float y1 = tile_coord.y;
    float x2 = x1 + 1.0;
    float y2 = y1 + 1.0;

    float u1 = 0.0;
    float v1 = 0.0;
    float u2 = 0.0;
    float v2 = 0.0;

    if(gs_in.tile_is_block) {

        vec2 block_atlas_size = vec2(textureSize(u_block_atlas,0).xy);
        float tex_coord_x = float((gs_in.tile_uv_index % 32) * tile_size);
        float tex_coord_y = float((gs_in.tile_uv_index / 32) * tile_size);
        u1 = (tex_coord_x + 0.5) / block_atlas_size.x;
        v1 = (tex_coord_y + 0.5) / block_atlas_size.y;
        u2 = (tex_coord_x + tile_size - 0.5) / block_atlas_size.x;
        v2 = (tex_coord_y + tile_size - 0.5) / block_atlas_size.y;

    } else {

        // Might need to adjust by 0.5 pixels
        vec2 terrain_texture_size = vec2(textureSize(u_terrain_texture,0).xy);
        u1 = x1 / (tile_size * terrain_texture_size.x);
        v1 = y2 / (tile_size * terrain_texture_size.y);
        u2 = x2 / (tile_size * terrain_texture_size.x);
        v2 = y1 / (tile_size * terrain_texture_size.y);

    }

    gl_Position = proj_view * vec4(x1,y1,0.0,1.0); //bl
    gs_out.tile_is_block = gs_in.tile_is_block;
    gs_out.tile_is_damaged = gs_in.tile_is_damaged;
    gs_out.uv = vec2(u1,v2);
    EmitVertex();

    gl_Position = proj_view * vec4(x2,y1,0.0,1.0); //br
    gs_out.tile_is_block = gs_in.tile_is_block;
    gs_out.tile_is_damaged = gs_in.tile_is_damaged;
    gs_out.uv = vec2(u2,v2);
    EmitVertex();

    gl_Position = proj_view * vec4(x1,y2,0.0,1.0); //tl
    gs_out.tile_is_block = gs_in.tile_is_block;
    gs_out.tile_is_damaged = gs_in.tile_is_damaged;
    gs_out.uv = vec2(u1,v1);
    EmitVertex();

    gl_Position = proj_view * vec4(x2,y2,0.0,1.0); //tl
    gs_out.tile_is_block = gs_in.tile_is_block;
    gs_out.tile_is_damaged = gs_in.tile_is_damaged;
    gs_out.uv = vec2(u2,v1);
    EmitVertex();

    EndPrimitive();


}