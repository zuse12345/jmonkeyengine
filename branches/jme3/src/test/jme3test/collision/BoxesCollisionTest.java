package jme3test.collision;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Geometry;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

public class BoxesCollisionTest extends SimpleApplication {

    Geometry geom1, geom2;
    Box box1, box2;

    public static void main(String[] args) {
        BoxesCollisionTest app = new BoxesCollisionTest();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Create two boxes
        box1 = new Box(new Vector3f(-2, 0, 0), 1, 1, 1);
        box2 = new Box(new Vector3f(2, 0, 0), 1, 1, 1);

        geom1 = new Geometry("Box", box1);
        geom2 = new Geometry("Box", box2);

        Material m1 = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        Material m2 = m1.clone();

        m1.setColor("m_Color", ColorRGBA.Blue);
        m2.setColor("m_Color", ColorRGBA.Green);

        geom1.setMaterial(m1);
        geom2.setMaterial(m2);

        rootNode.attachChild(geom1);
        rootNode.attachChild(geom2);

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
                geom1.move(10 * tpf, 0, 0);
            }

            if (name.equals("MoveLeft")) {
                geom1.move(-10 * tpf, 0, 0);
            }

            if (name.equals("MoveUp")) {
                geom1.move(0, 10 * tpf, 0);
            }

            if (name.equals("MoveDown")) {
                geom1.move(0, -10 * tpf, 0);
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        rootNode.updateGeometricState();
        if (geom1.getWorldBound().intersects(geom2.getWorldBound())) {
            System.out.println(tpf);
        }
    }
}
