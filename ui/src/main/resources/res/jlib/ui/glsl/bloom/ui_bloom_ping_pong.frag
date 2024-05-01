layout (location=0) out vec4 f_color;

in vec2 uv;

uniform int u_horizontal;
uniform sampler2D u_sampler2D;
// Effective bi-linear Gusian Blur
//https://www.rastergrid.com/blog/2010/09/efficient-gaussian-blur-with-linear-sampling/
const float weight[3] = { 0.2270270270, 0.3162162162, 0.0702702703 };
const float offset[3] = { 0.0, 1.3846153846, 3.2307692308 };

void main() {
    vec2 texture_size = textureSize(u_sampler2D,0);
    vec3 color = texture(u_sampler2D, uv).rgb * weight[0];
    if(u_horizontal > 0) {
        for(int i = 1; i < 3; ++i) {
            color += texture2D(u_sampler2D, ((gl_FragCoord.xy + vec2(offset[i], 0.0)) / texture_size)).rgb * weight[i];
            color += texture2D(u_sampler2D, ((gl_FragCoord.xy - vec2(offset[i], 0.0)) / texture_size)).rgb * weight[i];
        }
    } else{
        for(int i = 1; i < 3; ++i) {
            color += texture2D(u_sampler2D, ((gl_FragCoord.xy + vec2(0.0, offset[i])) / texture_size)).rgb * weight[i];
            color += texture2D(u_sampler2D, ((gl_FragCoord.xy - vec2(0.0, offset[i])) / texture_size)).rgb * weight[i];
        }
    } f_color = vec4(color, 1.0);
}

