package jme3test.bullet;

import com.jme3.app.Application;
import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.nodes.PhysicsGhostNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import java.util.logging.Logger;

/**
 *
 * @author tim8dev [at] gmail [dot com]
 */
public class TestGhostObject extends SimpleBulletApplication {

    private PhysicsGhostNode ghostNode;

    public static void main(String[] args) {
        Application app = new TestGhostObject();
        app.start();
    }

    private void initGhostObject() {
        Vector3f halfExtents = new Vector3f(3, 4.2f, 1);
        // create an visual Box to see, where the GhostObject is:
        Box boxGeom = new Box(Vector3f.ZERO,
                halfExtents.x, halfExtents.y, halfExtents.z);
        Geometry ghostGeom = new Geometry("ghostVisual", boxGeom);
        ghostGeom.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        ghostNode = new PhysicsGhostNode(ghostGeom, new BoxCollisionShape(halfExtents));
        rootNode.attachChild(ghostNode);
        getPhysicsSpace().add(ghostNode);
    }

    @Override
    public void simpleInitApp() {

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        // Mesh to be shared across several boxes.
        Box boxGeom = new Box(Vector3f.ZERO, 1f, 1f, 1f);
        // CollisionShape to be shared across several boxes.
        CollisionShape shape = new BoxCollisionShape(new Vector3f(1, 1, 1));

        // Add some phyisics boxes higher up, to fall down and be tracked.
        Geometry geom0 = new Geometry("box", boxGeom);
        geom0.setMaterial(mat);
        PhysicsNode physicsBox = new PhysicsNode(geom0, new BoxCollisionShape(new Vector3f(1, 1, 1)), 1);
        physicsBox.setName("box0");
        physicsBox.setFriction(0.1f);
        physicsBox.setLocalTranslation(new Vector3f(.6f, 4, .5f));
        physicsBox.updateGeometricState();
        physicsBox.updateModelBound();
        rootNode.attachChild(physicsBox);
        getPhysicsSpace().add(physicsBox);

        Geometry geom1 = new Geometry("box", boxGeom);
        geom1.setMaterial(mat);
        PhysicsNode physicsBox1 = new PhysicsNode(geom1, shape, 1);
        physicsBox1.setName("box1");
        physicsBox1.setFriction(0.1f);
        physicsBox1.setLocalTranslation(new Vector3f(0, 40, 0));
        physicsBox1.updateGeometricState();
        physicsBox1.updateModelBound();
        rootNode.attachChild(physicsBox1);
        getPhysicsSpace().add(physicsBox1);

        Geometry geom2 = new Geometry("box", boxGeom);
        geom2.setMaterial(mat);
        PhysicsNode physicsBox2 = new PhysicsNode(geom2, new BoxCollisionShape(new Vector3f(1, 1, 1)), 1);
        physicsBox2.setName("box0");
        physicsBox2.setFriction(0.1f);
        physicsBox2.setLocalTranslation(new Vector3f(.5f, 80, -.8f));
        physicsBox2.updateGeometricState();
        physicsBox2.updateModelBound();
        rootNode.attachChild(physicsBox2);
        getPhysicsSpace().add(physicsBox2);

        // the floor, does not move (mass=0)
        Geometry geom3 = new Geometry("box2", new Box(Vector3f.ZERO, 100f, 1f, 100f));
        geom3.setMaterial(mat);
        geom3.updateGeometricState();
        PhysicsNode node = new PhysicsNode(geom3, new BoxCollisionShape(new Vector3f(100, 1, 100)), 0);
        node.setName("floor");
        node.setLocalTranslation(new Vector3f(0f, -6, 0f));
        rootNode.attachChild(node);
        node.updateModelBound();
        node.updateGeometricState();
        getPhysicsSpace().add(node);

        initGhostObject();
    }

    @Override
    public void simpleUpdate(float tpf) {
        Logger.getLogger(TestGhostObject.class.getName()).
                info(ghostNode.getOverlappingObjects().toString());
    }
}
