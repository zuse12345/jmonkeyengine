/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jme3test.water;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.water.SimpleWaterProcessor;

/**
 *
 * @author nehon
 */
public class WaterUI {
    private SimpleWaterProcessor processor;
    public WaterUI(InputManager inputManager, SimpleWaterProcessor proc) {
        processor=proc;


        System.out.println("----------------- SSAO UI Debugger --------------------");
        System.out.println("-- Water transparency : press Y to increase, H to decrease");
        System.out.println("-- Water depth : press U to increase, J to decrease");
//        System.out.println("-- AO scale : press I to increase, K to decrease");
//        System.out.println("-- AO bias : press O to increase, P to decrease");
//        System.out.println("-- Toggle AO on/off : press space bar");
//        System.out.println("-- Use only AO : press Num pad 0");
//        System.out.println("-- Output config declaration : press P");
        System.out.println("-------------------------------------------------------");
    
        inputManager.addMapping("transparencyUp", new KeyTrigger(KeyInput.KEY_Y));
        inputManager.addMapping("transparencyDown", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("depthUp", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("depthDown", new KeyTrigger(KeyInput.KEY_J));
//        inputManager.addMapping("scaleUp", new KeyTrigger(KeyInput.KEY_I));
//        inputManager.addMapping("scaleDown", new KeyTrigger(KeyInput.KEY_K));
//        inputManager.addMapping("biasUp", new KeyTrigger(KeyInput.KEY_O));
//        inputManager.addMapping("biasDown", new KeyTrigger(KeyInput.KEY_L));
//        inputManager.addMapping("outputConfig", new KeyTrigger(KeyInput.KEY_P));
//        inputManager.addMapping("toggleUseAO", new KeyTrigger(KeyInput.KEY_SPACE));
//        inputManager.addMapping("toggleUseOnlyAo", new KeyTrigger(KeyInput.KEY_NUMPAD0));
        
//        ActionListener acl = new ActionListener() {
//
//            public void onAction(String name, boolean keyPressed, float tpf) {
//
//                if (name.equals("toggleUseAO") && keyPressed) {
//                    ssaoConfig.setUseAo(!ssaoConfig.isUseAo());
//                    System.out.println("use AO : "+ssaoConfig.isUseAo());
//                }
//                if (name.equals("toggleUseOnlyAo") && keyPressed) {
//                    ssaoConfig.setUseOnlyAo(!ssaoConfig.isUseOnlyAo());
//                    System.out.println("use Only AO : "+ssaoConfig.isUseOnlyAo());
//
//                }
//                if (name.equals("outputConfig") && keyPressed) {
//                    System.out.println("new SSAOConfig("+ssaoConfig.getSampleRadius()+"f,"+ssaoConfig.getIntensity()+"f,"+ssaoConfig.getScale()+"f,"+ssaoConfig.getBias()+"f,"+ssaoConfig.isUseOnlyAo()+","+ssaoConfig.isUseAo()+");");
//                }
//
//            }
//        };

         AnalogListener anl = new AnalogListener() {

            public void onAnalog(String name, float value, float tpf) {
                if (name.equals("transparencyUp")) {
                    processor.setWaterTransparency(processor.getWaterTransparency()+0.001f);
                    System.out.println("Water transparency : "+processor.getWaterTransparency());
                }
                if (name.equals("transparencyDown")) {
                    processor.setWaterTransparency(processor.getWaterTransparency()-0.001f);
                    System.out.println("Water transparency : "+processor.getWaterTransparency());
                }
                if (name.equals("depthUp")) {
                    processor.setWaterDepth(processor.getWaterDepth()+0.001f);
                    System.out.println("Water depth : "+processor.getWaterDepth());
                }
                if (name.equals("depthDown")) {
                    processor.setWaterDepth(processor.getWaterDepth()-0.001f);
                    System.out.println("Water depth : "+processor.getWaterDepth());
                }

            }
        };
    //    inputManager.addListener(acl,"toggleUseAO","toggleUseOnlyAo","outputConfig");
        inputManager.addListener(anl, "transparencyUp","transparencyDown","depthUp","depthDown");
     
    }
    
    

}
