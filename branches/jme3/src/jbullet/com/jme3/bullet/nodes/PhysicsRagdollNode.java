package com.jme3.bullet.nodes;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.joints.PhysicsPoint2PointJoint;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.ControlType;
import java.util.HashMap;
import java.util.Map;

public class PhysicsRagdollNode extends PhysicsNode {

    private static class PhysicsBoneLink {
        Bone bone;
        PhysicsJoint joint;
        PhysicsNode shapeNode;
    }

    private Map<Bone, PhysicsBoneLink> boneLinks = new HashMap<Bone, PhysicsBoneLink>();
    private Skeleton skeleton;

    public PhysicsRagdollNode(Spatial model){
        AnimControl animControl = (AnimControl) model.getControl(ControlType.BoneAnimation);
        skeleton = animControl.getSkeleton();

        // put into bind pose and compute bone transforms in model space
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
            shapeNode.setLocalTranslation(jointCenter);
            shapeNode.setLocalRotation(parentBone.getWorldRotation());
            attachChild(shapeNode);

            PhysicsBoneLink link = new PhysicsBoneLink();
            link.bone = bone;
            link.shapeNode = shapeNode;
            boneLinks.put(bone, link);
        }

        // connect the bone shapes with joints
        for (PhysicsBoneLink link : boneLinks.values()){
            PhysicsBoneLink parentLink = boneLinks.get(link.bone.getParent());
            if (parentLink == null)
                continue;

            PhysicsPoint2PointJoint joint = new PhysicsPoint2PointJoint(link.shapeNode,
                                                                        parentLink.shapeNode,
                                                                        link.bone.getWorldPosition(),
                                                                        parentLink.bone.getWorldPosition());
            joint.setCollisionBetweenLinkedBodys(false);
            PhysicsSpace.getPhysicsSpace().add(joint);
        }
    }

    @Override
    public void updateLogicalState(float tpf){
        for (PhysicsBoneLink link : boneLinks.values()){
            link.bone.setUserTransformsWorld(link.shapeNode.getWorldTranslation(),
                                             link.shapeNode.getWorldRotation());
        }
    }

}
