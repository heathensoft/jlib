#version 440 core

layout (location=0) in int a_tile;

out VS_OUT {
    uint tile_uv_index;
    bool tile_is_block;
    bool tile_is_damaged; // could also use this for non-blocks (burnt ground or whatever)
} vs_out;

void main() {

    float tile_x = float(a_tile & 0x3FF);
    float tile_y = float((a_tile >> 10) & 0x3FF);
    vs_out.tile_uv_index = uint((a_tile >> 20) & 0x3FF);
    vs_out.tile_is_damaged = bool((a_tile >> 30) & 0x01);
    vs_out.tile_is_block = a_tile < 0; // (msb-signed)
    gl_Position = vec4(tile_x,tile_y,0.0,1.0);

}