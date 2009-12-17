package com.g3d.material;

import com.g3d.renderer.Renderer;
import com.g3d.asset.AssetManager;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.shader.Uniform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MaterialDef implements Savable {

    private static final Logger logger = Logger.getLogger(MaterialDef.class.getName());

    public enum MatParamType {
        Float(false),
        Vector2(false),
        Vector3(false),
        Vector4(false),

        Int(false),

        Boolean(false),

        Matrix2(false),
        Matrix3(false),
        Matrix4(false),

        TextureBuffer(true),
        Texture2D(true),
        Texture3D(true),
        TextureCubeMap(true),
        TextureArray(true);
        
        private boolean textureType;

        MatParamType(boolean textureType){
            this.textureType = textureType;
        }

        public boolean isTextureType() {
            return textureType;
        }
    }

    public static class MatParam implements Savable {
        
        private MatParamType type;
        private String name;

        public MatParam(MatParamType type, String name){
            this.type = type;
            this.name = name;
        }

        public MatParam(){
        }

        public MatParamType getType() {
            return type;
        }
        public String getName(){
            return name;
        }

        public void write(G3DExporter ex) throws IOException{
            OutputCapsule oc = ex.getCapsule(this);
            oc.write(type, "type", null);
            oc.write(name, "name", null);
        }

        public void read(G3DImporter im) throws IOException{
            InputCapsule ic = im.getCapsule(this);
            type = ic.readEnum("type", MatParamType.class, null);
            name = ic.readString("name", null);
        }

        @Override
        public boolean equals(Object other){
            if (!(other instanceof MatParam))
                return false;

            MatParam otherParam = (MatParam) other;
            return otherParam.type == type &&
                   otherParam.name.equals(name);
        }

        @Override
        public String toString(){
            return type.name()+" "+name;
        }
    }

    private String name;
    private AssetManager assetManager;
    private Map<String, TechniqueDef> techniques;
    private Map<String, MatParam> matParams;

    public MaterialDef(){
    }
    
    public MaterialDef(AssetManager assetManager, String name){
        this.assetManager = assetManager;
        this.name = name;
        techniques = new HashMap<String, TechniqueDef>();
        matParams = new HashMap<String, MatParam>();
    }

    public void write(G3DExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name, "name", null);
        oc.writeStringSavableMap(techniques, "techniques", null);
        oc.writeStringSavableMap(matParams, "matParams", null);
    }

    public void read(G3DImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        name = ic.readString("name", null);
        techniques = (Map<String, TechniqueDef>) ic.readStringSavableMap("techniques", null);
        matParams = (Map<String, MatParam>) ic.readStringSavableMap("matParams", null);
        assetManager = im.getAssetManager();
    }

    public AssetManager getAssetManager(){
        return assetManager;
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

    public void addTechniqueDef(TechniqueDef technique){
        techniques.put(technique.getName(), technique);
    }

    public TechniqueDef getTechniqueDef(String name) {
        return techniques.get(name);
    }

}
