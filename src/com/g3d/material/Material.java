package com.g3d.material;

import com.g3d.material.MaterialDef.MatParam;
import com.g3d.material.MaterialDef.MatParamType;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Renderer;
import com.g3d.res.ContentManager;
import com.g3d.scene.Geometry;
import com.g3d.shader.Shader;
import com.g3d.shader.Uniform;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;
import java.util.HashMap;
import java.util.Map;

public class Material {

    private final MaterialDef def;
    private final Map<String, MatParamValue> paramValues = new HashMap<String, MatParamValue>();
    private final Map<String, MatParamTexture> texValues = new HashMap<String, MatParamTexture>();

    private Technique technique;
    private int nextTexUnit = 0;

    public static class MatParamValue extends MatParam {
        private Object value;
        public MatParamValue(MatParamType type, String name, Object value){
            super(type, name);
            this.value = value;
        }
        public Object getValue(){
            return value;
        }
        public void setValue(Object value){
            this.value = value;
        }
    }

    public static class MatParamTexture extends MatParam {
        private Texture value;
        private int unit;
        public MatParamTexture(MatParamType type, String name, Texture value, int unit){
            super(type, name);
            this.value = value;
            this.unit = unit;
        }
        public Texture getValue(){
            return value;
        }
        public void setValue(Texture value){
            this.value = value;
        }
        public int getUnit() {
            return unit;
        }
    }

    public Material(MaterialDef def){
        if (def == null)
            throw new NullPointerException("Material definition cannot be null");

        this.def = def;
    }

    public Material(ContentManager contentMan, String defName){
        this(contentMan.loadMaterialDef(defName));
    }

    public MaterialDef getMaterialDef(){
        return def;
    }

    public void setParam(String name, MatParamType type, Object value){
        MatParamValue val = paramValues.get(name);
        if (val == null)
            paramValues.put(name, new MatParamValue(type, name, value));
        else
            val.setValue(value);
    }

    public void setTextureParam(String name, MatParamType type, Texture value){
        MatParamTexture val = texValues.get(name);
        if (val == null)
            texValues.put(name, new MatParamTexture(type, name, value, nextTexUnit++));
        else
            val.setValue(value);
    }

    public void setTexture(String name, Texture value){
        if (value instanceof Texture2D){
            setTextureParam(name, MatParamType.Texture2D, value);
        }
    }

    public void setBoolean(String name, boolean value){
        setParam(name, MatParamType.Boolean, value);
    }

    public void setFloat(String name, float value){
        setParam(name, MatParamType.Float, value);
    }

    public void setColor(String name, ColorRGBA value){
        setParam(name, MatParamType.Vector4, value);
    }

    public void apply(Geometry geom, Renderer r){
        if (technique == null){
            // not initialized yet
            // select technique
            technique  = def.getTechnique("Default");

            Shader shader = technique.getShader();

            // write params to technique's shader
            for (MatParamValue value : paramValues.values()){
                Uniform uniform = shader.getUniform(value.getName());
                switch (value.getType()){
                    case Float:
                        uniform.setFloat((Float)value.getValue());
                        break;
                    case Boolean:
                        uniform.setBoolean((Boolean)value.getValue());
                        break;
                    case Vector2:
                        uniform.setVector2((Vector2f)value.getValue());
                        break;
                    case Vector3:
                        uniform.setVector3((Vector3f)value.getValue());
                        break;
                    case Vector4:
                        uniform.setColor((ColorRGBA)value.getValue());
                        break;
                    case Int:
                        uniform.setInt((Integer)value.getValue());
                        break;

                }
            }
            for (MatParamTexture tex : texValues.values()){
                Uniform uniform = shader.getUniform(tex.getName());
                switch (tex.getType()){
                    case Texture2D:
                        uniform.setInt(tex.getUnit());
                        break;
                }
            }
        }

        Shader shader = technique.getShader();

        // send lighting information, if needed
        if (technique.isUsingLighting()){
            r.updateLightListUniforms(shader, geom, 4);
        }

        // update camera and world matrices
        // NOTE: setWorldTransform should have been called already
        r.updateWorldParameters(technique.getWorldParams());

        // setup textures
        for (MatParamTexture tex : texValues.values()){
            r.setTexture(tex.getUnit(), tex.getValue());
        }

        // upload and bind shader
        r.setShader(shader);
    }

}
