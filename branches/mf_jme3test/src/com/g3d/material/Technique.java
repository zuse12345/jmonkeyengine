package com.g3d.material;

import com.g3d.shader.UniformBinding;
import com.g3d.shader.Attribute;
import com.g3d.shader.Shader;
import com.g3d.shader.Uniform;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Technique {

    private String name;
    private Shader shader;
    private boolean useLighting;

    private final EnumMap<UniformBinding, Uniform> worldParams
            = new EnumMap<UniformBinding, Uniform>(UniformBinding.class);

//    private final Map<String, Attribute> attribs = new HashMap<String, Attribute>();

    public Technique(String name){
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

    public Shader getShader() {
        return shader;
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }

    public boolean addWorldParam(String name){
        Uniform uniform = shader.getUniform(name);
        for (UniformBinding binding : UniformBinding.values()){
            if (binding.getVarName().equals(name)){
                 worldParams.put(binding, uniform);
                 return true;
            }
        }
        return false;
    }

//    public void addAttribute(String name) {
//        Attribute attrib = shader.getAttribute(name);
//        attribs.put(name, attrib);
//    }

    public EnumMap<UniformBinding, Uniform> getWorldParams() {
        return worldParams;
    }

}
