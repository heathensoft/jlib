#version 460 core

layout (location=0) out vec4 f_diffuse;

in GS_OUT {
    vec2 world_position;
    vec2 tile_uv;
    float tile_damage;
    flat bool tile_is_block;
} fs_in;


uniform sampler2D u_block_atlas;
uniform sampler2D u_terrain_blend_map;
uniform sampler2DArray u_terrain_textures;


int terrain_type(ivec2 tile) {
    return 0;
}

vec4 sampleTile(int terrain, vec2 uv) {
    return vec4(1);
}

vec4 interpolate(vec2 world_position, vec2 uv) {

    vec2 sample_position = world_position - 0.5;
    float x1 = floor(sample_position.x);
    float y1 = floor(sample_position.y);
    float x2 = x1 + 1.0;
    float y2 = y1 + 1.0;

    vec4 q11 = sampleTile(terrain_type(ivec2(x1,y1)),uv);
    vec4 q12 = sampleTile(terrain_type(ivec2(x1,y2)),uv);
    vec4 q21 = sampleTile(terrain_type(ivec2(x2,y1)),uv);
    vec4 q22 = sampleTile(terrain_type(ivec2(x2,y2)),uv);

    float dx = fract(sample_position.x);
    float dy = fract(sample_position.y);

    vec4 r1 = mix(q11,q21,dx);
    vec4 r2 = mix(q12,q22,dx);

    return mix(r1,r2,dy);

}

vec4 sampleBlendMap(vec2 uv) {
    return texture(u_terrain_blend_map,uv);
}

vec4 sampleBlockAtlas(vec2 uv) {
    return texture(u_block_atlas,uv);
}

vec4 sampleTerrain(vec2 uv, float layer) {
    return texture(u_terrain_textures, vec3(uv,layer));
}

vec2 pixel_art_antialiasing(vec2 uv, vec2 tex_size) {
    vec2 pix = uv * tex_size;
    pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;
    return vec2(pix / tex_size);
}

float clamp(float v) {
    return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
}

float smoothen(float v) {
    return v * v * (3.0 - 2.0 * v);
}

vec4 premultiply_alpha(vec4 color) {
    float r = color.r * color.a;
    float g = color.g * color.a;
    float b = color.b * color.a;
    return vec4(r,g,b,color.a);
}

// dst_alpha is always 1.0
vec4 alpha_blend(vec4 src, vec4 dst) {
    float r = (src.r * src.a) + dst.r * (1.0 - src.a);
    float g = (src.g * src.a) + dst.g * (1.0 - src.a);
    float b = (src.b * src.a) + dst.b * (1.0 - src.a);
    return vec4(r,g,b,1.0);
}

vec3 permute(vec3 x) { return mod(((x*34.0)+1.0)*x, 289.0); }

float noise(vec2 v){
    const vec4 C = vec4(0.211324865405187, 0.366025403784439,
                        -0.577350269189626, 0.024390243902439);
    vec2 i  = floor(v + dot(v, C.yy) );
    vec2 x0 = v -   i + dot(i, C.xx);
    vec2 i1;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;
    i = mod(i, 289.0);
    vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
                      + i.x + vec3(0.0, i1.x, 1.0 ));
    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy),
                            dot(x12.zw,x12.zw)), 0.0);
    m = m*m ;
    m = m*m ;
    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;
    m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );
    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

const mat2 m2 = mat2(0.8,-0.6,0.6,0.8);

float fbm( in vec2 p ){
    float f = 0.0;
    f += 0.5000*noise( p ); p = m2*p*2.02;
    f += 0.2500*noise( p ); p = m2*p*2.03;
    f += 0.1250*noise( p ); p = m2*p*2.01;
    f += 0.0625*noise( p );
    return f/0.9375;
}

void main() {

    vec4 color_diffuse = vec4(1.0);

    if(fs_in.tile_is_block)
    {
        // if zoom < 1.0 (zoomed inn)
        //vec2 block_atlas_size = textureSize(u_block_atlas,0).xy;
        //vec2 block_uv = pixel_art_antialiasing(fs_in.tile_uv,block_atlas_size);
        color_diffuse = sampleBlockAtlas(fs_in.tile_uv);
    }
    else {

        float n1 = mix(0.95,1.0,fbm((fs_in.world_position) * 0.005));
        float n2 = mix(0.95,1.0,fbm((fs_in.world_position + 1024) * 0.005));
        float n3 = mix(0.9,1.0,fbm((fs_in.world_position + 2048) * 0.001));
        float n4 = mix(0.2,0.7,fbm((fs_in.world_position + 4096) * 0.7));


        vec2 map_size = textureSize(u_terrain_blend_map,0).xy;
        vec2 bm_uv = fs_in.world_position / map_size;
        vec4 bm_color = sampleBlendMap(bm_uv);

        vec4 t0 = sampleTerrain(fs_in.tile_uv,0.0);
        vec4 t1 = sampleTerrain(fs_in.tile_uv,1.0);
        vec4 t2 = sampleTerrain(fs_in.tile_uv,2.0);
        vec4 t3 = sampleTerrain(fs_in.tile_uv,3.0);
        vec4 t4 = sampleTerrain(fs_in.tile_uv,4.0);

        t0.a = 1.0;
        t1.a *= (smoothen(bm_color.a * n1));
        t2.a *= (smoothen(bm_color.b * n2));
        t3.a *= (smoothen(bm_color.g * n3));
        t4.a *= (smoothen(bm_color.r * n4));


        color_diffuse = alpha_blend(t1,t0);
        color_diffuse = alpha_blend(t2,color_diffuse);
        color_diffuse = alpha_blend(t3,color_diffuse);
        color_diffuse = alpha_blend(t4,color_diffuse);

    }

    f_diffuse = color_diffuse;
}