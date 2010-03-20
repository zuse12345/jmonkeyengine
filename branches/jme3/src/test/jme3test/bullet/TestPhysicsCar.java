package jme3test.bullet;

import com.jme3.app.SimplePhysicsApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.nodes.PhysicsVehicleNode;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class TestPhysicsCar extends SimplePhysicsApplication implements BindingListener {
    //the new player object
    private PhysicsVehicleNode player;

    /**
     * Main entry point of the application
     */
    public static void main(String[] args) {
        TestPhysicsCar app = new TestPhysicsCar();
        app.start();
    }

    private void setupKeys() {
        inputManager.registerKeyBinding("Lefts", KeyInput.KEY_H);
        inputManager.registerKeyBinding("Rights", KeyInput.KEY_K);
        inputManager.registerKeyBinding("Ups", KeyInput.KEY_U);
        inputManager.registerKeyBinding("Downs", KeyInput.KEY_J);
        inputManager.registerKeyBinding("Space", KeyInput.KEY_SPACE);
        //used with method onBinding in BindingListener interface
        //in order to add function to keys
        inputManager.addTriggerListener(this);
    }

    public void onBinding(String binding, float value) {
        if(binding.equals("Lefts")){
            player.steer(.2f);
        }
        else if(binding.equals("Rights")){
            player.steer(-.2f);
        }
        else if(binding.equals("Ups")){
            player.accelerate(1);
        }
        else if(binding.equals("Downs")){
            player.accelerate(0);
        }
        else if(binding.equals("Space")){
            player.accelerate(0);
            player.steer(0);
        }
    }

    @Override
    public void simpleInitApp() {
        setupKeys();
        setupFloor();
        buildPlayer();
    }


    public void setupFloor() {
        Material mat = new Material(manager, "plain_texture.j3md");
        TextureKey key = new TextureKey("Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = manager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);
        
        Box floor = new Box(Vector3f.ZERO, 100, 1f, 100);
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setMaterial(mat);
        floorGeom.updateModelBound();

        PhysicsNode tb=new PhysicsNode(floorGeom,new MeshCollisionShape(floorGeom.getMesh()),0);
        rootNode.attachChild(tb);
        tb.setLocalTranslation(new Vector3f(0f,-6,0f));
        tb.updateModelBound();
        tb.updateGeometricState();
        getPhysicsSpace().add(tb);
    }

    private void buildPlayer() {
        float r=0.6f;
        float stiffness=90.0f;//200=f1 car
        float compValue=0.4f; //(lower than damp!)
        float dampValue=0.8f;

        Material matBox = new Material(manager, "plain_texture.j3md");
        TextureKey keyBox = new TextureKey("signpost_color.jpg", true);
        keyBox.setGenerateMips(true);
        Texture texBox = manager.loadTexture(keyBox);
        texBox.setMinFilter(Texture.MinFilter.Trilinear);
        matBox.setTexture("m_ColorMap", texBox);
        Material mat = new Material(manager, "plain_texture.j3md");
        TextureKey key = new TextureKey("Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = manager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);
        //box stand in
        Box b = new Box(new Vector3f(0,0,0),0.5f,0.5f,2f);
        Geometry g= new Geometry("Box",b);
        g.setMaterial(matBox);
        g.getMesh().setBound(new BoundingBox());
        player = new PhysicsVehicleNode(g, new BoxCollisionShape(new Vector3f(0.5f,0.5f,2f)),1);

        //setting default values for wheels
        player.setSuspensionCompression(compValue*2.0f*FastMath.sqrt(stiffness));
        player.setSuspensionDamping(dampValue*2.0f*FastMath.sqrt(stiffness));
        player.setSuspensionStiffness(stiffness);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection=new Vector3f(0,-1,0);
        Vector3f wheelAxle=new Vector3f(-1,0,0);
        Sphere wheelSphere=new Sphere(8,8,r);
        Geometry wheels1=new Geometry("wheel 1",wheelSphere);
        wheels1.setMaterial(mat);
        wheels1.setModelBound(new BoundingBox());
        wheels1.updateModelBound();
        player.addWheel(wheels1, new Vector3f(-1f,-0.5f,2f),
                        wheelDirection, wheelAxle, 0.2f, r, true);

        Geometry wheels2=new Geometry("wheel 2",wheelSphere);
        wheels2.setMaterial(mat);
        wheels2.setModelBound(new BoundingBox());
        wheels2.updateModelBound();
        player.addWheel(wheels2, new Vector3f(1f,-0.5f,2f),
                        wheelDirection, wheelAxle, 0.2f, r, true);

        Geometry wheels3=new Geometry("wheel 3",wheelSphere);
        wheels3.setMaterial(matBox);
        wheels3.setModelBound(new BoundingBox());
        wheels3.updateModelBound();
        player.addWheel(wheels3, new Vector3f(-1f,-0.5f,-2f),
                        wheelDirection, wheelAxle, 0.2f, r, false);

        Geometry wheels4=new Geometry("wheel 4",wheelSphere);
        wheels4.setMaterial(matBox);
        wheels4.setModelBound(new BoundingBox());
        wheels4.updateModelBound();
        player.addWheel(wheels4, new Vector3f(1f,-0.5f,-2f),
                        wheelDirection, wheelAxle, 0.2f, r, false);

        player.updateModelBound();
        rootNode.attachChild(player);

        getPhysicsSpace().add(player);
    }

    @Override
    public void simpleUpdate(float tpf) {
        
    }


}