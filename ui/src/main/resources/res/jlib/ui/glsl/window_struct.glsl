

// have a gui uniform buffer instead
// resolution, cursor, windows, time, paused

#define NUM_WINDOWS 128
#define GUI_BINDING_POINT 0

struct Window { // 32 bytes
    vec4 bounds;
    float data; // type, open
    float timer;
    float padding_0;
    float padding_1;
};

layout(std140, binding = GUI_BINDING_POINT) uniform GUIBlock { // 32
    vec2 resolution;
    vec2 mouse;
    vec3 padding;
    float run_time;
} gui;







