//#import "common.glsllib"

attribute vec4 inPosition;

//uniform mat4 g_WorldViewMatrix;
//varying float outDepth;

#ifdef PRESHADOW_INSTANCED
uniform mat4 g_WorldViewProjectionMatrices[NUM_INSTANCES];
#else
uniform mat4 g_WorldViewProjectionMatrix;
#endif

void main(){
    #ifdef PRESHADOW_INSTANCED
    gl_Position = g_WorldViewProjectionMatrices[gl_InstanceIDARB] * inPosition;
    #else
    gl_Position = g_WorldViewProjectionMatrix * inPosition;
    #endif

    //outDepth = -(g_WorldViewMatrix * inPosition).z;
}