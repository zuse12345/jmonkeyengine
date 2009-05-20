package com.g3d.shader;

public enum UniformBinding {
    WorldMatrix("g_WorldMatrix"),
    ViewMatrix("g_ViewMatrix"),
    ProjectionMatrix("g_ProjectionMatrix"),
    WorldViewMatrix("g_WorldViewMatrix"),
    NormalMatrix("g_NormalMatrix"),
    WorldViewProjectionMatrix("g_WorldViewProjectionMatrix"),

    WorldMatrixInverse("g_WorldMatrixInverse"),
    ViewMatrixInverse("g_ViewMatrixInverse"),
    ProjectionMatrixInverse("g_ProjectionMatrixInverse"),
    WorldViewMatrixInverse("g_WorldViewMatrixInverse"),
    NormalMatrixInverse("g_NormalMatrixInverse"),
    WorldViewProjectionMatrixInverse("g_WorldViewProjectionMatrixInverse"),

    Time("g_Time"),
    Tpf("g_TPF"),
    FrameRate("g_FrameRate");

    String varName;

    UniformBinding(String name){
        varName = name;
    }

    public String getVarName(){
        return varName;
    }

}
