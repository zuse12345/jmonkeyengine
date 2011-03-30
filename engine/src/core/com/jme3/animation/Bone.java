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
package com.jme3.animation;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <code>Bone</code> describes a bone in the bone-weight skeletal animation
 * system. A bone contains a name and an index, as well as relevant
 * transformation data.
 *
 * @author Kirill Vainer
 */
public final class Bone implements Savable {

    private String name;
    private Bone parent;
    private final ArrayList<Bone> children = new ArrayList<Bone>();
    /**
     * If enabled, user can control bone transform with setUserTransforms.
     * Animation transforms are not applied to this bone when enabled.
     */
    private boolean userControl = false;
    /**
     * The attachment node.
     */
    private Node attachNode;
    /**
     * Initial transform is the local bind transform of this bone.
     * PARENT SPACE -> BONE SPACE
     */
    private Vector3f initialPos;
    private Quaternion initialRot;
    private Vector3f initialScale = new Vector3f(1.0f, 1.0f, 1.0f);
    /**
     * The inverse world bind transform.
     * BONE SPACE -> MODEL SPACE
     */
    private Vector3f worldBindInversePos;
    private Quaternion worldBindInverseRot;
    private Vector3f worldBindInverseScale;
    /**
     * The local animated transform combined with the local bind transform and parent world transform
     */
    private Vector3f localPos = new Vector3f();
    private Quaternion localRot = new Quaternion();
    private Vector3f localScale = new Vector3f(1.0f, 1.0f, 1.0f);
    /**
     * MODEL SPACE -> BONE SPACE (in animated state)
     */
    private Vector3f worldPos = new Vector3f();
    private Quaternion worldRot = new Quaternion();
    private Vector3f worldScale = new Vector3f();
    
    /**
     * Creates a new bone with the given name.
     * 
     * @param name Name to give to this bone
     */
    public Bone(String name) {
        this.name = name;

        initialPos = new Vector3f();
        initialRot = new Quaternion();
        initialScale = new Vector3f();

        worldBindInversePos = new Vector3f();
        worldBindInverseRot = new Quaternion();
        worldBindInverseScale = new Vector3f();
    }

    /**
     * Copy constructor. local bind and world inverse bind transforms shallow copied.
     * @param source
     */
    Bone(Bone source) {
        this.name = source.name;

        userControl = source.userControl;

        initialPos = source.initialPos;
        initialRot = source.initialRot;
        initialScale = source.initialScale;

        worldBindInversePos = source.worldBindInversePos;
        worldBindInverseRot = source.worldBindInverseRot;
        worldBindInverseScale = source.worldBindInverseScale;

        // parent and children will be assigned manually..
    }

    /**
     * Used for binary loading as a Savable; the object must be constructed,
     * then the parameters usually present in the constructor for this class are
     * restored from the file the object was saved to.
     */
    public Bone() {
    }

    /**
     * @return The name of the bone, set in the constructor.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The parent bone of this bone, or null if it is a root bone.
     */
    public Bone getParent() {
        return parent;
    }

    /**
     * @return All the children bones of this bone.
     */
    public ArrayList<Bone> getChildren() {
        return children;
    }

    /**
     * @return The local position of the bone, relative to the parent bone.
     */
    public Vector3f getLocalPosition() {
        return localPos;
    }

    /**
     * @return The local rotation of the bone, relative to the parent bone.
     */
    public Quaternion getLocalRotation() {
        return localRot;
    }

    /**
     * @return The local scale of the bone, relative to the parent bone.
     */
    public Vector3f getLocalScale() {
        return localScale;
    }

    /**
     * @return The position of the bone in model space.
     */
    public Vector3f getModelSpacePosition() {
        return worldPos;
    }

    /**
     * @return The rotation of the bone in model space.
     */
    public Quaternion getModelSpaceRotation() {
        return worldRot;
    }

    /**
     * @return The scale of the bone in model space.
     */
    public Vector3f getModelSpaceScale() {
        return worldScale;
    }

    public Vector3f getWorldBindInversePosition() {
        return worldBindInversePos;
    }

    public Quaternion getWorldBindInverseRotation() {
        return worldBindInverseRot;
    }

    public Vector3f getWorldBindInverseScale() {
        return worldBindInverseScale;
    }

    /**
     * If enabled, user can control bone transform with setUserTransforms.
     * Animation transforms are not applied to this bone when enabled.
     */
    public void setUserControl(boolean enable) {
        userControl = enable;
    }

