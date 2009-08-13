#import "texture.glsllib"

varying vec2 texCoord;

uniform sampler2D m_ColorMap;

void main(){
    //Texture_GetColor(m_ColorMap, texCoord)
    //vec4 color = texture2D(m_ColorMap, texCoord);
    //color.rgb *= color.a;
    //gl_FragColor = vec4(color.a);
    gl_FragColor = Texture_GetColor(m_ColorMap, texCoord);
}