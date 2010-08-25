/*
GLSL conversion of Michael Horsch water demo
http://www.bonzaisoftware.com/wfs.html
Converted by Mars_999
8/20/2005
*/
uniform mat4 g_WorldViewProjectionMatrix;
attribute vec4 inPosition;
attribute vec2 inTexCoord;
varying vec4 waterTex0;
varying vec4 waterTex1;
varying vec4 waterTex2;
varying vec4 waterTex3;
varying vec4 waterTex4;
varying vec4 viewpos;
uniform vec3 g_CameraPosition;
uniform vec4 m_lightpos;
uniform float m_time, m_time2;
//unit 0 = water_reflection
//unit 1 = water_refraction
//unit 2 = water_normalmap
//unit 3 = water_dudvmap
//unit 4 = water_depthmap

void main(void)
{
    viewpos.x = g_CameraPosition.x;
    viewpos.y = g_CameraPosition.x;
    viewpos.z = g_CameraPosition.x;
    viewpos.w = 1.0;

    vec4 mpos, temp;
    vec4 tangent = vec4(1.0, 0.0, 0.0, 0.0);
    vec4 norm = vec4(0.0, 1.0, 0.0, 0.0);
    vec4 binormal = vec4(0.0, 0.0, 1.0, 0.0);



    temp = viewpos - inPosition;
    waterTex4.x = dot(temp, tangent);
    waterTex4.y = dot(temp, binormal);
    waterTex4.z = dot(temp, norm);
    waterTex4.w = 0.0;

    temp = m_lightpos - inPosition;
    waterTex0.x = dot(temp, tangent);
    waterTex0.y = dot(temp, binormal);
    waterTex0.z = dot(temp, norm);
    waterTex0.w = 0.0;

    mpos = g_WorldViewProjectionMatrix * inPosition;

    vec4 t1 = vec4(0.0, -m_time, 0.0,0.0);
    vec4 t2 = vec4(0.0, -m_time2, 0.0,0.0);

    waterTex1 =vec4(inTexCoord,0.0,0.0) + t1;
    waterTex2 =vec4(inTexCoord ,0.0,0.0)+ t2;

    waterTex3 = mpos;

    gl_Position =mpos;
}
