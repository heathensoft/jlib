#version 460 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;


in VS_OUT {
    uint tile_uv_index;
    uint tile_damage;
    bool tile_is_block;
} gs_in[];

out GS_OUT {
    vec2 world_position;
    vec2 tile_uv;
    float tile_damage;
    flat bool tile_is_block;
} gs_out;


uniform mat4 u_proj_view;
uniform float u_tile_size_pixels;
uniform sampler2D u_block_atlas;
uniform sampler2DArray u_terrain_textures;

void main() {

    vec2 tile_coord = gl_in[0].gl_Position.xy;
    float tile_damage = float(gs_in[0].tile_damage) / 15.0;

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

    if(gs_in[0].tile_is_block) {

        vec2 block_atlas_size = vec2(textureSize(u_block_atlas,0).xy);
        float tex_coord_x = float((gs_in[0].tile_uv_index % 64) * u_tile_size_pixels);
        float tex_coord_y = float((gs_in[0].tile_uv_index / 64) * u_tile_size_pixels);
        u1 = (tex_coord_x + 0.5) / block_atlas_size.x;
        v1 = (tex_coord_y + 0.5) / block_atlas_size.y;
        u2 = (tex_coord_x + u_tile_size_pixels - 0.5) / block_atlas_size.x;
        v2 = (tex_coord_y + u_tile_size_pixels - 0.5) / block_atlas_size.y;

    } else {

        // Might need to adjust by 0.5 pixels
        vec2 terrain_texture_size = vec2(textureSize(u_terrain_textures, 0).xy);
        u1 = (u_tile_size_pixels *  x1) / ( terrain_texture_size.x);
        v1 = (u_tile_size_pixels * -y2) / ( terrain_texture_size.y);
        u2 = (u_tile_size_pixels *  x2) / ( terrain_texture_size.x);
        v2 = (u_tile_size_pixels * -y1) / ( terrain_texture_size.y);

    }

    gl_Position = u_proj_view * vec4(x1, y1, 0.0, 1.0); //bl
    gs_out.world_position = vec2(x1,y1);
    gs_out.tile_is_block = gs_in[0].tile_is_block;
    gs_out.tile_damage = tile_damage;
    gs_out.tile_uv = vec2(u1, v2);
    EmitVertex();

    gl_Position = u_proj_view * vec4(x2, y1, 0.0, 1.0); //br
    gs_out.world_position = vec2(x2,y1);
    gs_out.tile_is_block = gs_in[0].tile_is_block;
    gs_out.tile_damage = tile_damage;
    gs_out.tile_uv = vec2(u2, v2);
    EmitVertex();

    gl_Position = u_proj_view * vec4(x1, y2, 0.0, 1.0); //tl
    gs_out.world_position = vec2(x1,y2);
    gs_out.tile_is_block = gs_in[0].tile_is_block;
    gs_out.tile_damage = tile_damage;
    gs_out.tile_uv = vec2(u1, v1);
    EmitVertex();

    gl_Position = u_proj_view * vec4(x2, y2, 0.0, 1.0); //tr
    gs_out.world_position = vec2(x2,y2);
    gs_out.tile_is_block = gs_in[0].tile_is_block;
    gs_out.tile_damage = tile_damage;
    gs_out.tile_uv = vec2(u2, v1);
    EmitVertex();

    EndPrimitive();


}