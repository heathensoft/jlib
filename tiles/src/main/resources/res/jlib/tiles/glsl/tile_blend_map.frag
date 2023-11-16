#version 440 core
#define SQRT_2 1.41421356
#define CORNER_WEIGHT .25
#define EDGES_WEIGHT .50
layout (location=0) out vec4 f_color;
in vec2 uv;
float _clamp(float v) { return v > 1.0 ? 1.0 : (v < 0.0 ? 0.0 : v); }
float _lerp(float a, float b, float t) { return a * (1.0 - t) + b * t; }
float _smooth(float v) { return v * v * (3.0 - 2.0 * v); }
void main() {
    float edge_x;
    float edge_y;
    float dist_x;
    float dist_y;
    float dist_c;
    if(uv.x < 0.5) {
        edge_x = 0.0;
        dist_x = (0.5 - uv.x) * 2.0;
    } else {
        edge_x = 1.0;
        dist_x = (uv.x - 0.5) * 2.0;
    }if(uv.y < 0.5) {
        edge_y = 0.0;
        dist_y = (0.5 - uv.y) * 2.0;
    } else {
        edge_y = 1.0;
        dist_y = (uv.y - 0.5) * 2.0;
    }
    dist_c = 1.0 -_clamp((2.0 * distance(vec2(edge_x,edge_y),uv))/SQRT_2);
    float value_c = _smooth(dist_c) * CORNER_WEIGHT;
    float value_x = max(0.0,(_smooth(dist_x) * EDGES_WEIGHT) - value_c);
    float value_y = max(0.0,(_smooth(dist_y) * EDGES_WEIGHT) - value_c);
    f_color = vec4(value_x,value_y,value_c,1.0);
}