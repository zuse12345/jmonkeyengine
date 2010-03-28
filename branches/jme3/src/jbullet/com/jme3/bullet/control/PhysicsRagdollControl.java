package com.jme3.bullet.control;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.joints.PhysicsConeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.control.ControlType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PhysicsRagdollControl implements Control {

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ControlType getType() {
        return ControlType.BoneControl;
    }

    public void setSpatial(Spatial spatial) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnabled(boolean enabled) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEnabled() {
        return true;
    }

    public void update(float tpf) {
        for (PhysicsBoneLink link : boneLinks.values()){
            link.bone.setUserTransformsWorld(link.shapeNode.getWorldTranslation(),
                                             link.shapeNode.getWorldRotation());
        }
    }

    public void render(RenderManager rm, ViewPort vp) {
    }

    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class PhysicsBoneLink {
        Bone bone;
        PhysicsJoint joint;
        PhysicsNode shapeNode;
        Vector3f pivotA;
        Vector3f pivotB;
    }

    private Map<Bone, PhysicsBoneLink> boneLinks = new HashMap<Bone, PhysicsBoneLink>();
    private Skeleton skeleton;
    private PhysicsSpace space;

    public PhysicsRagdollControl(Spatial model, PhysicsSpace space){
        this.space=space;
        AnimControl animControl = (AnimControl) model.getControl(ControlType.BoneAnimation);
        skeleton = animControl.getSkeleton();

        // put into bind pose and compute bone transforms in model space
        // maybe dont to ragdoll out of animations?
        skeleton.resetAndUpdate();

        for (int i = 0; i < skeleton.getBoneCount(); i++){
            Bone bone = skeleton.getBone(i);
            bone.setUserControl(true);
            if (bone.getParent() == null)
                continue; // no parent .. this is a root bone

            Bone parentBone = bone.getParent();

            Vector3f parentPos = parentBone.getWorldPosition();
            Vector3f bonePos = bone.getWorldPosition();
            Vector3f jointCenter = parentPos.add(bonePos).multLocal(0.5f);

            // length of the joint
            float height = parentPos.distance(bonePos);

            // radius 1 ?
            CapsuleCollisionShape shape = new CapsuleCollisionShape(1, height);

            // mass 1 ?
            PhysicsNode shapeNode = new PhysicsNode(null, shape, 1);
            shapeNode.setLocalTranslation(bonePos);
//            shapeNode.setLocalTranslation(jointCenter);
            shapeNode.setLocalRotation(parentBone.getWorldRotation());

            space.add(shapeNode);

            PhysicsBoneLink link = new PhysicsBoneLink();
            link.bone = bone;
            link.shapeNode = shapeNode;
            link.pivotA = new Vector3f(0,height,0);
            link.pivotB = new Vector3f(0,-height,0);
            boneLinks.put(bone, link);
        }

        // connect the bone shapes with joints
        for (PhysicsBoneLink link : boneLinks.values()){
            PhysicsBoneLink parentLink = boneLinks.get(link.bone.getParent());
            if (parentLink == null)
                continue;

            PhysicsConeJoint joint = new PhysicsConeJoint(link.shapeNode,
                                                                        parentLink.shapeNode,
                                                                        link.pivotA,
                                                                        link.pivotB);
            joint.setCollisionBetweenLinkedBodys(false);
            space.add(joint);
        }
    }

}
