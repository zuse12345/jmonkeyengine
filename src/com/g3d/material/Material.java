package com.g3d.material;

import com.g3d.material.MaterialDef.MatParam;
import com.g3d.material.MaterialDef.MatParamType;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix4f;
import com.g3d.math.Vector2f;
import com.g3d.renderer.Renderer;
import com.g3d.res.ContentManager;
import com.g3d.scene.Geometry;
import com.g3d.shader.Shader;
import com.g3d.texture.Texture;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Material {

    private final MaterialDef def;
    private final Map<String, MatParam> paramValues = new HashMap<String, MatParam>();
//    private final Map<String, MatParamValue> paramValues = new HashMap<String, MatParamValue>();
//    private final Map<String, MatParamTextureValue> texValues = new HashMap<String, MatParamTextureValue>();

    private Technique technique;
    private Map<String, Technique> techniques = new HashMap<String, Technique>();
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

    public static class MatParamTextureValue extends MatParam {

        private Texture value;
        private int unit;

        public MatParamTextureValue(MatParamType type, String name, Texture value, int unit){
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

    public Technique getActiveTechnique() {
        return technique;
    }

    /**
     * Should only be used in Technique.makeCurrent()
     * @param tech
     */
    public void setActiveTechnique(Technique tech){
        technique = tech;
    }

    public MaterialDef getMaterialDef(){
        return def;
    }

    public void selectTechnique(String name){
        // check if already created
        Technique tech = techniques.get(name);
        if (tech == null){
            // create technique instance
            TechniqueDef techDef = def.getTechniqueDef(name);
            tech = new Technique(this, techDef);
            techniques.put(name, tech);
        }else if (technique == tech){
            // attempting to switch to an already
            // active technique.
            return;
        }
        tech.makeCurrent(def.getContentManager());
        technique = tech;
    }

    public MatParamValue getParam(String name){
        MatParam param = paramValues.get(name);
        if (param instanceof MatParamValue)
            return (MatParamValue) param;

        return null;
    }

    public MatParamTextureValue getTextureParam(String name){
        MatParam param = paramValues.get(name);
        if (param instanceof MatParamTextureValue)
            return (MatParamTextureValue) param;

        return null;
    }

    public Collection<MatParam> getParams(){
        return paramValues.values();
    }

    public void setParam(String name, MatParamType type, Object value){
        MatParamValue val = getParam(name);
        if (technique != null){
            technique.notifySetParam(name, type, value);
        }
        if (val == null)
            paramValues.put(name, new MatParamValue(type, name, value));
        else
            val.setValue(value);
    }

    public void setTextureParam(String name, MatParamType type, Texture value){
        MatParamTextureValue val = getTextureParam(name);

        if (val == null)
            paramValues.put(name, new MatParamTextureValue(type, name, value, nextTexUnit++));
        else
            val.setValue(value);

        if (technique != null){
            technique.notifySetParam(name, type, nextTexUnit-1);
        }
    }

    public void setTexture(String name, Texture value){
        if (value.getType() == Texture.Type.TwoDimensional){
            setTextureParam(name, MatParamType.Texture2D, value);
        }else if (value.getType() == Texture.Type.CubeMap){
            setTextureParam(name, MatParamType.TextureCubeMap, value);
        }
    }

    public void setMatrix4(String name, Matrix4f value) {
        setParam(name, MatParamType.Matrix4, value);
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

    public void setVector2(String name, Vector2f value) {
        setParam(name, MatParamType.Vector2, value);
    }

    /**
     * Should be called after selectTechnique()
     * @param geom
     * @param r
     */
    public void apply(Geometry geom, Renderer r){
        if (technique == null)
            selectTechnique("Default");

        TechniqueDef techDef = technique.getDef();
        if (techDef.getRenderState() != null)
            r.applyRenderState(techDef.getRenderState());
        else
            r.applyRenderState(RenderState.DEFAULT);

        Shader shader = technique.getShader();

        // send lighting information, if needed
        if (techDef.isUsingLighting()){
            r.updateLightListUniforms(shader, geom, 4);
        }else{
            // tell renderer not to use lighting for this material
            r.updateLightListUniforms(shader, geom, 0);
        }

        // update camera and world matrices
        // NOTE: setWorldTransform should have been called already
        r.updateWorldParameters(technique.getWorldBindUniforms());

        // setup textures
        Collection<MatParam> params = paramValues.values();
        for (MatParam param : params){
            if (param instanceof MatParamTextureValue){
                MatParamTextureValue texParam = (MatParamTextureValue) param;
                r.setTexture(texParam.getUnit(), texParam.getValue());
                technique.updateUniformParam(texParam.getName(),
                                             texParam.getType(),
                                             texParam.getUnit());
            }else{
                MatParamValue valParam = (MatParamValue) param;
                technique.updateUniformParam(valParam.getName(),
                                             valParam.getType(),
                                             valParam.getValue());
            }
        }

        // upload and bind shader
        r.setShader(shader);
    }

}