    /**
     * Add a new child to this bone. Shouldn't be used by user code.
     * Can corrupt skeleton.
     * @param bone
     */
    public void addChild(Bone bone) {
        children.add(bone);
        bone.parent = this;
    }

    /**
     * Updates the world transforms for this bone, and, possibly the attach node if not null.
     */
    final void updateWorldVectors() {
        if (parent != null) {

            //rotation
            parent.worldRot.mult(localRot, worldRot);

            //scale
            //For scale parent scale is not taken into account!
            // worldScale.set(localScale);
            parent.worldScale.mult(localScale, worldScale);

            //translation
            //scale and rotation of parent affect bone position            
            parent.worldRot.mult(localPos, worldPos);
            worldPos.multLocal(parent.worldScale);
            worldPos.addLocal(parent.worldPos);



        } else {
            worldRot.set(localRot);
            worldPos.set(localPos);
            worldScale.set(localScale);
        }

        if (attachNode != null) {
            attachNode.setLocalTranslation(worldPos);
            attachNode.setLocalRotation(worldRot);
            attachNode.setLocalScale(worldScale);
        }
    }

    /**
     * Updates world transforms for this bone and it's children.
     */
    final void update() {
        this.updateWorldVectors();

        for (int i = children.size() - 1; i >= 0; i--) {
            children.get(i).update();
        }
    }

    /**
     * Saves the current bone state as its binding pose, including its children.
     */
    void setBindingPose() {
        initialPos.set(localPos);
        initialRot.set(localRot);
        initialScale.set(localScale);

        if (worldBindInversePos == null) {
            worldBindInversePos = new Vector3f();
            worldBindInverseRot = new Quaternion();
            worldBindInverseScale = new Vector3f();
        }

        // Save inverse derived position/scale/orientation, used for calculate offset transform later
        worldBindInversePos.set(worldPos);
        worldBindInversePos.negateLocal();

        worldBindInverseRot.set(worldRot);
        worldBindInverseRot.inverseLocal();

        worldBindInverseScale.set(Vector3f.UNIT_XYZ);
        worldBindInverseScale.divideLocal(worldScale);

        for (Bone b : children) {
            b.setBindingPose();
        }
    }

    /**
     * Reset the bone and it's children to bind pose.
     */
    final void reset() {
        if (!userControl) {
            localPos.set(initialPos);
            localRot.set(initialRot);
            localScale.set(initialScale);
        }

        for (int i = children.size() - 1; i >= 0; i--) {
            children.get(i).reset();
        }
    }

    //Temp 3x3 rotation matrix for transform computation
    Matrix3f rotMat=new Matrix3f();
    /**
     * Stores the skinning transform in the specified Matrix4f.
     * The skinning transform applies the animation of the bone to a vertex.
     * @param m
     */
    void getOffsetTransform(Matrix4f m, Quaternion tmp1, Vector3f tmp2, Vector3f tmp3) {

        //Computing scale
        Vector3f scale = worldScale.mult(worldBindInverseScale, tmp3);

        //computing rotation
        Quaternion rotate = worldRot.mult(worldBindInverseRot, tmp1);

        //computing translation
        //translation depend on rotation and scale
        Vector3f translate = worldPos.add(rotate.mult(scale.mult(worldBindInversePos, tmp2), tmp2), tmp2);
        
        //populating the matrix
        m.loadIdentity();
        m.setTransform(translate, scale, rotate.toRotationMatrix(rotMat));
    }

    /**
     * Set user transform.
     * @see setUserControl
     */
    public void setUserTransforms(Vector3f translation, Quaternion rotation, Vector3f scale) {
        if (!userControl) {
            throw new IllegalStateException("User control must be on bone to allow user transforms");
        }

        localPos.set(initialPos);
        localRot.set(initialRot);
        localScale.set(initialScale);

        localPos.addLocal(translation);
        localRot = localRot.mult(rotation);
        localScale.multLocal(scale);
    }

    /**
     * Must update all bones in skeleton for this to work.
     * @param translation
     * @param rotation
     *///TODO: add scale here ???
    public void setUserTransformsWorld(Vector3f translation, Quaternion rotation) {
        if (!userControl) {
            throw new IllegalStateException("User control must be on bone to allow user transforms");
        }

        worldPos.set(translation);
        worldRot.set(rotation);
    }

