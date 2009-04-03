package com.g3d.scene.material;

import com.g3d.math.ColorRGBA;
import com.g3d.shader.Shader;
import com.g3d.shader.Shader.ShaderType;

/**
 * Color material transforms geometry into clip space and colors them with
 * the color set with setColor().
 */
public class ColorMaterial extends Material {

    public ColorMaterial(){
        shader = new Shader();
        shader.addSource(ShaderType.Vertex,
                          "uniform mat4 g_WorldViewProjectionMatrix;\n" +
                          "attribute vec4 inPosition;\n" +
                          "\n" +
                          "void main(){\n" +
                          "    gl_Position = g_WorldViewProjectionMatrix * inPosition;\n" +
                          "}\n");
        shader.addSource(ShaderType.Fragment,
                          "uniform vec4 g_Color;" +
                          "" +
                          "void main(){\n" +
                          "   gl_FragColor = g_Color;\n" +
                          "}\n");
    }

    public ColorMaterial(ColorRGBA color){
        this();
        setColor(color);
    }

    public void setColor(ColorRGBA color){
        shader.getUniform("g_Color").setColor(color);
    }

}
