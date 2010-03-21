package com.jme3.material;

import com.jme3.material.MaterialDef.MatParam;
import com.jme3.material.MaterialDef.MatParamType;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.asset.AssetManager;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material.MatParamValue;
import com.jme3.shader.DefineList;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderKey;
import com.jme3.shader.Uniform;
import com.jme3.shader.UniformBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a technique instance.
 */
public class Technique implements Savable {

    private static final Logger logger = Logger.getLogger(Technique.class.getName());

    private TechniqueDef def;
    private Material owner;
    private ArrayList<Uniform> worldBindUniforms;
    private DefineList defines;
    private Shader shader;
    private boolean needReload = true;

    public Technique(Material owner, TechniqueDef def){
        this.owner = owner;
        this.def = def;
        if (def.isUsingShaders()){
            this.worldBindUniforms = new ArrayList<Uniform>();
            this.defines = new DefineList();
        }
    }

    public Technique(){
    }

    public void write(JmeExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(def, "def", null);
        // TODO:
        // oc.write(owner, "owner", null);
        oc.writeSavableArrayList(worldBindUniforms, "worldBindUniforms", null);
        oc.write(defines, "defines", null);
        oc.write(shader, "shader", null);
    }

    public void read(JmeImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        def = (TechniqueDef) ic.readSavable("def", null);
        worldBindUniforms = ic.readSavableArrayList("worldBindUniforms", null);
        defines = (DefineList) ic.readSavable("defines", null);
        shader = (Shader) ic.readSavable("shader", null);
    }

    public TechniqueDef getDef() {
        return def;
    }

    public Shader getShader() {
        return shader;
    }

    public List<Uniform> getWorldBindUniforms() {
        return worldBindUniforms;
    }

    public void setUserDefine(String defineName, String value){
        String prevVal = defines.get(defineName);
        if (value == null && prevVal != null){
            // clear define
            defines.remove(defineName);
            needReload = true;
        }else if (prevVal == null || !prevVal.equals(value)){
            // set define
            defines.set(defineName, value);
            needReload = true;
        }
    }
    
    /**
     * @param paramName
     */
    public void notifySetParam(String paramName, MatParamType type, Object value){
        String defineName = def.getShaderParamDefine(paramName);
        if (defineName != null){
            setUserDefine(defineName, value != null ? "1" : null);
        }
        if (shader != null){
            updateUniformParam(paramName, type, value);
        }
    }

    void updateUniformParam(String paramName, MatParamType type, Object value){
        Uniform u = shader.getUniform(paramName);
        switch (type){
            case Boolean: u.setBoolean( (Boolean)value ); break;
            case Float:   u.setFloat( (Float) value );    break;
            case Texture2D: // fall intentional
            case Texture3D:
            case TextureArray:
            case TextureCubeMap:
            case Int:     u.setInt( (Integer) value );    break;
            case Matrix3: u.setMatrix3( (Matrix3f) value ); break;
            case Matrix4: u.setMatrix4( (Matrix4f) value ); break;
            case Vector2: u.setVector2( (Vector2f) value ); break;
            case Vector3: u.setVector3( (Vector3f) value ); break;
            case Vector4: u.setColor( (ColorRGBA) value ); break;
        }
    }

    public boolean isNeedReload(){
        return needReload;
    }

    /**
     * Prepares the technique for use by loading the shader and setting
     * the proper defines based on material parameters.
     */
    public void makeCurrent(AssetManager manager){
        // check if reload is needed..
        if (def.isUsingShaders()){
            DefineList newDefines = new DefineList();
            Collection<MatParam> params = owner.getParams();
            for (MatParam param : params){
                String defineName = def.getShaderParamDefine(param.getName());
                if (defineName != null){
                    if (param instanceof MatParamValue){
                        MatParamValue paramVal = (MatParamValue) param;
                        switch (paramVal.getType()){
                            case Boolean:
                                if ( ((Boolean) paramVal.getValue()).booleanValue() )
                                    newDefines.set(defineName, "1");
                                break;
                            case Float:
                            case Int:
                                newDefines.set(defineName, paramVal.getValue().toString());
                                break;
                            default:
                                newDefines.set(defineName, "1");
                                break;
                        }
                    }else{
                        newDefines.set(defineName, "1");
                    }
                }
            }

            if (!needReload && defines.getCompiled().equals(newDefines.getCompiled())){
                newDefines = null;
                // defines have not been changed..
            }else{
                defines.clear();
                defines.addFrom(newDefines);
                // defines changed, recompile needed
                loadShader(manager);
            }
        }
    }

    private void loadShader(AssetManager manager){
        // recompute define list
        DefineList allDefines = new DefineList();
        allDefines.addFrom(def.getShaderPresetDefines());
        allDefines.addFrom(defines);

        ShaderKey key = new ShaderKey(def.getVertName(),
                                                  def.getFragName(),
                                                  allDefines,
                                                  def.getShaderLanguage());
        shader = manager.loadShader(key);
        if (shader == null){
            logger.warning("Failed to reload shader!");
            return;
        }

        // register the world bound uniforms
        worldBindUniforms.clear();
        for (UniformBinding binding : def.getWorldBindings()){
            Uniform uniform = shader.getUniform("g_"+binding.name());
            uniform.setBinding(binding);
            if (uniform != null)
                worldBindUniforms.add(uniform);
        }

        needReload = false;
    }

}
