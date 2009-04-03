package com.g3d.scene.material;

import com.g3d.shader.Shader;
import com.g3d.shader.Shader.ShaderType;

public class NormalMaterial extends Material {

    public NormalMaterial(){
        shader = new Shader();
        shader.addSource(ShaderType.Vertex,
                          "uniform mat4 g_WorldViewProjectionMatrix;\n" +
                          "attribute vec4 inPosition;" +
                          "attribute vec3 inNormal;\n" +
                          "varying vec3 normal;" +
                          "\n" +
                          "void main(){\n" +
                          "    gl_Position = g_WorldViewProjectionMatrix * inPosition;" +
                          "    normal = normalize(inNormal);\n" +
                          "}\n");
        shader.addSource(ShaderType.Fragment,
                          "uniform vec4 g_Color;" +
                          "varying vec3 normal;" +
                          "" +
                          "void main(){\n" +
                          "   gl_FragColor = vec4((normal * vec3(0.5)) + vec3(0.5), 1.0);\n" +
                          "}\n");
    }

}
