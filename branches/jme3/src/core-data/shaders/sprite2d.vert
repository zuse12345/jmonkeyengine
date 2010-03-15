uniform mat4 g_WorldOrthoMatrix;

attribute vec4 inPosition;
attribute vec2 inTexCoord;

varying vec2 texCoord;

void main() {
    vec2 pos = (g_WorldOrthoMatrix * inPosition).xy;
    texCoord = inTexCoord;
    gl_Position = vec4(pos, 0.0, 1.0);
}