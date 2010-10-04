uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat4 g_ViewMatrix;

varying vec2 texCoord;

attribute vec3 inPosition;
attribute vec2 inTexCoord;

varying vec3 vViewDir;
varying vec4 wvLightPos;

void main(){
   texCoord = inTexCoord;
   
   vec4 pos = vec4(inPosition, 1.0);
   gl_Position = vec4(sign(pos.xy-vec2(0.5)), 0.0, 1.0);
}