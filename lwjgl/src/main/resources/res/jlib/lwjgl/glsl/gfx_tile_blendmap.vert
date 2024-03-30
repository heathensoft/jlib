layout (location=0) in vec2 a_pos;
layout (location=1) in vec2 a_uv;
out vec2 uv;
void main() {
    uv = a_uv;
    vec2 pos = vec2(uv.x,1.0 - uv.y);
    pos = pos * 2.0 - 1.0;
    gl_Position = vec4(pos,0.0,1.0);
}