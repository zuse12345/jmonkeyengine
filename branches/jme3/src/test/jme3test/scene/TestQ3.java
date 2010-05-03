package jme3test.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.collision.MotionAllowedListener;
import com.jme3.input.FirstPersonCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.MeshLoader;
import com.jme3.scene.plugins.ogre.OgreMaterialList;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import com.jme3.scene.shape.Sphere;
import jme3test.collision.SphereMotionAllowedListener;

public class TestQ3 extends SimpleApplication {

    private Sphere sphereMesh = new Sphere(32, 32, 10f, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);
    private Spatial gameLevel;

    public static void main(String[] args){
        TestQ3 app = new TestQ3();
        app.start();
    }

    public void simpleInitApp() {
        MeshLoader.AUTO_INTERLEAVE = false;
//        this.flyCam.setMoveSpeed(500);
        inputManager.removeBindingListener(flyCam);
        FirstPersonCamera fps = new FirstPersonCamera(cam, new Vector3f(0, -10, 0));
        fps.registerWithDispatcher(inputManager);
        fps.setMoveSpeed(100);

        this.cam.setFrustumFar(2000);

        // load sky
//        sphere.updateModelBound();
//        sphere.setQueueBucket(Bucket.Sky);
//        Material sky = new Material(manager, "sky.j3md");
//        Texture tex = manager.loadTexture("sky3.dds", false, true, true, 0);
//        sky.setTexture("m_Texture", tex);
//        sphere.setMaterial(sky);
//        rootNode.attachChild(sphere);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(-1, -1, -1).normalize());
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(1, -1, 1).normalize());
        rootNode.addLight(dl);
        // create the geometry and attach it
        assetManager.registerLocator("quake3level.zip", ZipLocator.class.getName());

        // create the geometry and attach it
        OgreMaterialList matList = (OgreMaterialList) assetManager.loadAsset("Scene.material");
        OgreMeshKey key = new OgreMeshKey("main.meshxml", matList);
        gameLevel = (Spatial) assetManager.loadAsset(key);
        gameLevel.updateGeometricState();
//        gameLevel.setLocalScale(0.2f);
        rootNode.attachChild(gameLevel);
        rootNode.updateGeometricState();

//        cam.setLocation(new Vector3f(-205, 47, -1175));
        //cam.setLocation(new Vector3f(0, 750, 0));
        //cam.setRotation(new Quaternion(0.0073885284f, -0.67373353f, 0.006737455f, 0.7389067f));
        cam.setLocation(new Vector3f(340, 264, 453));
        cam.setRotation(new Quaternion(-2.97E-4f, 0.999f, -0.0349f, -0.0085f));
        MotionAllowedListener motAllow = new SphereMotionAllowedListener(rootNode, new Vector3f(100, 200, 100));
        fps.setMotionAllowedListener(motAllow);
        //flyCam.setMotionAllowedListener();
    }

    @Override
    public void simpleUpdate(float tpf){
        sphere.setLocalTranslation(cam.getLocation());
    }
}
