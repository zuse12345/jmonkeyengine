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
package com.jme3.bullet.nodes;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.CollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.nodes.infos.PhysicsNodeState;
import com.jme3.bullet.util.Converter;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Matrix3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>PhysicsNode - Basic physics object</p>
 * @author normenhansen
 */
public class PhysicsNode extends CollisionObject {

    protected RigidBodyConstructionInfo constructionInfo;
    protected RigidBody rBody;
    protected PhysicsNodeState motionState = new PhysicsNodeState();
    protected boolean rebuildBody = true;
    protected float mass = 1.0f;
    protected boolean kinematic = false;
    protected javax.vecmath.Vector3f tempVec = new javax.vecmath.Vector3f();
    protected javax.vecmath.Vector3f tempVec2 = new javax.vecmath.Vector3f();
    protected Transform tempTrans = new Transform(new javax.vecmath.Matrix3f());
    protected javax.vecmath.Matrix3f tempMatrix = new javax.vecmath.Matrix3f();
    //jme-specific
    protected Vector3f continuousForce = new Vector3f();
    protected Vector3f continuousForceLocation = new Vector3f();
    protected Vector3f continuousTorque = new Vector3f();
    //TEMP VARIABLES
    private javax.vecmath.Vector3f localInertia = new javax.vecmath.Vector3f();
    protected boolean applyForce = false;
    protected boolean applyTorque = false;
    private ArrayList<PhysicsJoint> joints = new ArrayList<PhysicsJoint>();

    public PhysicsNode() {
    }

    /**
     * creates a new PhysicsNode with the supplied collision shape
     * @param child
     * @param shape
     */
    public PhysicsNode(CollisionShape shape) {
        collisionShape = shape;
        rebuildRigidBody();
    }

    public PhysicsNode(CollisionShape shape, float mass) {
        collisionShape = shape;
        this.mass = mass;
        rebuildRigidBody();
    }

    /**
     * creates a new PhysicsNode with the supplied child node or geometry and
     * sets the supplied collision shape to the PhysicsNode
     * @param child
     * @param shape
     */
    public PhysicsNode(Spatial child, CollisionShape shape) {
        this(child, shape, 1.0f);
    }

    /**
     * creates a new PhysicsNode with the supplied child node or geometry and
     * uses the supplied collision shape for that PhysicsNode<br>
     * @param child
     * @param shape
     */
    public PhysicsNode(Spatial child, CollisionShape shape, float mass) {
        if (child != null) {
            this.attachChild(child);
        }
        this.mass = mass;
        this.collisionShape = shape;
        rebuildRigidBody();
    }

    /**
     * builds/rebuilds the phyiscs body when parameters have changed
     */
    protected void rebuildRigidBody() {
        boolean removed = false;
        if (rBody != null) {
            if (rBody.isInWorld()) {
                PhysicsSpace.getPhysicsSpace().remove(this);
                removed = true;
            }
            rBody.destroy();
        }
        preRebuild();
        rBody = new RigidBody(constructionInfo);
        postRebuild();
        if (removed) {
            PhysicsSpace.getPhysicsSpace().add(this);
        }
        rebuildBody = false;
    }

    protected void preRebuild() {
        collisionShape.calculateLocalInertia(mass, localInertia);
        if (constructionInfo == null) {
            constructionInfo = new RigidBodyConstructionInfo(mass, motionState, collisionShape.getCShape(), localInertia);
        } else {
            constructionInfo.mass = mass;
            constructionInfo.collisionShape = collisionShape.getCShape();
            constructionInfo.motionState = motionState;
        }
    }

    protected void postRebuild() {
        rBody.setUserPointer(this);
        if (mass == 0.0f) {
            rBody.setCollisionFlags(rBody.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);
        } else {
            rBody.setCollisionFlags(rBody.getCollisionFlags() & ~CollisionFlags.STATIC_OBJECT);
        }
    }

