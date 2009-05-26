uniform mat4 g_WorldMatrix;

attribute vec2 inPosition;
attribute vec2 inTexCoord;

uniform float g_Aspect;

varying vec2 texCoord;

void main() {
    vec2 pos = inPosition;
    pos.x /= g_Aspect;
    gl_Position = g_WorldMatrix * vec4(pos, 0.0, 1.0);
    texCoord = inTexCoord;
}
