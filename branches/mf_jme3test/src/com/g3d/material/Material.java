package com.g3d.material;

import com.g3d.material.MaterialDef.MatParam;
import com.g3d.material.MaterialDef.MatParamType;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix4f;
import com.g3d.math.Vector2f;
import com.g3d.asset.AssetManager;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.light.DirectionalLight;
import com.g3d.light.Light;
import com.g3d.light.LightList;
import com.g3d.light.PointLight;
import com.g3d.math.Vector3f;
import com.g3d.renderer.RenderManager;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.shader.Shader;
import com.g3d.shader.Uniform;
import com.g3d.texture.Texture;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Material implements Cloneable, Savable {

    private static final RenderState additiveLight = new RenderState();

    static {
//        additiveLight.set
        additiveLight.setBlendMode(RenderState.BlendMode.Additive);
    }

    private MaterialDef def;
    private Map<String, MatParam> paramValues = new HashMap<String, MatParam>();
//    private final Map<String, MatParamValue> paramValues = new HashMap<String, MatParamValue>();
//    private final Map<String, MatParamTextureValue> texValues = new HashMap<String, MatParamTextureValue>();

    private Technique technique;
    private Map<String, Technique> techniques = new HashMap<String, Technique>();
    private int nextTexUnit = 0;
    private RenderState additionalState = null;
    private boolean transparent = false;

    public static class MatParamValue extends MatParam {

        private Object value;

        public MatParamValue(MatParamType type, String name, Object value){
            super(type, name);
            this.value = value;
        }

        public MatParamValue(){
        }

        public Object getValue(){
            return value;
        }

        public void setValue(Object value){
            this.value = value;
        }

        public void write(G3DExporter ex) throws IOException{
            super.write(ex);
            OutputCapsule oc = ex.getCapsule(this);
            // TODO: ...
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

    public Material(AssetManager contentMan, String defName){
        this(contentMan.loadMaterialDef(defName));
    }

    /**
     * Do not use this constructor. Serialization purposes only.
     */
    public Material(){
    }

    public void write(G3DExporter ex){
    }

    public void read(G3DImporter im){
    }

    @Override
    public Material clone(){
        try{
            Material mat = (Material) super.clone();
            if (additionalState != null){
                mat.additionalState = additionalState.clone();
            }
            mat.technique = null;
            mat.techniques = new HashMap<String, Technique>();
            mat.paramValues = new HashMap<String, MatParam>(paramValues);
            return mat;
        }catch (CloneNotSupportedException ex){
            throw new AssertionError();
        }
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

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public RenderState getAdditionalRenderState(){
        if (additionalState == null)
            additionalState = new RenderState();
        return additionalState;
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
        tech.makeCurrent(def.getAssetManager());
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
     * Uploads the lights in the light list as two uniform arrays.<br/><br/>
     *      * <p>
     * <code>uniform vec4 g_LightColor[numLights];</code><br/>
     * // g_LightColor.rgb is the diffuse/specular color of the light.<br/>
     * // g_Lightcolor.a is the type of light, 0 = Directional, 1 = Point, <br/>
     * // 2 = Spot. <br/>
     * <br/>
     * <code>uniform vec4 g_LightPosition[numLights];</code><br/>
     * // g_LightPosition.xyz is the position of the light (for point lights)<br/>
     * // or the direction of the light (for directional lights).<br/>
     * // g_LightPosition.w is the inverse radius (1/r) of the light (for attenuation) <br/>
     * </p>
     *
     * @param shader
     * @param lightList
     */
    public void updateLightListUniforms(Shader shader, Geometry g, int numLights){
        if (numLights == 0) // this shader does not do lighting, ignore.
            return;

        LightList lightList = g.getWorldLightList();
        Uniform lightColor = shader.getUniform("g_LightColor");
        Uniform lightPos = shader.getUniform("g_LightPosition");
        lightColor.setVector4Length(numLights);
        lightPos.setVector4Length(numLights);
        for (int i = 0; i < numLights; i++){
            if (lightList.size() <= i){
                lightColor.setVector4InArray(0f, 0f, 0f, 0f, i);
                lightPos.setVector4InArray(0f, 0f, 0f, 0f, i);
            }else{
                Light l = lightList.get(i);
                ColorRGBA color = l.getColor();
                lightColor.setVector4InArray(color.getRed(),
                                             color.getGreen(),
                                             color.getBlue(),
                                             l.getType().getId(),
                                             i);

                switch (l.getType()){
                    case Directional:
                        DirectionalLight dl = (DirectionalLight) l;
                        Vector3f dir = dl.getDirection();
                        lightPos.setVector4InArray(dir.getX(), dir.getY(), dir.getZ(), -1, i);
                        break;
                    case Point:
                        PointLight pl = (PointLight) l;
                        Vector3f pos = pl.getPosition();
                        float invRadius = pl.getRadius();
                        if (invRadius != 0){
                            invRadius = 1f / invRadius;
                        }
                        lightPos.setVector4InArray(pos.getX(), pos.getY(), pos.getZ(), invRadius, i);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown type of light: "+l.getType());
                }
            }
        }
    }



    private void renderMultipassLighting(Shader shader, Geometry g, Renderer r){
        LightList lightList = g.getWorldLightList();
        Uniform lightColor = shader.getUniform("g_LightColor");
        Uniform lightPos = shader.getUniform("g_LightPosition");

        for (int i = 0; i < lightList.size(); i++){
            if (i == 1){
                r.applyRenderState(additiveLight);
            }

            Light l = lightList.get(i);
            ColorRGBA color = l.getColor();
            lightColor.setVector4(color.getRed(),
                                  color.getGreen(),
                                  color.getBlue(),
                                  l.getType().getId());

            switch (l.getType()){
                case Directional:
                    DirectionalLight dl = (DirectionalLight) l;
                    Vector3f dir = dl.getDirection();
                    lightPos.setVector4(dir.getX(), dir.getY(), dir.getZ(), -1);
                    break;
                case Point:
                    PointLight pl = (PointLight) l;
                    Vector3f pos = pl.getPosition();
                    float invRadius = pl.getRadius();
                    if (invRadius != 0){
                        invRadius = 1f / invRadius;
                    }
                    lightPos.setVector4(pos.getX(), pos.getY(), pos.getZ(), invRadius);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown type of light: "+l.getType());
            }

            r.setShader(shader);
            r.renderMesh(g.getMesh(), 1);
        }
        
    }

    /**
     * Should be called after selectTechnique()
     * @param geom
     * @param r
     */
    public void render(Geometry geom, RenderManager rm){
        if (technique == null)
            selectTechnique("Default");
        else if (technique.isNeedReload())
            technique.makeCurrent(def.getAssetManager());

        Renderer r = rm.getRenderer();
        TechniqueDef techDef = technique.getDef();
        if (techDef.getRenderState() != null){
            r.applyRenderState(techDef.getRenderState());
            if (additionalState != null)
                r.applyRenderState(additionalState);
        }else{
            if (additionalState != null)
                r.applyRenderState(additionalState);
            else
                r.applyRenderState(RenderState.DEFAULT);
        }

        
        // update camera and world matrices
        // NOTE: setWorldTransform should have been called already
        // XXX:
        rm.updateUniformBindings(technique.getWorldBindUniforms());

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

        Shader shader = technique.getShader();

        // send lighting information, if needed
        switch (techDef.getLightMode()){
            case SinglePass:
                updateLightListUniforms(shader, geom, 4);
                break;
            case FixedPipeline:
                r.setLighting(geom.getWorldLightList());
                break;
            case MultiPass:
                // NOTE: Special case!
                renderMultipassLighting(shader, geom, r);
                // very important, notice the return statement!
                return;
        }

        // upload and bind shader
        r.setShader(shader);
        r.renderMesh(geom.getMesh(), 1);
    }

}
