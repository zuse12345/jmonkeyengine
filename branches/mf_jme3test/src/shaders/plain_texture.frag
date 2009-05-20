varying vec2 texCoord;

uniform sampler2D m_ColorMap;

void main(){ 
    gl_FragColor = texture2D(m_ColorMap, texCoord);
}