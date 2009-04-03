package com.g3d.scene.material;

import com.g3d.shader.Shader;
import com.g3d.shader.Shader.ShaderType;

public class HeightMaterial extends Material {

    public HeightMaterial(){
        shader = new Shader();
        shader.addSource(ShaderType.Vertex,
                          "uniform mat4 g_WorldViewProjectionMatrix;\n" +
                          "attribute vec4 inPosition;\n" +
                          "uniform float g_MaxHeight;\n" +
                          "varying float height;\n" +
                          "\n" +
                          "void main(){\n" +
                          "    gl_Position = g_WorldViewProjectionMatrix * inPosition;" +
                          "    height = clamp(inPosition.y / g_MaxHeight, 0.0, 1.0);\n" +
                          "}\n");
        shader.addSource(ShaderType.Fragment,
                          "varying float height;" +
                          "\n" +
                          "void main(){\n" +
                          "   gl_FragColor = vec4(height);\n" +
                          "}\n");
        setMaxHeight((float) Math.pow(2, 7));
    }

    public void setMaxHeight(float maxHeight){
        shader.getUniform("g_MaxHeight").setFloat(maxHeight);
    }

}
