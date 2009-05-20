package com.g3d.app;

import com.g3d.input.FlyByCamera;
import com.g3d.input.SoftwareCursor;
import com.g3d.input.binding.BindingListener;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.lwjgl.LwjglRenderer;
import com.g3d.renderer.queue.RenderQueue;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.SceneManager;
import com.g3d.scene.Spatial;
import com.g3d.system.DisplaySettings;
import com.g3d.system.DisplaySettings.Template;
import com.g3d.system.Timer;
import java.util.List;
import org.lwjgl.input.Keyboard;

public abstract class SimpleApplication extends Application implements SceneManager {

    protected Node rootNode = new Node("Root Node");
    protected float secondCounter = 0.0f;

    protected FlyByCamera flyCam;
    protected SoftwareCursor cursor;

    public SimpleApplication(){
        setSceneManager(this);
        setSettings(new DisplaySettings(Template.Default640x480));
    }

    private void render(Spatial s, Renderer r){
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

    public void init(Renderer r) {
        // enable depth test and back-face culling for performance
        r.setDepthTest(true);
        r.setBackfaceCulling(true);

        flyCam = new FlyByCamera(cam);
        flyCam.setMoveSpeed(1f);
        flyCam.registerWithDispatcher(dispatcher);

//        cursor = new SoftwareCursor();
//        try {
//            Texture t = TextureLoader.loadTexture(Application.class.getResource("/com/g3d/app/cursor.png"));
//            cursor.setImage(t.getImage(), settings.getWidth(), settings.getHeight());
//        } catch (IOException ex) {
//        }
//        cursor.registerWithDispatcher(dispatcher);

        dispatcher.registerKeyBinding("SIMPLEAPP_Exit", Keyboard.KEY_ESCAPE);
        dispatcher.addTriggerListener(new BindingListener() {
            public void onBinding(String binding, float value) {
                if (binding.equals("SIMPLEAPP_Exit")){
                    stop();
                }
            }
        });

        // call user code
        simpleInitApp();
        // TODO: Add fps display
    }

    public void update(float tpf) {
        secondCounter += tpf;
        float fps = Timer.getTimer().getFrameRate();
        if (secondCounter >= 1.0f){
//            System.out.println(fps);
            secondCounter = 0.0f;
        }
        
        simpleUpdate(tpf);
        rootNode.updateGeometricState(tpf, true);
    }

    public void render(Renderer r) {
        LwjglRenderer lr = (LwjglRenderer) r;
//        lr.setupDepthPrePass();
//        {
//            render(rootNode, r);
//            simpleRender(r);
//        }
//        lr.setupColorPass();
//        {
            render(rootNode, r);
            simpleRender(r);

            if (cursor != null){
                cursor.updateGeometricState(0f, true);
                renderer.renderGeometry(cursor);
            }
//        }
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf){
    }

    public void simpleRender(Renderer r){
    }

}
