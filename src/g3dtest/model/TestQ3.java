package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.pack.J3PFileLocator;
import com.g3d.material.Material;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.shape.Sphere;
import com.g3d.texture.Texture;

public class TestQ3 extends SimpleApplication {

    private Sphere sphereMesh = new Sphere(32, 32, 10, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);

    public static void main(String[] args){
        TestQ3 app = new TestQ3();
        app.start();
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(100);

        // load sky
        sphere.updateModelBound();
        sphere.setQueueBucket(Bucket.Sky);
        Material sky = new Material(manager, "sky.j3md");
        Texture tex = manager.loadTexture("sky3.dds", false, true, true, 0);
        sky.setTexture("m_Texture", tex);
        sphere.setMaterial(sky);
        rootNode.attachChild(sphere);

        // create the geometry and attach it
        manager.registerLocator("Q3.j3p", J3PFileLocator.class, "tga", "meshxml", "material");

        // create the geometry and attach it
        Spatial cube = manager.loadOgreModel("main.meshxml","Scene.material");
        cube.setLocalScale(0.2f);
        rootNode.attachChild(cube);
    }
    
    @Override
    public void simpleUpdate(float tpf){
        sphere.setLocalTranslation(cam.getLocation());
    }
}
