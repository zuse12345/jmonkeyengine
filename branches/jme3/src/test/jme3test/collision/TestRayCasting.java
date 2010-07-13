package jme3test.collision;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.collision.CollisionResults;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class TestRayCasting extends SimpleApplication implements RawInputListener {

    private Ray ray = new Ray();
    private CollisionResults results = new CollisionResults();

    public static void main(String[] args){
        TestRayCasting app = new TestRayCasting();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        // load material
        Material mat = (Material) assetManager.loadAsset(new AssetKey("Interface/Logo/Logo.j3m"));

        // attach objects
//        Geometry sphere = new Geometry("Sphere", new Sphere(10, 10, 1));
//        sphere.rotate(FastMath.HALF_PI / 2f, FastMath.HALF_PI / 2f, 0);
//        sphere.setMaterial(mat);
//        rootNode.attachChild(sphere);

//        Geometry box = new Geometry("Box", new Box(Vector3f.ZERO, .75f, .75f, .75f));
//        box.setLocalTranslation(3f, 0, 0);
//        box.rotate(FastMath.HALF_PI / 2f, FastMath.HALF_PI / 2f, 0);
//        box.setMaterial(mat);
//        rootNode.attachChild(box);

        Spatial teapot = assetManager.loadModel("Models/Teapot/Teapot.meshxml");
//        teapot.move(-3f, 0, 0);
//        teapot.setLocalScale(0.5f);
        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);
        rootNode.updateGeometricState();

        cam.setLocation(cam.getLocation().add(0,1,0));
//        cam.lookAt(teapot.getWorldBound().getCenter(), Vector3f.UNIT_Y);

        inputManager.addRawInputListener(this);
        
        new RayTrace(rootNode, cam, 640, 480).trace();
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        int x = evt.getX();
        int y = evt.getY();

        Vector3f pos = cam.getWorldCoordinates(new Vector2f(x,y), 0.0f);
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(x,y), 0.3f);
        dir.subtractLocal(pos).normalizeLocal();
        
        ray.setOrigin(pos);
        ray.setDirection(dir);
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (evt.isPressed()){
            results.clear();
            rootNode.collideWith(ray, results);
            System.out.println(results.size());
        }
    }

    public void onKeyEvent(KeyInputEvent evt) {
    }

}