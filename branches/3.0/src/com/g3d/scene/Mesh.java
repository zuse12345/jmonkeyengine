package com.g3d.scene;

import com.g3d.bounding.BoundingBox;
import com.g3d.bounding.BoundingSphere;
import com.g3d.bounding.BoundingVolume;
import com.g3d.math.Triangle;
import com.g3d.math.Vector3f;
import com.g3d.scene.VertexBuffer.*;
import com.g3d.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;

public class Mesh {

    /**
     * The bounding volume that contains the mesh entirely.
     * By default a BoundingBox (AABB).
     */
    private BoundingVolume meshBound =  new BoundingBox();

    private EnumMap<VertexBuffer.Type, VertexBuffer> buffers = new EnumMap<Type, VertexBuffer>(VertexBuffer.Type.class);

    public Mesh(){
    }

    /**
     * Locks the mesh so it cannot be modified anymore, thus
     * optimizing its data.
     */
    public void lockStatic() {
        for (VertexBuffer vb : buffers.values()){
            vb.setUsage(Usage.Static);
        }
    }

    public void setBuffer(Type type, int components, FloatBuffer buf) {
        VertexBuffer vb = buffers.get(type);
        if (vb == null){
            if (buf == null)
                return;

            vb = new VertexBuffer(type);
            vb.setupData(Usage.Dynamic, components, Format.Float, buf);
            buffers.put(type, vb);
        }else if (buf == null){
            buffers.remove(type);
        }
    }

    public void setBuffer(Type type, int components, float[] buf){
        setBuffer(type, components, BufferUtils.createFloatBuffer(buf));
    }

    public void setBuffer(Type type, int components, IntBuffer buf) {
        VertexBuffer vb = buffers.get(type);
        if (vb == null){
            vb = new VertexBuffer(type);
            vb.setupData(Usage.Dynamic, components, Format.UnsignedInt, buf);
            buffers.put(type, vb);
        }
    }

    public void setBuffer(Type type, int components, int[] buf){
        setBuffer(type, components, BufferUtils.createIntBuffer(buf));
    }

    public void setBuffer(Type type, int components, ShortBuffer buf) {
        VertexBuffer vb = buffers.get(type);
        if (vb == null){
            vb = new VertexBuffer(type);
            vb.setupData(Usage.Dynamic, components, Format.UnsignedShort, buf);
            buffers.put(type, vb);
        }
    }

    public void setBuffer(Type type, int components, short[] buf){
        setBuffer(type, components, BufferUtils.createShortBuffer(buf));
    }

    public void updateBound(){
        VertexBuffer posBuf = buffers.get(VertexBuffer.Type.Position);
        if (meshBound != null && posBuf != null){
            meshBound.computeFromPoints((FloatBuffer)posBuf.getData());
        }
    }

    public BoundingVolume getBound() {
        return meshBound;
    }

    public void setBound(BoundingVolume modelBound) {
        meshBound = modelBound;
    }

    public Collection<VertexBuffer> getBuffers(){
        return buffers.values();
    }

}
