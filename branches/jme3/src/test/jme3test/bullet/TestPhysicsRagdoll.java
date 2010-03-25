package jme3test.bullet;

import com.jme3.app.SimplePhysicsApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.MeshLoader;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

public class TestPhysicsRagdoll  extends SimplePhysicsApplication {
//    private PhysicsSpace pSpace=PhysicsSpace.getPhysicsSpace();

    public static void main(String[] args){
        TestPhysicsRagdoll app = new TestPhysicsRagdoll();
        app.start();
    }

    public void simpleInitApp() {

        Material mat = new Material(manager, "plain_texture.j3md");
        TextureKey key = new TextureKey("Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = manager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        // the floor, does not move (mass=0)
        Geometry geom5=new Geometry("box2",new Box(Vector3f.ZERO,100f,0.2f,100f));
        geom5.setMaterial(mat);
        geom5.updateGeometricState();
        PhysicsNode node3=new PhysicsNode(geom5,new MeshCollisionShape(geom5.getMesh()),0);
        node3.setLocalTranslation(new Vector3f(0f,-6,0f));
        rootNode.attachChild(node3);
        node3.updateModelBound();
        node3.updateGeometricState();
        getPhysicsSpace().addQueued(node3);
        
        Spatial model = MeshLoader.loadModel(manager, "OTO.meshxml", "OTO.material");
        model.setLocalScale(10);
//        PhysicsRagdollNode ragdoll = new PhysicsRagdollNode(model);
//        rootNode.attachChild(ragdoll);
        rootNode.attachChild(model);
    }

}
