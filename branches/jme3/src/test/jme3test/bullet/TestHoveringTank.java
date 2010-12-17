/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jme3test.bullet;

import com.jme3.app.SimpleApplication;
import com.jme3.app.SimpleBulletApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.texture.Texture.WrapMode;

public class TestHoveringTank extends SimpleApplication implements AnalogListener,
                                                                      ActionListener {
    private BulletAppState bulletAppState;
    private PhysicsNode player;
    private Spatial spaceCraft;
    private float steeringValue=0;
    private float accelerationValue=0;

    private BitmapText angular;
    private BitmapText linear;

    public static void main(String[] args) {
        TestHoveringTank app = new TestHoveringTank();
        app.start();
    }

    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this,"Lefts");
        inputManager.addListener(this,"Rights");
        inputManager.addListener(this,"Ups");
        inputManager.addListener(this,"Downs");
        inputManager.addListener(this,"Space");
        inputManager.addListener(this,"Reset");
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        if (settings.getRenderer().startsWith("LWJGL")){
//            BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 1024);
//            bsr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
//            viewPort.addProcessor(bsr);

            PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager, 512, 3);
            pssmRenderer.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
            viewPort.addProcessor(pssmRenderer);
        }
        cam.setFrustumFar(50f);

        setupKeys();
        setupFloor();
        buildPlayer();

