package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.collision.MotionAllowedListener;
import com.g3d.input.FirstPersonCamera;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.plugins.ogre.MeshLoader;
import com.g3d.scene.plugins.ogre.OgreMaterialList;
import com.g3d.scene.plugins.ogre.OgreMeshKey;
import com.g3d.scene.shape.Sphere;
import g3dtest.collision.SphereMotionAllowedListener;

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
        inputManager.removeTriggerListener(flyCam);
        FirstPersonCamera fps = new FirstPersonCamera(cam, new Vector3f(0, -100, 0));
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

        // create the geometry and attach it
        manager.registerLocator("Q3.bin", "com.g3d.asset.pack.J3PFileLocator", "tga", "meshxml", "material");

        // create the geometry and attach it
        OgreMaterialList matList = (OgreMaterialList) manager.loadContent("Scene.material");
        OgreMeshKey key = new OgreMeshKey("main.meshxml", matList);
        gameLevel = (Spatial) manager.loadContent(key);
        gameLevel.updateGeometricState();
//        gameLevel.setLocalScale(0.2f);
        rootNode.attachChild(gameLevel);
        rootNode.updateGeometricState();

//        cam.setLocation(new Vector3f(-205, 47, -1175));
        //cam.setLocation(new Vector3f(0, 750, 0));
        //cam.setRotation(new Quaternion(0.0073885284f, -0.67373353f, 0.006737455f, 0.7389067f));
        cam.setLocation(new Vector3f(340, 264, 453));
        cam.setRotation(new Quaternion(-2.97E-4f, 0.999f, -0.0349f, -0.0085f));
        MotionAllowedListener motAllow = new SphereMotionAllowedListener(rootNode, new Vector3f(50, 100, 50));
        fps.setMotionAllowedListener(motAllow);
        //flyCam.setMotionAllowedListener();
    }

    @Override
    public void simpleUpdate(float tpf){
        sphere.setLocalTranslation(cam.getLocation());
    }
}
