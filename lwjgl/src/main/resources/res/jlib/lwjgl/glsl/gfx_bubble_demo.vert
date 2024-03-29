layout (location=0) in vec2 a_pos; // (0 to viewport size)
layout (location=1) in vec2 a_uv;

uniform vec2 u_resolution;

out VS_OUT {
    vec2 uv;
} vs_out;

void main() {
    vec2 centered = (a_pos * 2.0 - u_resolution);
    vs_out.uv = centered / u_resolution.y;
    gl_Position = vec4(centered/u_resolution, 0.0, 1.0);
}
