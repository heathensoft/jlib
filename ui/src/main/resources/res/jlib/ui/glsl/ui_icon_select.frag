layout (location=0) out vec4 f_color;
in vec2 uv;

#define SPEED 0.7853981634
#define WRAP 2.5

uniform float u_time;
uniform float u_border_pixels;
uniform float u_radius_pixels;
uniform vec2 u_resolution;

float _rectSDF(vec2 p, vec2 b, float r)  {
    vec2 d = abs(p) - b + vec2(r);
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

float _rotFactor(vec2 ndc, float time) {
    vec2 rot = vec2(sin(time * SPEED), cos(time * SPEED));
    float dot = dot(normalize(ndc), rot);
    return clamp(dot + WRAP, 0.0, WRAP + 1.0) / (WRAP + 1.0);
}

vec2 _ndc(vec2 uv) { return uv * 2.0 - 1.0; }

void main() {
    float half_border_px = u_border_pixels / 2.0;
    vec2 half_shape_size_px = u_resolution / 2.0 - vec2(half_border_px);
    vec2 center_pos_px = gl_FragCoord.xy - u_resolution / 2.0;
    float dist = _rectSDF(center_pos_px,half_shape_size_px,u_radius_pixels - half_border_px);
    dist = clamp(1.0 - (abs(dist) - half_border_px),0.0,1.0);
    f_color = vec4(dist) * _rotFactor(_ndc(uv), u_time);
}

