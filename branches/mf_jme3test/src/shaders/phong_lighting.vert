uniform mat4 g_WorldViewProjectionMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_WorldViewMatrix;

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec3 inNormal;
attribute vec3 inTangent;
attribute vec3 inBinormal;

varying vec3 wvNormal;
varying vec3 wvTangent;
varying vec3 wvPosition;

varying vec2 texCoord;
varying vec3 viewDir;

void main(){
   vec4 pos = vec4(inPosition, 1.0);
   gl_Position = g_WorldViewProjectionMatrix * pos;
   wvPosition = (g_WorldViewMatrix * pos).xyz;
   texCoord = inTexCoord;

   wvNormal = normalize(g_NormalMatrix * inNormal);
   wvTangent = normalize(g_NormalMatrix * inTangent);

   viewDir = normalize(-wvPosition);
}





















