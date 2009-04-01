package com.g3d.scene.material;

import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.shader.Shader;
import com.g3d.texture.Texture;

public class TextureMaterial extends Material {

    private Texture tex;

    public TextureMaterial(){
        shader = new Shader();
        shader.addSource(Shader.ShaderType.Vertex,
                "uniform mat4 g_WorldViewProjectionMatrix;\n" +
                "in vec4 inPosition;\n" +
                "in vec2 inTexCoord;" +
                "varying vec2 texCoord;\n" +
                "\n" +
                "void main(){\n" +
                "    gl_Position = g_WorldViewProjectionMatrix * inPosition;\n" +
                "    texCoord = inTexCoord;\n" +
                "}\n");
        shader.addSource(Shader.ShaderType.Fragment,
                "varying vec2 texCoord;\n" +
                "uniform sampler2D colorMap;\n" +
                "\n" +
                "void main(){\n" +
                "    gl_FragColor = texture2D(colorMap, texCoord);\n" +
                "}\n");
        shader.getUniform("colorMap").setInt(0);
    }

    public TextureMaterial(Texture tex) {
        this();
        setTexture(tex);
    }

    public void setTexture(Texture tex){
        this.tex = tex;
    }

    @Override
    public void apply(Geometry g, Renderer r){
        super.apply(g, r);
        r.clearTextureUnits();
        r.setTexture(0, tex);
//        r.setTexture("colorMap", tex);
    }
}
