uniform sampler2D m_Texture;

varying vec2 texCoord;

void main() {
    float fontColor = texture2D(m_Texture,texCoord).r;
    gl_FragColor = vec4(fontColor, fontColor, fontColor, fontColor);
}

