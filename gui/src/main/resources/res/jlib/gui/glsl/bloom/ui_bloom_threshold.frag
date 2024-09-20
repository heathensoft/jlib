layout (location=0) out vec3 f_color;

in vec2 uv;

#define NUM_NOISE_OCTAVES 5

uniform sampler2D u_diffuse;
uniform sampler2D u_emissive;
uniform float u_threshold;

float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.13);
    p3 += dot(p3, p3.yzx + 3.333);
    return fract((p3.x + p3.y) * p3.z);
}

float noise(vec2 x) {
    vec2 i = floor(x);
    vec2 f = fract(x);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 x) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.50));
    for (int i = 0; i < NUM_NOISE_OCTAVES; ++i) {
        v += a * noise(x);
        x = rot * x * 2.0 + shift;
        a *= 0.5;
    } return v;
}

vec3 apply_glow(vec3 col, float glow) {
    col.r = col.r + (col.r * (1.0 - col.r)) * glow;
    col.g = col.g + (col.g * (1.0 - col.g)) * glow;
    col.b = col.b + (col.b * (1.0 - col.b)) * glow;
    return col * glow;
}

void main() {
    vec4 diffuse = texture(u_diffuse, uv);
    float emissive = texture(u_emissive, uv).r * diffuse.a;
    vec3 color = apply_glow(diffuse.rgb, emissive);
    float brightness = dot(color,vec3(0.2126, 0.7152, 0.0722));
    if(brightness < u_threshold) {
        color = vec3(0.0);
    } f_color = color;
}