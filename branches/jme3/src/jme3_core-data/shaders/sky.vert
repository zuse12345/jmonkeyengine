uniform mat4 g_ViewMatrix;
uniform mat4 g_ProjectionMatrix;
uniform mat3 g_NormalMatrix;

uniform vec3 m_NormalScale;

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 direction;

void main(){
    vec4 pos = vec4(inPosition, 1.0);

    // rotation only
    mat3 mat = mat3(g_ViewMatrix);

    //gl_Position = g_WorldViewProjectionMatrix * pos;
    gl_Position = g_ProjectionMatrix * vec4(mat * pos.xyz, 1.0);

    direction = normalize(inNormal * m_NormalScale);
}
