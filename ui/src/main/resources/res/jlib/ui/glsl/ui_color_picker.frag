
layout (location=0) out vec4 f_color;
in vec2 uv;

const int MODE_ALPHA_SLIDER = 0;
const int MODE_HSV_WINDOW = 1;
const int MODE_HUE_SLIDER = 2;

uniform float u_hue;
uniform int u_mode;

vec3 hsv_to_rgb(float h, float s, float v);

void main() {
    float alpha = 1.0f;
    vec3 color = vec3(1);
    if(u_mode == MODE_ALPHA_SLIDER) {
        alpha = 1.0 - uv.x;
    } else{
        float h = 0.0;
        float s = 1.0;
        float v = 1.0;
        if(u_mode == MODE_HSV_WINDOW) {
            h = mod(u_hue,360.0);
            s = uv.x;
            v = 1.0 - uv.y;
        } else if(u_mode == MODE_HUE_SLIDER) {
            h = uv.x * 360.0;
        } color = hsv_to_rgb(h,s,v);
    } f_color = vec4(color,alpha);
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