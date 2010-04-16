package jme3test.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class TestSceneLoading extends SimpleApplication {

    private Sphere sphereMesh = new Sphere(32, 32, 10, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);

    public static void main(String[] args){
        TestSceneLoading app = new TestSceneLoading();
        app.start();
    }

    @Override
    public void simpleUpdate(float tpf){
        sphere.setLocalTranslation(cam.getLocation());
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(10);

        // load sky
//        sphere.updateModelBound();
//        sphere.setQueueBucket(Bucket.Sky);
//        Material sky = new Material(manager, "sky.j3md");
//        TextureKey key = new TextureKey("sky3.dds", false);
//        key.setGenerateMips(true);
//        key.setAsCube(true);
//        Texture tex = manager.loadTexture(key);
//        sky.setTexture("m_Texture", tex);
//        sphere.setMaterial(sky);
//        rootNode.attachChild(sphere);

        // create the geometry and attach it
        manager.registerLocator("wildhouse.zip",
                                ZipLocator.class.getName(),
                                "scene", "meshxml",
                                "material", "jpg", "png");

        Spatial scene = manager.loadModel("main.scene");
        rootNode.attachChild(scene);
    }
}