//        DirectionalLight dl = new DirectionalLight();
//        dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
//        rootNode.addLight(dl);
//
//        dl = new DirectionalLight();
//        dl.setDirection(new Vector3f(0.5f, -0.1f, 0.3f).normalizeLocal());
//        rootNode.addLight(dl);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new ColorRGBA(1.0f, 0.94f, 0.8f, 1f).multLocal(1.3f));
        dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        rootNode.addLight(dl);

        Vector3f lightDir2 = new Vector3f(0.70518064f, 0.5902297f, -0.39287305f);
        DirectionalLight dl2 = new DirectionalLight();
        dl2.setColor(new ColorRGBA(0.7f, 0.85f, 1.0f, 1f));
        dl2.setDirection(lightDir2);
        rootNode.addLight(dl2);

        angular = new BitmapText(guiFont);
        linear  = new BitmapText(guiFont);

        linear.setLocalTranslation(0,  settings.getHeight(), 0);
        angular.setLocalTranslation(0, settings.getHeight() - linear.getLineHeight(), 0);

        guiNode.attachChild(angular);
        guiNode.attachChild(linear);
    }


    public void setupFloor() {
        Material mat = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall.j3m");
        mat.getTextureParam("m_DiffuseMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_NormalMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_ParallaxMap").getTextureValue().setWrap(WrapMode.Repeat);

        Box floor = new Box(Vector3f.ZERO, 40, 1f, 40);
        floor.scaleTextureCoordinates(new Vector2f(12.0f, 12.0f));
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setShadowMode(ShadowMode.Receive);
        floorGeom.setMaterial(mat);

        BoxCollisionShape colShape = new BoxCollisionShape(new Vector3f(40, 1, 40));
        PhysicsNode tb=new PhysicsNode(floorGeom,colShape,0);
        tb.setName("Floor");
        tb.setLocalTranslation(new Vector3f(0f,-7,0f));

        rootNode.attachChild(tb);
        getPhysicsSpace().add(tb);
    }

    private void buildPlayer() {
        spaceCraft = assetManager.loadModel("Models/HoverTank/Tank2.mesh.xml");
        spaceCraft.scale(0.5f);
//        spaceCraft.rotate(0, FastMath.PI, 0);
        spaceCraft.updateGeometricState();

        BoundingBox box = (BoundingBox) spaceCraft.getWorldBound();
        final Vector3f extent = box.getExtent(null);
        float height = extent.getY() * 2f;
        float width  = extent.getX() * 2f;

//        spaceCraft.move(0, -height, 0);

        CompoundCollisionShape colShape = new CompoundCollisionShape();

//        BoxCollisionShape boxShape2 = new BoxCollisionShape(extent.clone().multLocal(2, 0.5f, 2));
//        colShape.addChildShape(boxShape2, new Vector3f(0, -height, 0));

//        CollisionShapeFactory.shiftCompoundShapeContents(colShape, new Vector3f(0, -height, 0));

        BoxCollisionShape boxShape = new BoxCollisionShape(extent);
        colShape.addChildShape(boxShape, Vector3f.ZERO);

        player = new PhysicsNode(spaceCraft, colShape, 1000);
        player.setName("Player");
        player.setSleepingThresholds(0.75f, 0.2f);
        player.setRestitution(0); // do not bounce
        player.attachDebugShape(assetManager);

        spaceCraft.setShadowMode(ShadowMode.CastAndReceive);

        rootNode.attachChild(player);
        getPhysicsSpace().add(player);

        flyCam.setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(cam, spaceCraft, inputManager);

        getPhysicsSpace().addCollisionGroupListener(new PhysicsCollisionGroupListener() {
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB) {
                String a = nodeA.getName();
                String b = nodeB.getName();
                if (a.equals("Missile") && b.equals("Player"))
                    return false;
                else if (a.equals("Player") && b.equals("Missile"))
                    return false;
                
                return true;
            }
        }, PhysicsNode.COLLISION_GROUP_01);
    }

    public void makeMissile(){
        Vector3f pos   = spaceCraft.getWorldTranslation().clone();
        Quaternion rot = spaceCraft.getWorldRotation();
        Vector3f dir   = rot.getRotationColumn(2);

        Spatial missile = assetManager.loadModel("Models/SpaceCraft/Rocket.mesh.xml");
        missile.scale(0.5f);
        missile.rotate(0, FastMath.PI, 0);
        missile.updateGeometricState();

        BoundingBox box = (BoundingBox) missile.getWorldBound();
        final Vector3f extent = box.getExtent(null);

        BoxCollisionShape boxShape = new BoxCollisionShape(extent);

        PhysicsNode physMissile = new PhysicsNode(missile, boxShape, 20);
        physMissile.setName("Missile");
        physMissile.rotate(rot);
        physMissile.setLocalTranslation(pos.addLocal(0,extent.y*4.5f,0));
        
        physMissile.clearForces();
        physMissile.applyContinuousForce(true, new Vector3f(0, 9.81f, 0));
        physMissile.applyForce(dir.mult(50000), Vector3f.ZERO);
        physMissile.setRestitution(0); // do not bounce
//        physMissile.attachDebugShape(assetManager);
        physMissile.setLinearDamping(0);
        physMissile.setAngularDamping(1); // prevent rotation

        physMissile.setShadowMode(ShadowMode.CastAndReceive);

        rootNode.attachChild(physMissile);
        getPhysicsSpace().add(physMissile);
    }

     public void updatePlayer(){
         rootNode.updateGeometricState();

         Vector3f angVel = player.getAngularVelocity();
         float velocity = angVel.getY();

         Quaternion q = player.getWorldRotation();
         Vector3f dir = q.getRotationColumn(2);
         dir.y = 0;
         dir.normalizeLocal();

         Vector3f vel = player.getLinearVelocity();

         linear.setText("Linear Velocity: " + ((int)vel.length()) );
         angular.setText("Angular Velocity: " + ((int)velocity) );

         Quaternion newRot = new Quaternion();
         float pitch = (velocity / 3f) * FastMath.HALF_PI;
         float yaw   = (vel.length() / 5f / 10f) * FastMath.HALF_PI * 1f;
         newRot.fromAngles(yaw, 0/*FastMath.PI*/, pitch);
         spaceCraft.setLocalRotation(newRot);

        if (steeringValue != 0){
            if (velocity < 1 && velocity > -1){
                player.applyTorque(new Vector3f(0, steeringValue, 0));
            }
            steeringValue = 0;
        }else{
            // counter the steering value!
            if (velocity > 0.2f){
                player.applyTorque(new Vector3f(0, -10000, 0));
            }else if (velocity < -0.2f){
                player.applyTorque(new Vector3f(0,  10000, 0));
            }
        }
        if (accelerationValue > 0){

            // counter force that will adjust velocity
            // if we are not going where we want to go.
            // this will prevent "drifting" and thus improve control
            // of the vehicle
            float d = dir.dot(vel.normalize());
            Vector3f counter = dir.project(vel).normalizeLocal().negateLocal().multLocal(1-d);
            // adjust the "2000" value to increase or decrease counter-drifting
            player.applyForce(counter.mult(2000), Vector3f.ZERO);

            if (vel.length() < 10){
                player.applyForce(dir.mult(accelerationValue), Vector3f.ZERO);
            }

            accelerationValue = 0;
        }else{
             // counter the acceleration value
             if (vel.length() > FastMath.ZERO_TOLERANCE){
                 vel.normalizeLocal().negateLocal();
                 player.applyForce(vel.mult(2000), Vector3f.ZERO);
             }
        }
    }

    public void onAnalog(String binding, float value, float tpf) {
        if (binding.equals("Lefts")) {
            steeringValue = 50000f * value/tpf;
        } else if (binding.equals("Rights")) {
            steeringValue = -50000f * value/tpf;
        } else if (binding.equals("Ups")) {
            accelerationValue = 8000f * value/tpf;
        } else if (binding.equals("Downs")) {
        }
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Reset")) {
            if (value) {
                System.out.println("Reset");
                player.setLocalTranslation(0, 0, 0);
                player.setLocalRotation(new Quaternion());
                player.setLinearVelocity(Vector3f.ZERO);
                player.setAngularVelocity(Vector3f.ZERO);
            } else {
            }
        }else if (binding.equals("Space") && value){
            makeMissile();
        }
    }

    public void updateCamera(){
        rootNode.updateGeometricState();

        Vector3f pos   = player.getWorldTranslation().clone();
        Quaternion rot = player.getWorldRotation();
        Vector3f dir   = rot.getRotationColumn(2);

        // make it XZ only
        Vector3f camPos = new Vector3f(dir);
        camPos.setY(0);
        camPos.normalizeLocal();

        // negate and multiply by distance from object
        camPos.negateLocal();
        camPos.multLocal(15);

        // add Y distance
        camPos.setY(2);
        camPos.addLocal(pos);
        cam.setLocation(camPos);

        Vector3f lookAt = new Vector3f(dir);
        lookAt.multLocal(7); // look at dist
        lookAt.addLocal(pos);
        cam.lookAt(lookAt, Vector3f.UNIT_Y);
    }

    @Override
    public void simpleUpdate(float tpf) {
        updatePlayer();
//        updateCamera();
    }



}