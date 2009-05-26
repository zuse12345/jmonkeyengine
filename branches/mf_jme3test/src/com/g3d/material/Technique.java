package com.g3d.material;

import com.g3d.shader.UniformBinding;
import com.g3d.shader.Shader;
import com.g3d.shader.Uniform;
import java.util.ArrayList;
import java.util.List;

public class Technique {

    private String name;
    private Shader shader;
    private boolean useLighting;

    private final List<Uniform> worldParams = new ArrayList<Uniform>();

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

    public boolean addWorldParam(String name) {
        Uniform uniform = shader.getUniform("g_"+name);
        for (UniformBinding binding : UniformBinding.values()) {
            if (binding.name().equals(name)) {
                uniform.setBinding(binding);
                worldParams.add(uniform);
                return true;
            }
        }
        return false;
    }

//    public void addAttribute(String name) {
//        Attribute attrib = shader.getAttribute(name);
//        attribs.put(name, attrib);
//    }

    public List<Uniform> getWorldParams() {
        return worldParams;
    }

}
