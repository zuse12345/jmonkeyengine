package com.jme3.animation;

import com.jme3.export.G3DExporter;
import com.jme3.export.G3DImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Model extends Node {

    /**
     * List of targets which this controller effects.
     */
    private Mesh[] targets;

    /**
     * Skeleton object must contain corresponding data for the targets' weight buffers.
     */
    private Skeleton skeleton;

    /**
     * List of animations, bone or vertex based.
     */
    private HashMap<String, BoneAnimation> animationMap;

    /**
     * The currently playing animation.
     */
    private BoneAnimation animation;
    private float time = 0f;
    private float speed = 1f;

     public Model(String name,
                  Mesh[] meshes,
                  Skeleton skeleton,
                  HashMap<String, BoneAnimation> anims) {
        super(name);
        this.skeleton = skeleton;
        this.animationMap = anims;
        this.targets = meshes;
        reset();
    }

    /**
     * Copy constructor. The mesh data has to be unique, and copied through OgreMesh.cloneFromMesh.
     * The rest is handled automatically by this call.
     */
    public Model(Mesh[] meshes, Model sourceControl){
        this.skeleton = new Skeleton(sourceControl.skeleton);
        this.animationMap = sourceControl.animationMap;
        this.targets = meshes;
        reset();
    }

    public Model clone(){
        Model clone = (Model) super.clone();
        clone.skeleton = new Skeleton(skeleton);
        Mesh[] meshes = new Mesh[targets.length];
        for (int i = 0; i < meshes.length; i++){
            meshes[i] = ((Geometry) clone.getChild(i)).getMesh();
        }
        clone.targets = meshes;
        return clone;
    }

    /**
     * Used only for Saving/Loading models (all parameters of the non-default
     * constructor are restored from the saved model, but the object must be
     * constructed beforehand)
     */
    public Model() {
    }

    /**
     * Sets the currently active animation.
     * Use the animation name "<bind>" to set the model into bind pose.
     *
     * @returns true if the animation has been successfuly set. False if no such animation exists.
     */
    public boolean setAnimation(String name){
        if (animation != null && animation.getName().equals(name))
            return true;

        if (name.equals("<bind>")){
            reset();
            return true;
        }

        animation = animationMap.get(name);

        if (animation == null)
            return false;

        resetToBind();
        time = 0;

        return true;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    

    public Collection<String> getAnimationNames(){
        return animationMap.keySet();
    }

    public float getAnimationLength(String name){
        BoneAnimation a = animationMap.get(name);
        if (a == null)
            return -1;

        return a.getLength();
    }

    public String getCurrentAnimation(){
        return animation.getName();
    }


    void reset(){
        resetToBind();
        if (skeleton != null){
            skeleton.resetAndUpdate();
        }
        animation = null;
        time = 0;
    }
    
    void resetToBind(){
        for (int i = 0; i < targets.length; i++){
            if (targets[i].getBuffer(Type.BindPosePosition) != null){
                VertexBuffer bindPos = targets[i].getBuffer(Type.BindPosePosition);
                VertexBuffer bindNorm = targets[i].getBuffer(Type.BindPoseNormal);
                VertexBuffer pos = targets[i].getBuffer(Type.Position);
                VertexBuffer norm = targets[i].getBuffer(Type.Normal);
                FloatBuffer pb = (FloatBuffer) pos.getData();
                FloatBuffer nb = (FloatBuffer) norm.getData();
                FloatBuffer bpb = (FloatBuffer) bindPos.getData();
                FloatBuffer bnb = (FloatBuffer) bindNorm.getData();
                pb.clear();
                nb.clear();
                bpb.clear();
                bnb.clear();
                pb.put(bpb).clear();
                nb.put(bnb).clear();
            }
        }
    }

    public float clampWrapTime(float t, float max, LoopMode loopMode){
        if (t < 0f){
            switch (loopMode){
                case DontLoop:
                    return 0;
                case Cycle:
                    return 0;
                case Loop:
                    return max - t;
            }
        }else if (t > max){
            switch (loopMode){
                case DontLoop:
                    return max;
                case Cycle:
                    return max;
                case Loop:
                    return t - max;
            }
        }

        return t;
    }

    private void softwareSkinUpdate(Mesh mesh, Matrix4f[] offsetMatrices){
        int maxWeightsPerVert = mesh.getMaxNumWeights();
        int fourMinusMaxWeights = 4 - maxWeightsPerVert;

        // NOTE: This code assumes the vertex buffer is in bind pose
        // resetToBind() has been called this frame
        VertexBuffer vb = mesh.getBuffer(Type.Position);
        FloatBuffer fvb = (FloatBuffer) vb.getData();
        fvb.rewind();

        VertexBuffer nb = mesh.getBuffer(Type.Normal);
        FloatBuffer fnb = (FloatBuffer) nb.getData();
        fnb.rewind();

        // get boneIndexes and weights for mesh
        ByteBuffer ib = (ByteBuffer) mesh.getBuffer(Type.BoneIndex).getData();
        FloatBuffer wb = (FloatBuffer) mesh.getBuffer(Type.BoneWeight).getData();
        
        ib.rewind();
        wb.rewind();

        float[] weights = wb.array();
        byte[] indices = ib.array();
        int idxWeights = 0;

        TempVars vars = TempVars.get();
        float[] posBuf = vars.skinPositions;
        float[] normBuf = vars.skinNormals;

        int iterations = (int) FastMath.ceil(fvb.capacity() / ((float)posBuf.length));
        int bufLength = posBuf.length * 3;
        for (int i = iterations-1; i >= 0; i--){
            // read next set of positions and normals from native buffer
            bufLength = Math.min(posBuf.length, fvb.remaining());
            fvb.get(posBuf, 0, bufLength);
            fnb.get(normBuf, 0, bufLength);
            int verts = bufLength / 3;
            int idxPositions = 0;

            // iterate vertices and apply skinning transform for each effecting bone
            for (int vert = verts - 1; vert >= 0; vert--){
                float nmx = normBuf[idxPositions];
                float vtx = posBuf[idxPositions++];
                float nmy = normBuf[idxPositions];
                float vty = posBuf[idxPositions++];
                float nmz = normBuf[idxPositions];
                float vtz = posBuf[idxPositions++];

                float rx=0, ry=0, rz=0, rnx=0, rny=0, rnz=0;

    //            float vtx = fvb.get();
    //            float vty = fvb.get();
    //            float vtz = fvb.get();
    //            float nmx = fnb.get();
    //            float nmy = fnb.get();
    //            float nmz = fnb.get();
    //            float rx=0, ry=0, rz=0, rnx=0, rny=0, rnz=0;

                for (int w = maxWeightsPerVert - 1; w >= 0; w--){
                    float weight = weights[idxWeights];//wb.get();
                    Matrix4f mat = offsetMatrices[indices[idxWeights++]];//offsetMatrices[ib.get()];

                    rx += (mat.m00 * vtx + mat.m01 * vty + mat.m02 * vtz + mat.m03) * weight;
                    ry += (mat.m10 * vtx + mat.m11 * vty + mat.m12 * vtz + mat.m13) * weight;
                    rz += (mat.m20 * vtx + mat.m21 * vty + mat.m22 * vtz + mat.m23) * weight;

                    rnx += (nmx * mat.m00 + nmy * mat.m01 + nmz * mat.m02) * weight;
                    rny += (nmx * mat.m10 + nmy * mat.m11 + nmz * mat.m12) * weight;
                    rnz += (nmx * mat.m20 + nmy * mat.m21 + nmz * mat.m22) * weight;
                }

                idxWeights += fourMinusMaxWeights;
    //            ib.position(ib.position()+fourMinusMaxWeights);
    //            wb.position(wb.position()+fourMinusMaxWeights);

                idxPositions -= 3;
                normBuf[idxPositions] = rnx;
                posBuf[idxPositions++] = rx;
                normBuf[idxPositions] = rny;
                posBuf[idxPositions++] = ry;
                normBuf[idxPositions] = rnz;
                posBuf[idxPositions++] = rz;

                // overwrite vertex with transformed pos
    //            fvb.position(fvb.position()-3);
    //            fvb.put(rx).put(ry).put(rz);
    //
    //            fnb.position(fnb.position()-3);
    //            fnb.put(rnx).put(rny).put(rnz);
            }

        
            fvb.position(fvb.position()-bufLength);
            fvb.put(posBuf, 0, bufLength);
            fnb.position(fnb.position()-bufLength);
            fnb.put(normBuf, 0, bufLength);
        }

        vb.updateData(fvb);
        nb.updateData(fnb);

//        mesh.updateBound();
    }

    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);
        if (animation == null)
            return;

        // do clamping/wrapping of time
        time = clampWrapTime(time, animation.getLength(), LoopMode.Loop);
        resetToBind(); // reset morph meshes to bind pose
        skeleton.reset(); // reset skeleton to bind pose
        animation.setTime(time, skeleton, 1f);
        skeleton.updateWorldVectors();
        // here update the targets verticles if no hardware skinning supported

        Matrix4f[] offsetMatrices = skeleton.computeSkinningMatrices();

        // if hardware skinning is supported, the matrices and weight buffer
        // will be sent by the SkinningShaderLogic object assigned to the shader
        for (int i = 0; i < targets.length; i++){
            softwareSkinUpdate(targets[i], offsetMatrices);
        }

        time += tpf * speed;
    }

    @Override
    public void read(G3DImporter im) throws IOException{
        super.read(im);

        InputCapsule in = im.getCapsule(this);
        Savable[] sav = in.readSavableArray("targets", null);
        if (sav != null){
            targets = new Mesh[sav.length];
            System.arraycopy(sav, 0, targets, 0, sav.length);
        }
        skeleton = (Skeleton) in.readSavable("skeleton", null);
        animationMap = (HashMap<String, BoneAnimation>) in.readStringSavableMap("animations", null);
    }

    @Override
    public void write(G3DExporter ex) throws IOException{
        super.write(ex);

        OutputCapsule out = ex.getCapsule(this);
        out.write(targets, "targets", null);
        out.write(skeleton, "skeleton", null);
        out.writeStringSavableMap(animationMap, "animations", null);
    }
}
