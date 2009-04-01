package com.g3d.app;

import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.SceneManager;
import com.g3d.scene.Spatial;
import com.g3d.system.DisplaySettings;
import com.g3d.system.DisplaySettings.Template;

public abstract class SimpleApplication extends Application implements SceneManager {

    protected Node rootNode = new Node("Root Node");

    public SimpleApplication(){
        setSceneManager(this);
        setSettings(new DisplaySettings(Template.Default640x480));
    }

    public void start(){
        init();
        run();
    }

    private void render(Spatial s, Renderer r){
        if (s instanceof Node){
            Node n = (Node) s;
            for (Spatial child : n.getChildren()){
                render(child, r);
            }
        }else if (s instanceof Geometry){
            Geometry gm = (Geometry) s;
            r.renderGeometry(gm);
        }
    }

    public void init(Renderer r) {
        // enable depth test and back-face culling for performance
        r.setDepthTest(true);
        r.setBackfaceCulling(true);

        // call user code
        simpleInitApp();
        // TODO: Add fps display
    }

    public void update(float tpf) {
        simpleUpdate(tpf);
        rootNode.updateGeometricState(tpf, true); 
    }

    public void render(Renderer r) {
        render(rootNode, r);
        simpleRender(r);
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf){
    }

    public void simpleRender(Renderer r){
    }

}
