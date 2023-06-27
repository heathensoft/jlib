#version 460 core

#define CHUNK_TILES_ACROSS 16

layout (location=0) in uint a_tile;

out VS_OUT {
    uint tile_uv_index;
    uint tile_damage;
    bool tile_is_block;
} vs_out;

void main() {

    uint local_x = gl_VertexID % CHUNK_TILES_ACROSS;
    uint local_y = gl_VertexID / CHUNK_TILES_ACROSS;
    uint tile_x = local_x + ((a_tile & 0x3F) * CHUNK_TILES_ACROSS);
    uint tile_y = local_y + (((a_tile >> 6) & 0x3F) * CHUNK_TILES_ACROSS);
    vs_out.tile_uv_index = (a_tile >> 12) & 0xFFF;
    vs_out.tile_damage = (a_tile >> 24) & 0x0F;
    vs_out.tile_is_block = bool((a_tile >> 31) & 0x01);
    gl_Position = vec4(tile_x,tile_y,0.0,1.0);

}