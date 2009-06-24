package com.g3d.material;

import com.g3d.shader.DefineList;
import com.g3d.shader.UniformBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniqueDef {

    private String name;

    private String vertName;
    private String fragName;
    private String shaderLang;
    private DefineList presetDefines = new DefineList();

    private boolean useLighting;
    private RenderState renderState;

    private final Map<String, String> defineParams = new HashMap<String, String>();
    private final List<UniformBinding> worldBinds = new ArrayList<UniformBinding>();
//    private final Map<String, Attribute> attribs = new HashMap<String, Attribute>();

    public TechniqueDef(String name){
        this.name = name == null ? "Default" : name;
    }

    public String getName(){
        return name;
    }

    public void setUsesLighting(boolean lighting){
        useLighting = lighting;
    }

    public boolean isUsingLighting(){
        return useLighting;
    }

    public RenderState getRenderState() {
        return renderState;
    }

    public void setRenderState(RenderState renderState) {
        this.renderState = renderState;
    }

    public void setShaderFile(String vert, String frag, String lang){
        this.vertName = vert;
        this.fragName = frag;
        this.shaderLang = lang;
    }

    public DefineList getShaderPresetDefines() {
        return presetDefines;
    }

    public String getShaderParamDefine(String paramName){
        return defineParams.get(paramName);
    }

    public void addShaderParamDefine(String paramName, String defineName){
        defineParams.put(paramName, defineName);
    }

    public void addShaderPresetDefine(String defineName, String value){
        presetDefines.set(defineName, value);
    }

    public String getFragName() {
        return fragName;
    }

    public String getVertName() {
        return vertName;
    }

    public String getShaderLanguage() {
        return shaderLang;
    }

    public boolean addWorldParam(String name) {
        for (UniformBinding binding : UniformBinding.values()) {
            if (binding.name().equals(name)) {
                worldBinds.add(binding);
                return true;
            }
        }
        return false;
    }

//    public void addAttribute(String name) {
//        Attribute attrib = shader.getAttribute(name);
//        attribs.put(name, attrib);
//    }

    public List<UniformBinding> getWorldBindings() {
        return worldBinds;
    }

}
