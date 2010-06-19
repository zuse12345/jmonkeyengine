package jme3test.input;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.system.AppSettings;
import com.jme3.input.controls.Controls;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.lwjgl.LwjglKeyInput;
import com.jme3.input.lwjgl.LwjglMouseInput;

public class TestControls extends SimpleApplication {

    private Controls controls;
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){
            System.out.println(name + " = " + pressed);
        }
    };
    public AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            System.out.println(name + " = " + value);
        }
    };

    public static void main(String[] args){
        TestControls app = new TestControls();
        AppSettings settings = new AppSettings(true);
        settings.setUseInput(false);
//        settings.setFrameRate(15);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        controls = new Controls(new LwjglKeyInput(), new LwjglMouseInput(), null);

        // Test multiple inputs per mapping
        controls.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE),
                                     new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));

        // Test multiple listeners per mapping
        controls.addListener(actionListener, "Space");
        controls.addListener(analogListener, "Space");
    }

    @Override
    public void simpleUpdate(float tpf){
        // You won't have to do this when the system becomes core.
        controls.update(tpf);
    }

}