    /**
     * Returns the attachment node.
     * Attach models and effects to this node to make
     * them follow this bone's motions.
     */
    public Node getAttachmentsNode() {
        if (attachNode == null) {
            attachNode = new Node(name + "_attachnode");
            attachNode.setUserData("AttachedBone", this);
        }
        return attachNode;
    }

    /**
     * Used internally after model cloning.
     * @param attachNode
     */
    public void setAttachmentsNode(Node attachNode) {
        this.attachNode = attachNode;
    }

    /**
     * Sets the local animation transform of this bone.
     * Bone is assumed to be in bind pose when this is called.
     */
    void setAnimTransforms(Vector3f translation, Quaternion rotation, Vector3f scale) {
        if (userControl) {
            return;
        }

//        localPos.addLocal(translation);
//        localRot.multLocal(rotation);
        //localRot = localRot.mult(rotation);

        localPos.set(initialPos).addLocal(translation);
        localRot.set(initialRot).multLocal(rotation);
       
        if (scale != null) {
            localScale.set(initialScale).multLocal(scale);
        }
    }

    void blendAnimTransforms(Vector3f translation, Quaternion rotation, Vector3f scale, float weight) {
        if (userControl) {
            return;
        }

        TempVars vars = TempVars.get();
        assert vars.lock();

        Vector3f tmpV = vars.vect1;
        Vector3f tmpV2 = vars.vect2;
        Quaternion tmpQ = vars.quat1;

        //location
        tmpV.set(initialPos).addLocal(translation);
        localPos.interpolate(tmpV, weight);

        //rotation
        tmpQ.set(initialRot).multLocal(rotation);
        localRot.slerp(tmpQ, weight);

        //scale
        if (scale != null) {
            tmpV2.set(initialScale).multLocal(scale);
            localScale.interpolate(tmpV2, weight);
        }


        assert vars.unlock();
    }

    /**
     * Sets local bind transform for bone.
     * Call setBindingPose() after all of the skeleton bones' bind transforms are set to save them.
     */
    public void setBindTransforms(Vector3f translation, Quaternion rotation, Vector3f scale) {
        initialPos.set(translation);
        initialRot.set(rotation);
        if (scale != null) {
            initialScale.set(scale);
        }

        localPos.set(translation);
        localRot.set(rotation);
        if (scale != null) {
            localScale.set(scale);
        }
    }

    void setAnimTransforms(Vector3f translation, Quaternion rotation) {
        this.setAnimTransforms(translation, rotation, Vector3f.UNIT_XYZ);
    }

    public void setBindTransforms(Vector3f translation, Quaternion rotation) {
        this.setBindTransforms(translation, rotation, Vector3f.UNIT_XYZ);
    }

    private String toString(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append('-');
        }

        sb.append(name).append(" bone\n");
        for (Bone child : children) {
            sb.append(child.toString(depth + 1));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.toString(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(JmeImporter im) throws IOException {
        InputCapsule input = im.getCapsule(this);

        name = input.readString("name", null);
        initialPos = (Vector3f) input.readSavable("initialPos", null);
        initialRot = (Quaternion) input.readSavable("initialRot", null);
        initialScale = (Vector3f) input.readSavable("initialScale", new Vector3f(1.0f, 1.0f, 1.0f));
        attachNode = (Node) input.readSavable("attachNode", null);

        localPos.set(initialPos);
        localRot.set(initialRot);

        ArrayList<Bone> childList = input.readSavableArrayList("children", null);
        for (int i = childList.size() - 1; i >= 0; i--) {
            this.addChild(childList.get(i));
        }

        // NOTE: Parent skeleton will call update() then setBindingPose()
        // after Skeleton has been de-serialized.
        // Therefore, worldBindInversePos and worldBindInverseRot
        // will be reconstructed based on that information.
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule output = ex.getCapsule(this);

        output.write(name, "name", null);
        output.write(attachNode, "attachNode", null);
        output.write(initialPos, "initialPos", null);
        output.write(initialRot, "initialRot", null);
        output.write(initialScale, "initialScale", new Vector3f(1.0f, 1.0f, 1.0f));
        output.writeSavableArrayList(children, "children", null);
    }

    public Vector3f getInitialPos() {
        return initialPos;
    }

    public Quaternion getInitialRot() {
        return initialRot;
    }
}
