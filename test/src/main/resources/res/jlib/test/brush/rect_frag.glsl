#version 440

// blending disbled
layout (location=0) out vec4 f_color;

in VS_OUT {
vec2 uv;
} fs_in;

uniform sampler2D u_source_rgba;

void main() {

    float alpha = texture(u_source_rgba,fs_in.uv).a;
    if(alpha > 0.1) {
        f_color = vec4(1.0,0.0,0.0,1.0);
    }
    discard;

}
