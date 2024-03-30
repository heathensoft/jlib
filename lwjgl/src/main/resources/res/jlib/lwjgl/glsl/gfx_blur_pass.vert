layout (location=0) in vec2 a_pos;
layout (location=1) in vec2 a_uv;
out VS_OUT {
    vec2 uv;
} vs_out;
void main() {
    vs_out.uv = a_uv;
    gl_Position = vec4((a_pos * 2.0 - 1.0),0.0,1.0);
}