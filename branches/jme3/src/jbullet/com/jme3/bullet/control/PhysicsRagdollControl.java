package com.jme3.bullet.control;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.joints.Physics6DofJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.control.ControlType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3test.bullet.TestPhysicsRagdoll;

public class PhysicsRagdollControl implements Control {
    private static final Logger logger = Logger.getLogger(PhysicsRagdollControl.class
        .getName());

    private List<PhysicsBoneLink> boneLinks;
    private Skeleton skeleton;
    private PhysicsSpace space;
    //TODO:remove after testing
    private TestPhysicsRagdoll root;

    public PhysicsRagdollControl(TestPhysicsRagdoll root, PhysicsSpace space){
        this.space=space;
        this.root=root;
    }

    private List<PhysicsBoneLink> boneRecursion(Bone bone, PhysicsNode parent, List<PhysicsBoneLink> list){
        ArrayList<Bone> children = bone.getChildren();
        bone.setUserControl(true);
        for (Iterator<Bone> it = children.iterator(); it.hasNext();) {
            Bone childBone = it.next();

            Bone parentBone = bone;//childBone.getParent();
            Vector3f parentPos = parentBone.getWorldPosition();
            Vector3f childPos = childBone.getWorldPosition();
//            Vector3f jointCenter = parentPos.add(childPos.subtract(parentPos).multLocal(0.5f));
            Vector3f jointCenter = parentPos.add(childPos).multLocal(0.5f);
            Quaternion jointRotation=new Quaternion();
            jointRotation.lookAt(childPos.subtract(parentPos), Vector3f.UNIT_Y);

            // length of the joint
            float height = parentPos.distance(childPos);

            // radius 1 ? -> problems if lower than .4f..
//            CapsuleCollisionShape shape = new CapsuleCollisionShape(.2f, height*0.8f,2);
            BoxCollisionShape shape=new BoxCollisionShape(new Vector3f(.2f,.2f,.2f));//height*.4f));
//            SphereCollisionShape shape=new SphereCollisionShape(.2f);

            PhysicsNode shapeNode=null;
            //TODO: remove cylinder when testing is over
            shapeNode= new PhysicsNode(root.createCylinder(.2f, height), shape, 10);
            shapeNode.setLocalTranslation(jointCenter);
            shapeNode.setLocalRotation(jointRotation);//parentBone.getWorldRotation());

            //TODO: remove when testing is over, the shapeNode may not be in display tree (see update() method)
            root.getRootNode().attachChild(shapeNode);
            space.add(shapeNode);

            // mass 1 ?
            if(parent!=null){
                PhysicsBoneLink link = new PhysicsBoneLink();
                link.parentBone = parentBone;
                link.childBone = childBone;
                link.shapeNode = shapeNode;
                link.length=height;
                //local position from parent
                link.pivotA = new Vector3f(0,0,(height*.5f));
                //local position from child
                link.pivotB = new Vector3f(0,0,-(height*.5f));
                list.add(link);

//                PhysicsConeJoint joint = new PhysicsConeJoint(parent,shapeNode,
//                                                                            link.pivotA,
//                                                                            link.pivotB);
//                joint.setLimit(FastMath.QUARTER_PI, FastMath.QUARTER_PI, FastMath.QUARTER_PI);

                Physics6DofJoint joint = new Physics6DofJoint(parent,shapeNode,
                                                                            link.pivotA,
                                                                            link.pivotB,true);
                joint.getRotationalLimitMotor(0).setHiLimit(0);
                joint.getRotationalLimitMotor(0).setLoLimit(0);

                joint.setCollisionBetweenLinkedBodys(false);
                space.addQueued(joint);
            }
            boneRecursion(childBone,shapeNode,list);
        }
        return list;
    }

    public void update(float tpf) {
        for (PhysicsBoneLink link : boneLinks){
            //Looks bad updating here but the shapeNode is not in a display tree
            //maybe introduce new class or handle inside this class w/o PhysicsNodes
//            link.shapeNode.updateGeometricState();

            //TODO: bone location is not shapeNode location but add 0.5*dist in child direction
//            link.parentBone.setUserTransforms(link.shapeNode.getWorldTranslation(),
//                                             link.shapeNode.getWorldRotation(), new Vector3f(1,1,1));

            link.parentBone.setUserTransformsWorld(link.shapeNode.getWorldTranslation(),
                                             link.shapeNode.getWorldRotation());
        }
        skeleton.updateWorldVectors();
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ControlType getType() {
        return ControlType.BoneControl;
    }

    public void setSpatial(Spatial model) {
        //TODO: move to setSpatial()
        AnimControl animControl = (AnimControl) model.getControl(ControlType.BoneAnimation);
        skeleton = animControl.getSkeleton();

        // put into bind pose and compute bone transforms in model space
        // maybe dont reset to ragdoll out of animations?
        skeleton.updateWorldVectors();
//        skeleton.resetAndUpdate();

        logger.log(Level.INFO,"Create physics ragdoll for skeleton {0}",skeleton);
        List<PhysicsBoneLink> list=new LinkedList<PhysicsBoneLink>();
        for (int i = 0; i < skeleton.getBoneCount(); i++){
            Bone childBone = skeleton.getBone(i);
            childBone.setUserControl(true);
            if (childBone.getParent() == null){
                Vector3f parentPos = childBone.getWorldPosition();
                logger.log(Level.INFO,"Found root bone in skeleton {0}",skeleton);
                PhysicsNode shapeNode = new PhysicsNode(root.createCylinder(.5f, 1f), new SphereCollisionShape(.5f), 1);
                shapeNode.setLocalTranslation(parentPos);
                space.add(shapeNode);
                boneLinks=boneRecursion(childBone,shapeNode,list);
            }

        }
    }

    public void setEnabled(boolean enabled) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEnabled() {
        return true;
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
        Bone childBone;
        Bone parentBone;
        PhysicsJoint joint;
        PhysicsNode shapeNode;
        Vector3f pivotA;
        Vector3f pivotB;
        float length;
    }

}
