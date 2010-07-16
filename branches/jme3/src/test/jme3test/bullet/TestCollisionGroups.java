/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

/**
 *
 * @author normenhansen
 */
public class TestCollisionGroups extends SimpleBulletApplication{

    public static void main(String[] args){
        TestCollisionGroups app = new TestCollisionGroups();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

//         Add a physics sphere to the world
        Sphere sphere=new Sphere(16,16,1f);
        Geometry geom=new Geometry("sphere",sphere);
        geom.setMaterial(mat);
        PhysicsNode physicsSphere=new PhysicsNode(geom,new SphereCollisionShape(1),1);
        physicsSphere.setLocalTranslation(new Vector3f(3,6,0));
        physicsSphere.updateGeometricState();
        physicsSphere.updateModelBound();
        rootNode.attachChild(physicsSphere);
        getPhysicsSpace().add(physicsSphere);

        // Add a physics sphere to the world using the collision shape from sphere one
        Sphere sphere2=new Sphere(16,16,1f);
        Geometry geom2=new Geometry("sphere2",sphere2);
        geom2.setMaterial(mat);
        PhysicsNode physicsSphere2=new PhysicsNode(geom2,physicsSphere.getCollisionShape(),1);
        physicsSphere2.setLocalTranslation(new Vector3f(4,8,0));
        physicsSphere2.updateGeometricState();
        physicsSphere2.updateModelBound();
        physicsSphere2.addCollideWithGroup(PhysicsNode.COLLISION_GROUP_02);
        rootNode.attachChild(physicsSphere2);
        getPhysicsSpace().add(physicsSphere2);

        // an obstacle mesh, does not move (mass=0)
        Geometry geom4=new Geometry("node2",new Sphere(16,16,1.2f));
        geom4.setMaterial(mat);
        PhysicsNode node2=new PhysicsNode(geom4,new MeshCollisionShape(geom4.getMesh()),0);
        node2.setLocalTranslation(new Vector3f(2.5f,-4,0f));
        node2.setCollisionGroup(PhysicsNode.COLLISION_GROUP_02);
        node2.setCollideWithGroups(PhysicsNode.COLLISION_GROUP_02);
        rootNode.attachChild(node2);
        getPhysicsSpace().add(node2);

        // the floor, does not move (mass=0)
        Geometry geom5=new Geometry("box2",new Box(Vector3f.ZERO,100f,0.2f,100f));
        geom5.setMaterial(mat);
        geom5.updateGeometricState();
        PhysicsNode node3=new PhysicsNode(geom5,new MeshCollisionShape(geom5.getMesh()),0);
        node3.setLocalTranslation(new Vector3f(0f,-6,0f));
        rootNode.attachChild(node3);
        node3.updateModelBound();
        node3.updateGeometricState();
        getPhysicsSpace().add(node3);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
