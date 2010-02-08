#import "texture.glsllib"

#ifdef VERTEX_COLOR
varying vec4 vertColor;
#endif

#ifdef TEXTURE
uniform sampler2D m_ColorMap;
varying vec2 texCoord;
#endif


void main(){
    vec4 color = vec4(1.0);
    #ifdef TEXTURE
    color *= Texture_GetColor(m_ColorMap, texCoord);
    #endif
    #ifdef VERTEX_COLOR
    color.rgb *= vertColor.rgb;
    #endif
    // premultiply alpha
    color.rgb *= color.a;

    gl_FragColor = color;
}