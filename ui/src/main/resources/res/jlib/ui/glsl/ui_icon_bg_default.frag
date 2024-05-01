layout (location=0) out vec4 f_color;
in vec2 uv;

#define BORDER 0.6

vec2 _ndc(vec2 uv) { return uv * 2.0 - 1.0; }

void main() {
    vec2 fc = 1.0 - smoothstep(vec2(BORDER), vec2(1.0), abs(_ndc(uv)));
    float fact = fc.x * fc.y;
    f_color = vec4(1.0,1.0,1.0,smoothstep(0.0,1.0,fact));
}

