
#define NO_TEXTUE 15
#define TEXTURE_SLOTS 15

layout (location=0) out vec4 f_diffuse;
layout (location=1) out vec3 f_normals;
layout (location=2) out float f_emissive;
layout (location=3) out uint f_pixel_id;

in VS_OUT {
    vec4 color;
    vec2 uv;
    vec2 elipse_coord;
    float glow;
    flat uint texture_slot_diffuse;
    flat uint texture_slot_normals;
    flat uint pixel_id;
    flat bool draw_alpha;
    flat bool draw_elipse;
    flat bool pixel_aa;
} fs_in;

uniform sampler2D[TEXTURE_SLOTS] u_diffuse_textures;
uniform sampler2D[TEXTURE_SLOTS] u_normals_textures;
const float ALPHA_THRESHOLD = 0.005f;
const vec3 FLAT_NORMAL = vec3(0.5, 0.5, 1.0);
const float ELIPSE_ALPHA_THRESHOLD = 0.97; // Soft Edges

float _lengthSq(vec2 v) { return v.x * v.x + v.y * v.y; }
float _lerp(float a, float b, float t) { return a * (1-t) + b * t; }
float _clamp(float v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
float _unlerp(float a, float b, float t) { return _clamp((t - a) / (b - a)); }
float _remap(float v, float v_min, float v_max, float out_min, float out_max) {
    return _lerp(out_min,out_max,_unlerp(v_min,v_max,v));
}

void main() {

    vec4 diffuse = fs_in.color;
    vec3 normals = FLAT_NORMAL;
    uint id = fs_in.pixel_id;
    float glow = fs_in.glow;

    if(fs_in.texture_slot_diffuse != NO_TEXTUE) {
        uint slot = fs_in.texture_slot_diffuse;
        vec4 t_sample;
        if(fs_in.pixel_aa) {
            vec2 texture_size = vec2(textureSize(u_diffuse_textures[slot],0).xy);
            vec2 pix = fs_in.uv * texture_size;
            pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;
            t_sample = texture(u_diffuse_textures[slot],pix / texture_size);
        } else t_sample = texture(u_diffuse_textures[slot],fs_in.uv);
        diffuse *= t_sample;
    }
    if(fs_in.texture_slot_normals != NO_TEXTUE) {
        uint slot = fs_in.texture_slot_normals;
        if(fs_in.pixel_aa) {
            vec2 texture_size = vec2(textureSize(u_normals_textures[slot],0).xy);
            vec2 pix = fs_in.uv * texture_size;
            pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;
            normals = texture(u_normals_textures[slot],pix / texture_size).rgb;
        } else normals = texture(u_normals_textures[slot],fs_in.uv).rgb;

    }
    if(fs_in.draw_elipse) {
        float l2 = _lengthSq(fs_in.elipse_coord);
        if(l2 > 1.0) { diffuse.a = 0.0f; }
        else if(l2 >= ELIPSE_ALPHA_THRESHOLD) {
            float factor = _remap(l2,ELIPSE_ALPHA_THRESHOLD,1.0,1.0,0.0);
            diffuse.a *= factor;
        }
    }
    if(diffuse.a < ALPHA_THRESHOLD) {
        if(!fs_in.draw_alpha) {
            discard;
        }
    }

    f_diffuse = diffuse;
    f_emissive = glow * diffuse.a; // maybe no. Depends on blending
    f_normals = normals;
    f_pixel_id = id;
}

