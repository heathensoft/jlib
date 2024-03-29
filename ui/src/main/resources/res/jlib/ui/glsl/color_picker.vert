
layout (location=0) in vec4 a_pos;
out vec2 pos;
void main() {
    pos = (vec2(a_pos.xy) + 1.0) / 2.0;
    gl_Position = a_pos;
}