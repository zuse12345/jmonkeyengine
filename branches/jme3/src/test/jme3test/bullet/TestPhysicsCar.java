package jme3test.bullet;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.nodes.PhysicsVehicleNode;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;

public class TestPhysicsCar extends SimpleBulletApplication implements ActionListener {
    private PhysicsVehicleNode player;
    private float steeringValue=0;
    private float accelerationValue=0;

    public static void main(String[] args) {
        TestPhysicsCar app = new TestPhysicsCar();
        app.start();
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this,"Lefts");
        inputManager.addListener(this,"Rights");
        inputManager.addListener(this,"Ups");
        inputManager.addListener(this,"Downs");
        inputManager.addListener(this,"Space");
    }

    @Override
    public void simpleInitApp() {
        setupKeys();
        setupFloor();
        buildPlayer();
    }


    public void setupFloor() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
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
        float r=0.5f;
        float stiffness=90.0f;//200=f1 car
        float compValue=0.4f; //(lower than damp!)
        float dampValue=0.8f;

        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Red);

        //box stand in
        player = new PhysicsVehicleNode(new Node(),new BoxCollisionShape(new Vector3f(1.2f,0.5f,2.4f)),1);

        //setting default values for wheels
        player.setSuspensionCompression(compValue*2.0f*FastMath.sqrt(stiffness));
        player.setSuspensionDamping(dampValue*2.0f*FastMath.sqrt(stiffness));
        player.setSuspensionStiffness(stiffness);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection=new Vector3f(0,-1,0); // was 0, -1, 0
        Vector3f wheelAxle=new Vector3f(-1,0,0); // was -1, 0, 0

        Cylinder wheelMesh = new Cylinder(16, 16, r, r * 0.6f, true);

        Node node1 = new Node("wheel 1 node");
        Geometry wheels1=new Geometry("wheel 1",wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(mat);
        player.addWheel(wheels1, new Vector3f(-1f,-0.5f,2f),
                        wheelDirection, wheelAxle, 0.2f, r, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2=new Geometry("wheel 2",wheelMesh);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(mat);
        player.addWheel(wheels2, new Vector3f(1f,-0.5f,2f),
                        wheelDirection, wheelAxle, 0.2f, r, true);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3=new Geometry("wheel 3",wheelMesh);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(mat);
        player.addWheel(wheels3, new Vector3f(-1f,-0.5f,-2f),
                        wheelDirection, wheelAxle, 0.2f, r, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4=new Geometry("wheel 4",wheelMesh);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(mat);
        player.addWheel(wheels4, new Vector3f(1f,-0.5f,-2f),
                        wheelDirection, wheelAxle, 0.2f, r, false);

        player.updateModelBound();
        player.attachDebugShape(assetManager);
        rootNode.attachChild(player);

        getPhysicsSpace().add(player);
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            if(value)
                steeringValue+=.5f;
            else
                steeringValue+=-.5f;
            player.steer(steeringValue);
        } else if (binding.equals("Rights")) {
            if(value)
                steeringValue+=-.5f;
            else
                steeringValue+=.5f;
            player.steer(steeringValue);
        } else if (binding.equals("Ups")) {
            if(value)
                accelerationValue+=2;
            else
                accelerationValue-=2;
            player.accelerate(accelerationValue);
        } else if (binding.equals("Downs")) {
            if(value)
                player.brake(60f);
            else
                player.brake(0f);
        }
    }

}