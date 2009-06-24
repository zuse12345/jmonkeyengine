#import "texture.glsllib"

varying vec2 texCoord;

uniform sampler2D m_ColorMap;

void main(){ 
    gl_FragColor = Texture_GetColor(m_ColorMap, texCoord);
}