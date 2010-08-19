/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jme3test.post;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.post.ssao.SSAOConfig;

/**
 *
 * @author nehon
 */
public class SSAOUI {
    private SSAOConfig ssaoConfig;
    public SSAOUI(InputManager inputManager, SSAOConfig config) {
        this.ssaoConfig=config;
        
//    protected float sampleRadius = 5.1f;
//    protected float intensity = 1.5f;
//    protected float scale = 0.2f;
//    protected float bias = 0.1f;
//    protected boolean useOnlyAo = false;
//    protected boolean useAo = true;
    
        inputManager.addMapping("sampleRadiusUp", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("sampleRadiusDown", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("intensityUp", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("intensityDown", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("scaleUp", new KeyTrigger(KeyInput.KEY_O));
        inputManager.addMapping("scaleDown", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("biasUp", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("biasDown", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("toggleUseAO", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("toggleUseOnlyAo", new KeyTrigger(KeyInput.KEY_NUMPAD0));
        
        ActionListener acl = new ActionListener() {

            public void onAction(String name, boolean keyPressed, float tpf) {
               

               
                
                if (name.equals("toggleUseAO") && keyPressed) {
                    ssaoConfig.setUseAo(!ssaoConfig.isUseAo());
                    System.out.println("use AO : "+ssaoConfig.isUseAo());
                }
                if (name.equals("toggleUseOnlyAo") && keyPressed) {
                    ssaoConfig.setUseOnlyAo(!ssaoConfig.isUseOnlyAo());
                    System.out.println("use Only AO : "+ssaoConfig.isUseOnlyAo());

                }
               
               
            }
        };

         AnalogListener anl = new AnalogListener() {

            public void onAnalog(String name, float value, float tpf) {
                if (name.equals("sampleRadiusUp")) {
                    ssaoConfig.setSampleRadius(ssaoConfig.getSampleRadius()+0.01f);
                    System.out.println("Sample Radius : "+ssaoConfig.getSampleRadius());
                }
                if (name.equals("sampleRadiusDown")) {
                    ssaoConfig.setSampleRadius(ssaoConfig.getSampleRadius()-0.01f);
                    System.out.println("Sample Radius : "+ssaoConfig.getSampleRadius());
                }
                if (name.equals("intensityUp")) {
                    ssaoConfig.setIntensity(ssaoConfig.getIntensity()+0.01f);
                    System.out.println("Intensity : "+ssaoConfig.getIntensity());
                }
                if (name.equals("intensityDown")) {
                    ssaoConfig.setIntensity(ssaoConfig.getIntensity()-0.01f);
                    System.out.println("Intensity : "+ssaoConfig.getIntensity());
                }
                if (name.equals("scaleUp")) {
                    ssaoConfig.setScale(ssaoConfig.getScale()+0.01f);
                    System.out.println("scale : "+ssaoConfig.getScale());
                }
                if (name.equals("scaleDown")) {
                    ssaoConfig.setScale(ssaoConfig.getScale()-0.01f);
                    System.out.println("scale : "+ssaoConfig.getScale());
                }
                if (name.equals("biasUp")) {
                    ssaoConfig.setBias(ssaoConfig.getBias()+0.001f);
                    System.out.println("bias : "+ssaoConfig.getBias());
                }
                if (name.equals("biasDown")) {
                    ssaoConfig.setBias(ssaoConfig.getBias()-0.001f);
                    System.out.println("bias : "+ssaoConfig.getBias());
                }
            }
        };
        inputManager.addListener(acl,"toggleUseAO","toggleUseOnlyAo");
        inputManager.addListener(anl, "sampleRadiusUp","sampleRadiusDown","intensityUp","intensityDown", "scaleUp","scaleDown",
                                "biasUp","biasDown");
     
    }
    
    

}
