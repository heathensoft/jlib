layout (location=0) out vec4 f_color;
in VS_OUT {
    vec2 uv;
} vs_in;
uniform sampler2D u_sampler2D;
void main() {
    const float[9] kernel = {
    0.0778,	0.1233,	0.0778,
    0.1233,	0.1953,	0.1233,
    0.0778,	0.1233,	0.0778 };
    const vec2 adj[9] = {
    vec2(-1, 1), vec2(0, 1), vec2(1, 1),
    vec2(-1, 0), vec2(0, 0), vec2(1, 0),
    vec2(-1,-1), vec2(0,-1), vec2(1,-1) };
    vec2 tex_size_inv = 1.0 / vec2(textureSize(u_sampler2D, 0));
    vec4 color = vec4(0.0);
    for(int i = 0; i < 9; i++) {
        vec2 sample_uv = vs_in.uv + adj[i] * tex_size_inv;
        color += texture(u_sampler2D, sample_uv) * kernel[i];
    } f_color = color;
}