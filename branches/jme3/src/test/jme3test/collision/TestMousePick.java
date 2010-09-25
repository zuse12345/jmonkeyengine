package jme3test.collision;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

public class TestMousePick extends SimpleApplication {

    public static void main(String[] args) {
        TestMousePick app = new TestMousePick();
        app.start();
    }
    
    Node shootables;
    Geometry mark;

    @Override
    public void simpleInitApp() {
        initKeys();       // load custom key mappings
        initMark();       // a red sphere to mark the hit

        /** create four colored boxes and a floor to shoot at: */
        shootables = new Node("Shootables");
        rootNode.attachChild(shootables);
        shootables.attachChild(makeCube("a Dragon", -2f, 0f, 1f));
        shootables.attachChild(makeCube("a tin can", 1f, -2f, 0f));
        shootables.attachChild(makeCube("the Sheriff", 0f, 1f, -2f));
        shootables.attachChild(makeCube("the Deputy", 1f, 0f, -4f));
        shootables.attachChild(makeFloor());
        shootables.attachChild(makeCharacter());
    }

    /** Declaring the "Shoot" action and mapping to its triggers. */
    private void initKeys() {
        flyCam.setEnabled(false);
        inputManager.addMapping("Shoot", new MouseButtonTrigger(0));
        inputManager.addListener(actionListener, "Shoot");
    }
    /** Defining the "Shoot" action: Determine what was hit and how to respond. */
    private ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Shoot") && !keyPressed) {
                Vector3f origin    = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
                Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
                direction.subtractLocal(origin).normalizeLocal();

                Ray ray = new Ray(origin, direction);
                CollisionResults results = new CollisionResults();
                shootables.collideWith(ray, results);
                System.out.println("----- Collisions? " + results.size() + "-----");
                for (int i = 0; i < results.size(); i++) {
                    // For each hit, we know distance, impact point, name of geometry.
                    float dist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getWorldContactPoint();
                    String hit = results.getCollision(i).getGeometry().getName();
                    System.out.println("* Collision #" + i);
                    System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
                }
                if (results.size() > 0) {
                    CollisionResult closest = results.getClosestCollision();
                    mark.setLocalTranslation(closest.getWorldContactPoint());
                    rootNode.attachChild(mark);
                } else {
                    rootNode.detachChild(mark);
                }
            }
        }
    };

    /** A cube object for target practice */
    protected Geometry makeCube(String name, float x, float y, float z) {
        Box box = new Box(new Vector3f(x, y, z), 1, 1, 1);
        Geometry cube = new Geometry(name, box);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat1.setColor("m_Color", ColorRGBA.randomColor());
        cube.setMaterial(mat1);
        return cube;
    }

    /** A floor to show that the "shot" can go through several objects. */
    protected Geometry makeFloor() {
        Box box = new Box(new Vector3f(0, -4, -5), 15, .2f, 15);
        Geometry floor = new Geometry("the Floor", box);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat1.setColor("m_Color", ColorRGBA.Gray);
        floor.setMaterial(mat1);
        return floor;
    }

    /** A red ball that marks the last spot that was "hit" by the "shot". */
    protected void initMark() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mark_mat.setColor("m_Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }

    protected Spatial makeCharacter() {
        // load a character from jme3test-test-data
        Spatial golem = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        golem.scale(0.5f);
        golem.setLocalTranslation(-1.0f, -1.5f, -0.6f);
        golem.updateGeometricState();
        golem.setModelBound(new BoundingBox());
        golem.updateModelBound();
        // We must add a light to make the model visible
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        golem.addLight(sun);
        return golem;
    }
}
