package com.g3d.app;

import com.g3d.input.FlyByCamera;
import com.g3d.input.binding.BindingListener;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.queue.RenderQueue;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial;
import com.g3d.system.AppSettings;
import com.g3d.system.AppSettings.Template;
import java.util.List;
import org.lwjgl.input.Keyboard;

/**
 * <code>SimpleApplication</code> extends the <code>Application</code> class
 * to provide default functionality like a first-person camera,
 * and an accessible root node that is updated and rendered regularly.
 */
public abstract class SimpleApplication extends Application {

    protected Node rootNode = new Node("Root Node");
    protected float secondCounter = 0.0f;

    protected FlyByCamera flyCam;

    public SimpleApplication(){
        setSettings(new AppSettings(Template.Default640x480));
    }

    protected void render(Spatial s, Renderer r){
        if (!s.checkCulling(cam)){
                return;
        }
        if (s instanceof Node){
            Node n = (Node) s;
            List<Spatial> children = n.getChildren();
            for (int i = 0; i < children.size(); i++){
                render(children.get(i), r);
            }
        }else if (s instanceof Geometry){
            Geometry gm = (Geometry) s;
            r.addToQueue(gm, RenderQueue.Bucket.Opaque);
        }
    }

    @Override
    public void initialize(){
        super.initialize();

        // enable depth test and back-face culling for performance
        renderer.setDepthTest(true);
        renderer.setBackfaceCulling(true);

        if (dispatcher != null){
            flyCam = new FlyByCamera(cam);
            flyCam.setMoveSpeed(1f);
            flyCam.registerWithDispatcher(dispatcher);
        
            dispatcher.registerKeyBinding("SIMPLEAPP_Exit", Keyboard.KEY_ESCAPE);
            dispatcher.addTriggerListener(new BindingListener() {
                public void onBinding(String binding, float value) {
                    if (binding.equals("SIMPLEAPP_Exit")){
                        stop();
                    }
                }
            });
        }

        // call user code
        simpleInitApp();
        // TODO: Add fps display
    }

    @Override
    public void update() {
        super.update();
        float tpf = timer.getTimePerFrame();

        secondCounter += tpf;
        float fps = timer.getFrameRate();
        if (secondCounter >= 1.0f){
//            System.out.println(fps);
            secondCounter = 0.0f;
        }
        
        simpleUpdate(tpf);
        rootNode.updateGeometricState(tpf, true);

        renderer.clearBuffers(true, true, true);
        render(rootNode, renderer);
        simpleRender(renderer);
        renderer.renderQueue();
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf){
    }

    public void simpleRender(Renderer r){
    }

}
