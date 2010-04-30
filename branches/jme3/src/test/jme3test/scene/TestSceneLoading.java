package jme3test.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class TestSceneLoading extends SimpleApplication {

//    private Sphere sphereMesh = new Sphere(32, 32, 10, false, true);
//    private Geometry sphere = new Geometry("Sky", sphereMesh);

    public static void main(String[] args){
        TestSceneLoading app = new TestSceneLoading();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleUpdate(float tpf){
//        sphere.setLocalTranslation(cam.getLocation());
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
        manager.registerLocator("wildhouse.zip", ZipLocator.class.getName());
        Spatial scene = manager.loadModel("main.scene");

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(Vector3f.UNIT_Y.negate());
        sun.setColor(ColorRGBA.White);
        scene.addLight(sun);

        rootNode.attachChild(scene);
    }
}
