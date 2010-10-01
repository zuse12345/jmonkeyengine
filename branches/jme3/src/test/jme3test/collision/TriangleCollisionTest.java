package jme3test.collision;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Geometry;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

public class TriangleCollisionTest extends SimpleApplication {

    Geometry geom1;

    Spatial golem;

    public static void main(String[] args) {
        TriangleCollisionTest app = new TriangleCollisionTest();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Create two boxes
        Mesh mesh1 = new Box(0.5f, 0.5f, 0.5f);
        geom1 = new Geometry("Box", mesh1);
        geom1.move(2, 2, -.5f);
        Material m1 = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        m1.setColor("m_Color", ColorRGBA.Blue);
        geom1.setMaterial(m1);
        rootNode.attachChild(geom1);

        // load a character from jme3test-test-data
        golem = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        golem.scale(0.5f);
        golem.setLocalTranslation(-1.0f, -1.5f, -0.6f);

        // We must add a light to make the model visible
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
        golem.addLight(sun);
        rootNode.attachChild(golem);

        // Create input
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("MoveUp", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("MoveDown", new KeyTrigger(KeyInput.KEY_K));

        inputManager.addListener(analogListener, new String[]{
                    "MoveRight", "MoveLeft", "MoveUp", "MoveDown"
                });
    }
    private AnalogListener analogListener = new AnalogListener() {

        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("MoveRight")) {
                geom1.move(2 * tpf, 0, 0);
            }

            if (name.equals("MoveLeft")) {
                geom1.move(-2 * tpf, 0, 0);
            }

            if (name.equals("MoveUp")) {
                geom1.move(0, 2 * tpf, 0);
            }

            if (name.equals("MoveDown")) {
                geom1.move(0, -2 * tpf, 0);
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        rootNode.updateGeometricState();

        CollisionResults results = new CollisionResults();
        BoundingVolume bv = geom1.getWorldBound();
        golem.collideWith(bv, results);

        if (results.size() > 0) {
            geom1.getMaterial().setColor("m_Color", ColorRGBA.Red);
        }else{
            geom1.getMaterial().setColor("m_Color", ColorRGBA.Blue);
        }
    }
}
