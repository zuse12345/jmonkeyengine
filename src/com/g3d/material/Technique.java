package com.g3d.material;

import com.g3d.material.Material.MatParamTextureValue;
import com.g3d.material.Material.MatParamValue;
import com.g3d.material.MaterialDef.MatParam;
import com.g3d.material.MaterialDef.MatParamType;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix3f;
import com.g3d.math.Matrix4f;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.res.ContentManager;
import com.g3d.shader.DefineList;
import com.g3d.shader.Shader;
import com.g3d.shader.ShaderMasterKey;
import com.g3d.shader.Uniform;
import com.g3d.shader.UniformBinding;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a technique instance.
 */
public class Technique {

    private static final Logger logger = Logger.getLogger(Technique.class.getName());

    private final TechniqueDef def;
    private final Material owner;
    private final List<Uniform> worldBindUniforms = new ArrayList<Uniform>();
    private final DefineList defines = new DefineList();
    private Shader shader;
    private boolean needReload = true;

    public Technique(Material owner, TechniqueDef def){
        this.owner = owner;
        this.def = def;
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
    public void makeCurrent(ContentManager manager){
        // check if reload is needed..
        DefineList newDefines = new DefineList();
        Collection<MatParam> params = owner.getParams();
        for (MatParam param : params){
            String defineName = def.getShaderParamDefine(param.getName());
            if (defineName != null){
                newDefines.set(defineName, "1");
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

    public void loadShader(ContentManager manager){
        // recompute define list
        DefineList allDefines = new DefineList();
        allDefines.addFrom(def.getShaderPresetDefines());
        allDefines.addFrom(defines);

        ShaderMasterKey key = new ShaderMasterKey(def.getVertName(),
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
