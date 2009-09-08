package com.g3d.app;

import com.g3d.font.BitmapFont;
import com.g3d.font.BitmapText;
import com.g3d.input.FlyByCamera;
import com.g3d.input.KeyInput;
import com.g3d.input.binding.BindingListener;
import com.g3d.material.RenderState;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial.CullHint;
import com.g3d.system.AppSettings;
import java.net.URL;

/**
 * <code>SimpleApplication</code> extends the <code>Application</code> class
 * to provide default functionality like a first-person camera,
 * and an accessible root node that is updated and rendered regularly.
 */
public abstract class SimpleApplication extends Application {

    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");

    protected float secondCounter = 0.0f;
    protected BitmapText fpsText;

    protected FlyByCamera flyCam;

    public SimpleApplication(){
        super();
        
        // set some default settings in-case
        // settings dialog is not shown
        setSettings(new AppSettings(true));
    }

    

    @Override
    public void start(){
        // show settings dialog
        URL iconUrl = SimpleApplication.class.getResource("Monkey.png");
        SettingsDialog dialog = new SettingsDialog(settings, iconUrl);
        dialog.showDialog();
        if (dialog.waitForSelection() == SettingsDialog.CANCEL_SELECTION){
            // user pressed cancel/exit
            return;
        }
        
        super.start();
    }

    public void loadFPSText(){
        BitmapFont font = manager.loadFont("cooper.fnt");

        fpsText = new BitmapText(font, false);
        fpsText.setSize(font.getCharSet().getRenderedSize());
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        guiNode.attachChild(fpsText);
    }

    @Override
    public void initialize(){
        super.initialize();

        // enable depth test and back-face culling for performance
        renderer.applyRenderState(RenderState.DEFAULT);

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        loadFPSText();

        if (inputManager != null){
            flyCam = new FlyByCamera(cam);
            flyCam.setMoveSpeed(1f);
            flyCam.registerWithDispatcher(inputManager);
        
            inputManager.registerKeyBinding("SIMPLEAPP_Exit", KeyInput.KEY_ESCAPE);
            inputManager.registerKeyBinding("SIMPLEAPP_CameraPos", KeyInput.KEY_C);
            inputManager.addTriggerListener(new BindingListener() {
                public void onBinding(String binding, float value) {
                    if (binding.equals("SIMPLEAPP_Exit")){
                        stop();
                    }else if (binding.equals("SIMPLEAPP_CameraPos")){
                        Camera cam = renderer.getCamera();
                        if (cam != null){
                            Vector3f loc = cam.getLocation();
                            Quaternion rot = cam.getRotation();
                            System.out.println("Camera Position: ("+
                                    loc.x+", "+loc.y+", "+loc.z+")");
                            System.out.println("Camera Position: "+rot);
                        }
                    }
                }
            });
        }

        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        if (speed == 0)
            return;
        
        super.update();
        float tpf = timer.getTimePerFrame();

        secondCounter += tpf;
        int fps = (int) timer.getFrameRate();
        if (secondCounter >= 1.0f){
            fpsText.setText("FPS: "+fps);
            fpsText.assemble();
            secondCounter = 0.0f;
        }
        
        simpleUpdate(tpf);
        rootNode.updateGeometricState(tpf, true);
        guiNode.updateGeometricState(tpf, true);

        renderer.clearBuffers(true, true, true);
        render(rootNode, renderer);
        render(guiNode, renderer);
        simpleRender(renderer);
        renderer.renderQueue();
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf){
    }

    public void simpleRender(Renderer r){
    }

}
