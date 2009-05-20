package com.g3d.material;

import com.g3d.renderer.Renderer;
import com.g3d.shader.Uniform;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MaterialDef {

    private static final Logger logger = Logger.getLogger(MaterialDef.class.getName());

    public enum MatParamType {
        Float(false),
        Vector2(false),
        Vector3(false),
        Vector4(false),

        Int(false),
        Int2(false),
        Int3(false),
        Int4(false),

        Matrix2(false),
        Matrix3(false),
        Matrix4(false),

        Texture1D(true),
        Texture2D(true),
        Texture3D(true),
        TextureCube(true),
        TextureArray(true);
        
        private boolean textureType;

        MatParamType(boolean textureType){
            this.textureType = textureType;
        }

        public boolean isTextureType() {
            return textureType;
        }
    }

    public static class MatParam {
        
        final MatParamType type;
        final String name;

        public MatParam(MatParamType type, String name){
            this.type = type;
            this.name = name;
        }
        public MatParamType getType() {
            return type;
        }
        public String getName(){
            return name;
        }
    }

    private String name;
    private final Map<String, Technique> techniques = new HashMap<String, Technique>();
    private final Map<String, MatParam> matParams = new HashMap<String, MatParam>();
    
    public MaterialDef(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void addMaterialParam(MatParamType type, String name) {
        matParams.put(name, new MatParam(type,name));
    }
    
    public MatParam getMaterialParam(String name){
        return matParams.get(name);
    }

    public void addTechnique(Technique technique){
        techniques.put(technique.getName(), technique);
    }

    public Technique getTechnique(String name) {
        return techniques.get(name);
    }

}
