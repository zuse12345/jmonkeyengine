uniform sampler2D m_Texture;
uniform bool m_AlphaTest;

varying vec2 texCoord;

void main() {
    vec4 color = texture2D(m_Texture, texCoord);
    if (color.a < 0.01 && m_AlphaTest)
        discard;

    gl_FragColor = color;
}