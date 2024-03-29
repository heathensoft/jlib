
layout (location=0) out vec4 f_color;
in vec2 pos;

uniform float u_hue;
uniform int u_draw_slider;

vec3 hsv_to_rgb(float h, float s, float v);

void main() {

    float h = 0.0;
    float s = 1.0;
    float v = 1.0;

    if(u_draw_slider == 0) {
        h = mod(u_hue,360.0);
        s = pos.x;
        v = 1.0 - pos.y;
    } else {
        h = pos.x * 360.0;
    }

    vec3 col = hsv_to_rgb(h,s,v);
    f_color = vec4(col,1.0);
}

vec3 hsv_to_rgb(float h, float s, float v) {
    vec3 rgb = vec3(1.0,1.0,1.0);
    float x = mod((h / 60.0 + 6.0), 6.0);
    int i = int(x);
    float f = x - float(i);
    float p = v * (1.0 - s);
    float q = v * (1.0 - s * f);
    float t = v * (1.0 - s * (1.0 - f));
    if(i == 0) {
        rgb.x = v; rgb.y = t; rgb.z = p;
    } else if(i == 1) {
        rgb.x = q; rgb.y = v; rgb.z = p;
    } else if(i == 2) {
        rgb.x = p; rgb.y = v; rgb.z = t;
    } else if(i == 3) {
        rgb.x = p; rgb.y = q; rgb.z = v;
    } else if(i == 4) {
        rgb.x = t; rgb.y = p; rgb.z = v;
    } else {
        rgb.x = v; rgb.y = p; rgb.z = q;
    } return rgb;
}