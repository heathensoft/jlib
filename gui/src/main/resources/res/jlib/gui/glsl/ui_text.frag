
#define NUM_FONTS 5
layout (location = 0) out vec4 f_diffuse;
layout (location = 1) out vec4 f_emissive;

in GS_OUT {
    vec4 color;
    vec2 uv;
    float glow;
    flat uint texture_slot;
    flat bool cursor;
} fs_in;

uniform sampler2D[NUM_FONTS] u_font_textures;
//const vec3 FLAT_NORMAL = vec3(0.5, 0.5, 1.0);

float _lerp(float a, float b, float t) { return a * (1-t) + b * t; }
float _clamp(float v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
float _unlerp(float a, float b, float t) { return _clamp((t - a) / (b - a)); }
float _remap(float v, float v_min, float v_max, float out_min, float out_max) {
    return _lerp(out_min,out_max,_unlerp(v_min,v_max,v));
}

void main() {

    vec4 diffuse = fs_in.color;
    float glow = fs_in.glow;

    if(!fs_in.cursor)
    {
        vec4 font_sample = texture(u_font_textures[fs_in.texture_slot], fs_in.uv);
        diffuse.a *= font_sample.a;
    }

    f_emissive = vec4(vec3(glow),diffuse.a);
    f_diffuse = diffuse;

}