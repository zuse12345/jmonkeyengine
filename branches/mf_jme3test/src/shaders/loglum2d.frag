uniform sampler2D m_Input;

varying vec2 texCoord;

float lum(vec3 color){
    return dot(color,vec3(0.2125, 0.7154, 0.0721));
}

void main() {
    vec4 color = texture2D(m_Input, texCoord);
    gl_FragColor = vec4(lum(color.rgb));
}

