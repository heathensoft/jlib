
#define GUI_BINDING_POINT 0

layout (location=0) in vec2 a_pos;
layout (location=1) in vec4 a_color;

layout(std140, binding = GUI_BINDING_POINT) uniform GUIBlock {
    vec3 padding;
    float run_time;
    vec2 resolution;
    vec2 mouse;
} gui;

out vec4 color;

void main() {
    color = a_color;
    color.a *= (255.0/254.0);
    gl_Position = vec4((a_pos / gui.resolution) * 2.0 - 1.0,0.0,1.0);
}