    @Override
    public void updateGeometricState() {
        if ((refreshFlags & RF_LIGHTLIST) != 0) {
            updateWorldLightList();
        }

        if ((refreshFlags & RF_TRANSFORM) != 0) {
            // combine with parent transforms- same for all spatial
            // subclasses.
            updateWorldTransforms();
            motionState.setWorldTransform(getWorldTranslation(), getWorldRotation());
        } else if (motionState.applyTransform(this)) {
            updateWorldTransforms();
        }

        // the important part- make sure child geometric state is refreshed
        // first before updating own world bound. This saves
        // a round-trip later on.
        for (int i = 0, cSize = children.size(); i < cSize; i++) {
            Spatial child = children.get(i);
            child.updateGeometricState();
        }

        if ((refreshFlags & RF_BOUND) != 0) {
            updateWorldBound();
        }

        //only called to sync debug shapes in CollisionObject
        super.updateGeometricState();
    }

    /**
     * only to be called from physics thread!!
     */
    @Override
    public void updatePhysicsState() {
        if (rebuildBody) {
            rebuildRigidBody();
        }
        motionState.applyTransform(rBody);
    }

    /**
     * this is normally only needed when using detached physics
     * @param location the location of the actual physics object
     */
    public void setPhysicsLocation(Vector3f location) {
        rBody.getWorldTransform(tempTrans);
        Converter.convert(location, tempTrans.origin);
        rBody.setWorldTransform(tempTrans);
    }

    /**
     * this is normally only needed when using detached physics
     * @param rotation the rotation of the actual physics object
     */
    public void setPhysicsRotation(Matrix3f rotation) {
        rBody.getWorldTransform(tempTrans);
        Converter.convert(rotation, tempTrans.basis);
        rBody.setWorldTransform(tempTrans);
    }

    /**
     * this is normally only needed when using detached physics
     * @param location the location of the actual physics object is stored in this Vector3f
     */
    public void getPhysicsLocation(Vector3f location) {
        rBody.getWorldTransform(tempTrans);
        Converter.convert(tempTrans.origin, location);
    }

    /**
     * this is normally only needed when using detached physics
     * @param rotation the rotation of the actual physics object is stored in this Matrix3f
     */
    public void getPhysicsRotation(Matrix3f rotation) {
        rBody.getWorldTransform(tempTrans);
        Converter.convert(tempTrans.basis, rotation);
    }

