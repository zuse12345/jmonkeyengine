package com.g3d.scene.material;

import com.g3d.shader.Shader;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;

/**
 * A material describes how the surface of geoemtry should be shaded.
 * A material contains a shader and parameters that are applied to it.
 */
public abstract class Material {

    protected Shader shader;

    public Material(Shader shader){
        this.shader = shader;
    }

    public Material(){
        this.shader = null;
    }

    public void apply(Geometry g, Renderer r){
        if (shader != null)
            r.setShader(shader);
    }
    
}
