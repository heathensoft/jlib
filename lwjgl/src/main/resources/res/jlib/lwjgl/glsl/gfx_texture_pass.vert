layout (location=0) in vec2 a_pos;
layout (location=1) in vec2 a_uv;

uniform vec2 u_resolution;

out VS_OUT {
    vec2 uv;
} vs_out;

void main() {
    vs_out.uv = a_uv;
    vec2 pos = vec2(a_pos / u_resolution) * 2.0 - 1.0;
    gl_Position = vec4(pos,0.0,1.0);
}