    /**
     * sets the node to kinematic mode. in this mode the node is not affected by physics
     * but affects other physics objects. its kinetic force is calculated by the amount
     * of movement it is exposed to and its weight.
     * @param kinematic
     */
    public void setKinematic(boolean kinematic) {
        this.kinematic = kinematic;
        if (kinematic) {
            rBody.setCollisionFlags(rBody.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
            rBody.setActivationState(com.bulletphysics.collision.dispatch.CollisionObject.DISABLE_DEACTIVATION);
        } else {
            rBody.setCollisionFlags(rBody.getCollisionFlags() & ~CollisionFlags.KINEMATIC_OBJECT);
            rBody.setActivationState(com.bulletphysics.collision.dispatch.CollisionObject.ACTIVE_TAG);
        }
    }

    public boolean isKinematic() {
        return kinematic;
    }

    public float getMass() {
        return mass;
    }

    /**
     * sets the mass of this PhysicsNode, objects with mass=0 are static.
     * @param mass
     */
    public void setMass(float mass) {
        this.mass = mass;
        rBody.setMassProps(mass, localInertia);
        if (mass == 0.0f) {
            rBody.setCollisionFlags(rBody.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);
        } else {
            rBody.setCollisionFlags(rBody.getCollisionFlags() & ~CollisionFlags.STATIC_OBJECT);
        }
    }

    public void getGravity(Vector3f gravity) {
        rBody.getGravity(tempVec);
        Converter.convert(tempVec, gravity);
    }

    /**
     * set the gravity of this PhysicsNode
     * @param gravity the gravity vector to set
     */
    public void setGravity(Vector3f gravity) {
        rBody.setGravity(Converter.convert(gravity, tempVec));
    }

    public float getFriction() {
        return rBody.getFriction();
    }

    /**
     * sets the friction of this physics object
     * @param friction the friction of this physics object
     */
    public void setFriction(float friction) {
        constructionInfo.friction = friction;
        rBody.setFriction(friction);
    }

    public void setDamping(float linearDamping, float angularDamping) {
        constructionInfo.linearDamping = linearDamping;
        constructionInfo.angularDamping = angularDamping;
        rBody.setDamping(linearDamping, angularDamping);
    }

    public float getRestitution() {
        return rBody.getRestitution();
    }

    /**
     * the "bouncyness" of the PhysicsNode
     * best performance if restitution=0
     * @param restitution
     */
    public void setRestitution(float restitution) {
        constructionInfo.restitution = restitution;
        rBody.setRestitution(restitution);
    }

    /**
     * get the current angular velocity of this PhysicsNode
     * @return the current linear velocity
     */
    public Vector3f getAngularVelocity() {
        return Converter.convert(rBody.getAngularVelocity(tempVec));
    }

    /**
     * get the current angular velocity of this PhysicsNode
     * @param vec the vector to store the velocity in
     */
    public void getAngularVelocity(Vector3f vec) {
        Converter.convert(rBody.getAngularVelocity(tempVec), vec);
    }

    /**
     * sets the angular velocity of this PhysicsNode
     * @param vec the angular velocity of this PhysicsNode
     */
    public void setAngularVelocity(Vector3f vec) {
        rBody.setAngularVelocity(Converter.convert(vec, tempVec));
        rBody.activate();
    }

    /**
     * get the current linear velocity of this PhysicsNode
     * @return the current linear velocity
     */
    public Vector3f getLinearVelocity() {
        return Converter.convert(rBody.getLinearVelocity(tempVec));
    }

    /**
     * get the current linear velocity of this PhysicsNode
     * @param vec the vector to store the velocity in
     */
    public void getLinearVelocity(Vector3f vec) {
        Converter.convert(rBody.getLinearVelocity(tempVec), vec);
    }

    /**
     * sets the linear velocity of this PhysicsNode
     * @param vec the linear velocity of this PhysicsNode
     */
    public void setLinearVelocity(Vector3f vec) {
        rBody.setLinearVelocity(Converter.convert(vec, tempVec));
        rBody.activate();
    }

    /**
     * get the currently applied continuous force
     * @param vec the vector to store the continuous force in
     * @return null if no force is applied
     */
    public Vector3f getContinuousForce(Vector3f vec) {
        if (applyForce) {
            return vec.set(continuousForce);
        } else {
            return null;
        }
    }

    /**
     * get the currently applied continuous force
     * @return null if no force is applied
     */
    public Vector3f getContinuousForce() {
        if (applyForce) {
            return continuousForce;
        } else {
            return null;
        }
    }

    /**
     * get the currently applied continuous force location
     * @return null if no force is applied
     */
    public Vector3f getContinuousForceLocation() {
        if (applyForce) {
            return continuousForceLocation;
        } else {
            return null;
        }
    }

    /**
     * apply a continuous force to this PhysicsNode, the force is updated automatically each
     * tick so you only need to set it once and then set it to false to stop applying
     * the force.
     * @param apply true if the force should be applied each physics tick
     * @param force the vector of the force to apply
     */
    public void applyContinuousForce(boolean apply, Vector3f force) {
        if (force != null) {
            continuousForce.set(force);
        }
        continuousForceLocation.set(0, 0, 0);
        if (!applyForce && apply) {
            PhysicsSpace.enqueueOnThisThread(doApplyContinuousForce);
        }
        applyForce = apply;

    }

    /**
     * apply a continuous force to this PhysicsNode, the force is updated automatically each
     * tick so you only need to set it once and then set it to false to stop applying
     * the force.
     * @param apply true if the force should be applied each physics tick
     * @param force the offset of the force
     */
    public void applyContinuousForce(boolean apply, Vector3f force, Vector3f location) {
        if (force != null) {
            continuousForce.set(force);
        }
        if (location != null) {
            continuousForceLocation.set(location);
        }
        if (!applyForce && apply) {
            PhysicsSpace.enqueueOnThisThread(doApplyContinuousForce);
        }
        applyForce = apply;

    }

    /**
     * use to enable/disable continuous force
     * @param apply set to false to disable
     */
    public void applyContinuousForce(boolean apply) {
        if (!applyForce && apply) {
            PhysicsSpace.enqueueOnThisThread(doApplyContinuousForce);
        }
        applyForce = apply;
    }
    private Callable doApplyContinuousForce = new Callable() {

        public Object call() throws Exception {
            rBody.applyForce(Converter.convert(continuousForce, tempVec), Converter.convert(continuousForceLocation, tempVec2));
            rBody.activate();
            if (applyForce) {
                PhysicsSpace.requeueOnThisThread(doApplyContinuousForce);
            }
            return null;
        }
    };

    /**
     * get the currently applied continuous torque
     * @return null if no torque is applied
     */
    public Vector3f getContinuousTorque() {
        if (applyTorque) {
            return continuousTorque;
        } else {
            return null;
        }
    }

    /**
     * get the currently applied continuous torque
     * @param vec the vector to store the continuous torque in
     * @return null if no torque is applied
     */
    public Vector3f getContinuousTorque(Vector3f vec) {
        if (applyTorque) {
            return vec.set(continuousTorque);
        } else {
            return null;
        }
    }

    /**
     * apply a continuous torque to this PhysicsNode. The torque is updated automatically each
     * tick so you only need to set it once and then set it to false to stop applying
     * the torque.
     * @param apply true if the force should be applied each physics tick
     * @param vec the vector of the force to apply
     */
    public void applyContinuousTorque(boolean apply, Vector3f vec) {
        if (vec != null) {
            continuousTorque.set(vec);
        }
        if (!applyTorque && apply) {
            PhysicsSpace.enqueueOnThisThread(doApplyContinuousTorque);
        }
        applyTorque = apply;
    }

    /**
     * use to enable/disable continuous torque
     * @param apply set to false to disable
     */
    public void applyContinuousTorque(boolean apply) {
        if (!applyTorque && apply) {
            PhysicsSpace.enqueueOnThisThread(doApplyContinuousTorque);
        }
        applyTorque = apply;
    }
    private Callable doApplyContinuousTorque = new Callable() {

        public Object call() throws Exception {
            rBody.applyTorque(Converter.convert(continuousTorque, tempVec));
            rBody.activate();
            if (applyTorque) {
                PhysicsSpace.requeueOnThisThread(doApplyContinuousTorque);
            }
            return null;
        }
    };

    /**
     * apply a force to the PhysicsNode, only applies force in the next physics tick,
     * use applyContinuousForce to apply continuous force
     * 
     * @param force the force
     * @param location the location of the force
     */
    public void applyForce(final Vector3f force, final Vector3f location) {
        rBody.applyForce(Converter.convert(force, tempVec), Converter.convert(location, tempVec2));
        rBody.activate();
    }

    /**
     * apply a force to the PhysicsNode, only applies force in the next physics tick,
     * use applyContinuousForce to apply continuous force
     * 
     * @param force the force
     */
    public void applyCentralForce(final Vector3f force) {
        rBody.applyCentralForce(Converter.convert(force, tempVec));
        rBody.activate();
    }

    /**
     * apply a torque to the PhysicsNode, only applies force in the next physics tick,
     * use applyContinuousTorque to apply continuous torque
     * 
     * @param torque the torque
     */
    public void applyTorque(final Vector3f torque) {
        rBody.applyTorque(Converter.convert(torque, tempVec));
        rBody.activate();
    }

    /**
     * apply an impulse to the PhysicsNode
     * @param impulse applied impulse
     * @param rel_pos location relative to object
     */
    public void applyImpulse(final Vector3f impulse, final Vector3f rel_pos) {
        rBody.applyImpulse(Converter.convert(impulse, tempVec), Converter.convert(rel_pos, tempVec2));
        rBody.activate();
    }

    /**
     * apply a torque impulse to the PhysicsNode
     * 
     * @param vec
     */
    public void applyTorqueImpulse(final Vector3f vec) {
        rBody.applyTorqueImpulse(Converter.convert(vec, tempVec));
        rBody.activate();
    }

    /**
     * clear all forces from the PhysicsNode
     * 
     */
    public void clearForces() {
        rBody.clearForces();
    }

    /**
     * @return the CollisionShape of this PhysicsNode, to be able to reuse it with
     * other physics nodes (increases performance)
     */
    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    /**
     * sets a CollisionShape to be used for this PhysicsNode for reusing CollisionShapes
     * @param collisionShape the CollisionShape to set
     */
    public void setCollisionShape(CollisionShape collisionShape) {
        this.collisionShape = collisionShape;
        rebuildRigidBody();
//        rebuildBody = true;
    }

    /**
     * reactivates this PhysicsNode when it has been deactivated because it was not moving
     */
    public void activate() {
        rBody.activate();
    }

    public boolean isActive() {
        return rBody.isActive();
    }

    /**
     * sets the sleeping thresholds, these define when the object gets deactivated
     * to save ressources. Low values keep the object active when it barely moves
     * @param linear the linear sleeping threshold
     * @param angular the angular sleeping threshold
     */
    public void setSleepingThresholds(float linear, float angular) {
        constructionInfo.linearSleepingThreshold = linear;
        constructionInfo.angularSleepingThreshold = angular;
        rBody.setSleepingThresholds(linear, angular);
    }

    /**
     * do not use manually, joints are added automatically
     */
    public void addJoint(PhysicsJoint joint) {
        if (!joints.contains(joint)) {
            joints.add(joint);
        }
    }

    /**
     * 
     */
    public void removeJoint(PhysicsJoint joint) {
        joints.remove(joint);
    }

    /**
     * Returns a list of connected joints. This list is only filled when
     * the PhysicsNode is actually added to the physics space or loaded from disk.
     * @return list of active joints connected to this physicsnode
     */
    public List<PhysicsJoint> getJoints() {
        return joints;
    }

    /**
     * used internally
     */
    public RigidBody getRigidBody() {
        return rBody;
    }

    /**
     * destroys this PhysicsNode and removes it from memory
     */
    public void destroy() {
        rBody.destroy();
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        super.write(e);
        OutputCapsule capsule = e.getCapsule(this);

        capsule.write(getMass(), "mass", 1.0f);

        Vector3f store = new Vector3f();
        getGravity(store);

        capsule.write(store, "gravity", Vector3f.ZERO);
        capsule.write(getFriction(), "friction", 0);
        capsule.write(getRestitution(), "restitution", 0);
        capsule.write(constructionInfo.linearDamping, "linearDamping", 0);
        capsule.write(constructionInfo.angularDamping, "angularDamping", 0);

        capsule.write(kinematic, "kinematic", false);
        capsule.write(continuousForce, "continuousForce", Vector3f.ZERO);
        capsule.write(continuousForceLocation, "continuousForceLocation", Vector3f.ZERO);
        capsule.write(continuousTorque, "continuousTorque", Vector3f.ZERO);
        capsule.write(applyForce, "applyForce", false);
        capsule.write(applyTorque, "applyTorque", false);

        capsule.write(collisionShape, "collisionShape", null);

        capsule.writeSavableArrayList(joints, "joints", null);
    }

    @Override
    public void read(JmeImporter e) throws IOException {
        super.read(e);

        InputCapsule capsule = e.getCapsule(this);
        float mass = capsule.readFloat("mass", 1.0f);
        this.mass = mass;
        CollisionShape shape = (CollisionShape) capsule.readSavable("collisionShape", new BoxCollisionShape(Vector3f.UNIT_XYZ));
        collisionShape = shape;
        rebuildRigidBody();
        setGravity((Vector3f) capsule.readSavable("gravity", Vector3f.ZERO));
        setFriction(capsule.readFloat("friction", 0));
        float linearDamping = capsule.readFloat("linearDamping", 0);
        float angularDamping = capsule.readFloat("angularDamping", 0);
        setDamping(linearDamping, angularDamping);
        setRestitution(capsule.readFloat("restitution", 0));
        setKinematic(capsule.readBoolean("kinematic", false));

        Vector3f continuousForce = (Vector3f) capsule.readSavable("continuousForce", Vector3f.ZERO);
        Vector3f continuousForceLocation = (Vector3f) capsule.readSavable("continuousForceLocation", Vector3f.ZERO);
        boolean applyForce = capsule.readBoolean("applyForce", false);
        applyContinuousForce(applyForce, continuousForce, continuousForceLocation);

        Vector3f continuousTorque = (Vector3f) capsule.readSavable("continuousTorque", Vector3f.ZERO);
        boolean applyTorque = capsule.readBoolean("applyTorque", false);
        applyContinuousTorque(applyTorque, continuousTorque);

        joints = capsule.readSavableArrayList("joints", null);
    }
}
