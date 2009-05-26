varying vec2 texCoord;

uniform sampler2D m_Texture;

void main() {
    vec4 color = texture2D(m_Texture, texCoord);
    if (color.a < 0.01)
        discard;

    gl_FragColor = color;
}