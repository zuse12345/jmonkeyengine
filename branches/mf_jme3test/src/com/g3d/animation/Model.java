package com.g3d.animation;

import com.g3d.math.Matrix4f;
import com.g3d.math.Vector3f;
import com.g3d.scene.Mesh;
import com.g3d.scene.Node;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Type;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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
    private Map<String, BoneAnimation> animationMap;

    /**
     * The currently playing animation.
     */
    private BoneAnimation animation;
    private float time = 0f;

     public Model(String name,
                  Mesh[] meshes,
                  Skeleton skeleton,
                  Map<String, BoneAnimation> anims) {
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
        Vector3f vt = new Vector3f();
        Vector3f nm = new Vector3f();
        Vector3f resultVert = new Vector3f();
        Vector3f resultNorm = new Vector3f();

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
        ByteBuffer wb = (ByteBuffer) mesh.getBuffer(Type.BoneWeight).getData();
        int maxWeightsPerVert = 4;//mesh.getWeightBuffer().maxWeightsPerVert;
        int fourMinusMaxWeights = 4 - maxWeightsPerVert;
        ib.rewind();
        wb.rewind();

        Vector3f tmp = new Vector3f();

        // iterate vertices and apply skinning transform for each effecting bone
        for (int vert = 0; vert < mesh.getVertexCount(); vert++){
            vt.x = fvb.get();
            vt.y = fvb.get();
            vt.z = fvb.get();
            nm.x = fnb.get();
            nm.y = fnb.get();
            nm.z = fnb.get();
            resultVert.x = resultVert.y = resultVert.z = 0;
            resultNorm.x = resultNorm.y = resultNorm.z = 0;

            for (int w = 0; w < maxWeightsPerVert; w++){
                float weight = wb.get();
                Matrix4f mat = offsetMatrices[ib.get()];

                mat.mult(vt, tmp).multLocal(weight);
                resultVert.addLocal(tmp);

                mat.multNormal(nm, tmp).multLocal(weight);
                resultNorm.addLocal(tmp);
//                resultVert.x += (mat.m00 * vt.x + mat.m01 * vt.y + mat.m02 * vt.z + mat.m03) * weight;
//                resultVert.y += (mat.m10 * vt.x + mat.m11 * vt.y + mat.m12 * vt.z + mat.m13) * weight;
//                resultVert.z += (mat.m20 * vt.x + mat.m21 * vt.y + mat.m22 * vt.z + mat.m23) * weight;
//
//                resultNorm.x += (nm.x * mat.m00 + nm.y * mat.m01 + nm.z * mat.m02) * weight;
//                resultNorm.y += (nm.x * mat.m10 + nm.y * mat.m11 + nm.z * mat.m12) * weight;
//                resultNorm.z += (nm.x * mat.m20 + nm.y * mat.m21 + nm.z * mat.m22) * weight;
            }

            ib.position(ib.position()+fourMinusMaxWeights);
            wb.position(wb.position()+fourMinusMaxWeights);

            // overwrite vertex with transformed pos
            fvb.position(fvb.position()-3);
            fvb.put(resultVert.x).put(resultVert.y).put(resultVert.z);

            fnb.position(fnb.position()-3);
            fnb.put(resultNorm.x).put(resultNorm.y).put(resultNorm.z);
        }

        fvb.flip();
        fnb.flip();

        vb.updateData(fvb);
        nb.updateData(fnb);

        mesh.updateBound();
    }

    public void updateLogicalState(float tpf) {
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

        time += tpf;
    }
}
