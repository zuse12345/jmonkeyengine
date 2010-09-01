/*
GLSL conversion of Michael Horsch water demo
http://www.bonzaisoftware.com/wfs.html
Converted by Mars_999
8/20/2005
*/
uniform vec3 m_lightPos;
uniform float m_time;

uniform mat4 g_WorldViewProjectionMatrix;
uniform vec3 g_CameraPosition;

attribute vec4 inPosition;
attribute vec2 inTexCoord;

varying vec4 lightDir;
varying vec4 waterTex1;
varying vec4 waterTex2;
varying vec4 position;
varying vec4 viewDir;
varying vec4 viewpos;


//unit 0 = water_reflection
//unit 1 = water_refraction
//unit 2 = water_normalmap
//unit 3 = water_dudvmap
//unit 4 = water_depthmap

void main(void)
{
    viewpos.x = g_CameraPosition.x;
    viewpos.y = g_CameraPosition.y;
    viewpos.z = g_CameraPosition.z;
    viewpos.w = 1.0;

    vec4  temp;
    vec4 tangent = vec4(1.0, 0.0, 0.0, 0.0);
    vec4 norm = vec4(0.0, 1.0, 0.0, 0.0);
    vec4 binormal = vec4(0.0, 0.0, 1.0, 0.0);

    temp = viewpos - inPosition;

    viewDir.x = dot(temp, tangent);
    viewDir.y = dot(temp, binormal);
    viewDir.z = dot(temp, norm);
    viewDir.w = 0.0;

    temp = vec4(m_lightPos,1.0);//- inPosition;
    lightDir.x = dot(temp, tangent);
    lightDir.y = dot(temp, binormal);
    lightDir.z = dot(temp, norm);
    lightDir.w = 0.0;

  

    vec4 t1 = vec4(0.0, -m_time, 0.0,0.0);
    vec4 t2 = vec4(0.0, m_time, 0.0,0.0);

    waterTex1 =vec4(inTexCoord,0.0,0.0) + t1;
    waterTex2 =vec4(inTexCoord ,0.0,0.0)+ t2;

    position = g_WorldViewProjectionMatrix * inPosition;
    gl_Position = position;
}
