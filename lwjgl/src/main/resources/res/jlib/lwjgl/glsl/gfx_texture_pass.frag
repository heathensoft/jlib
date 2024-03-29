layout (location=0) out vec4 f_color;
in VS_OUT {
    vec2 uv;
} vs_in;
uniform sampler2D u_sampler2D;
void main() { f_color = texture(u_sampler2D,vs_in.uv); }