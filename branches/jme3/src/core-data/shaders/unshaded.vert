uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;

#ifdef VERTEX_COLOR
attribute vec4 inColor;
varying vec4 vertColor;
#endif
#ifdef TEXTURE
attribute vec2 inTexCoord;
varying vec2 texCoord;
#endif

void main(){
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    #ifdef VERTEX_COLOR
    vertColor = inColor;
    #endif
    #ifdef TEXTURE
    texCoord = inTexCoord;
    #endif
}