package com.g3d.scene;

import com.g3d.bounding.BoundingBox;
import com.g3d.bounding.BoundingVolume;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.math.Triangle;
import com.g3d.scene.VertexBuffer.*;
import com.g3d.util.BufferUtils;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.EnumMap;

public class Mesh implements Savable {

    public enum Mode {
        Points,
        Lines,
        LineLoop,
        LineStrip,
        Triangles,
        TriangleStrip,
        TriangleFan,
    }

    /**
     * The bounding volume that contains the mesh entirely.
     * By default a BoundingBox (AABB).
     */
    private BoundingVolume meshBound =  new BoundingBox();

    private EnumMap<VertexBuffer.Type, VertexBuffer> buffers = new EnumMap<Type, VertexBuffer>(VertexBuffer.Type.class);

    private transient int vertexArrayID = -1;

    private int vertCount = -1;
    private int elementCount = -1;

    private Mode mode = Mode.Triangles;

    public Mesh(){
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Locks the mesh so it cannot be modified anymore, thus
     * optimizing its data.
     */
    public void setStatic() {
        for (VertexBuffer vb : buffers.values()){
            vb.setUsage(Usage.Static);
        }
    }

    public void setStreamed(){
        for (VertexBuffer vb : buffers.values()){
            vb.setUsage(Usage.DynamicWriteOnly);
        }
    }

    /**
     * Converts all single floating-point vertex buffers
     * into half percision floating-point. Reduces memory and bandwidth
     * when sending to GPU, but may cause percision issues with large values.
     */
    public void convertToHalf() {
        for (VertexBuffer vb : buffers.values()){
            if (vb.getFormat() == Format.Float)
                vb.convertToHalf();
        }
    }

    public void updateCounts(){
        VertexBuffer pb = getBuffer(Type.Position);
        VertexBuffer ib = getBuffer(Type.Index);
        if (pb != null){
            vertCount = pb.getData().capacity() / 3;
        }
        if (ib != null){
            switch (mode){
                case Triangles:
                    elementCount = ib.getData().capacity() / 3;
                case TriangleFan:
                case TriangleStrip:
                    elementCount = ib.getData().capacity() - 2;
                case Points:
                    elementCount = ib.getData().capacity();
                case Lines:
                    elementCount = ib.getData().capacity() / 2;
                case LineLoop:
                    elementCount = ib.getData().capacity();
                case LineStrip:
                    elementCount = ib.getData().capacity() - 1;
            }
            
        }
    }

    public int getTriangleCount(){
        return elementCount;
    }

    public int getVertexCount(){
        return vertCount;
    }

    public void setTriangleCount(int count){
        this.elementCount = count;
    }

    public void setVertexCount(int count){
        this.vertCount = count;
    }

    public void getTriangle(int index, Triangle store){
        VertexBuffer pb = getBuffer(Type.Position);
        VertexBuffer ib = getBuffer(Type.Index);

        if (pb.getFormat() == Format.Float){
            FloatBuffer fpb = (FloatBuffer) pb.getData();

            if (ib.getFormat() == Format.Short){
                // accepted format for buffers
                ShortBuffer sib = (ShortBuffer) ib.getData();

                // aquire triangle's vertex indices
                int vertIndex = index * 3;
                int vert1 = sib.get(vertIndex);
                int vert2 = sib.get(vertIndex+1);
                int vert3 = sib.get(vertIndex+2);

                BufferUtils.populateFromBuffer(store.get(0), fpb, vert1);
                BufferUtils.populateFromBuffer(store.get(1), fpb, vert2);
                BufferUtils.populateFromBuffer(store.get(2), fpb, vert3);
            }
        }
    }

    public void getTriangle(int index, int[] indices){
        VertexBuffer ib = getBuffer(Type.Index);
        if (ib.getFormat() == Format.Short){
            // accepted format for buffers
            ShortBuffer sib = (ShortBuffer) ib.getData();

            // aquire triangle's vertex indices
            int vertIndex = index * 3;
            indices[0] = sib.get(vertIndex);
            indices[1] = sib.get(vertIndex+1);
            indices[2] = sib.get(vertIndex+2);
        }
    }

    public int getId(){
        return vertexArrayID;
    }

    public void setId(int id){
        if (vertexArrayID != -1)
            throw new IllegalStateException("ID has already been set.");
        
        vertexArrayID = id;
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
        }else{
            vb.setupData(Usage.Dynamic, components, Format.Float, buf);
        }
        updateCounts();
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
            updateCounts();
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
            updateCounts();
        }
    }

    public void setBuffer(VertexBuffer vb){
        if (buffers.get(vb.getBufferType()) != null)
            throw new IllegalArgumentException("Buffer type already set: "+vb.getBufferType());

        buffers.put(vb.getBufferType(), vb);
    }

    public void setBuffer(Type type, int components, short[] buf){
        setBuffer(type, components, BufferUtils.createShortBuffer(buf));
    }

    public VertexBuffer getBuffer(Type type){
        return buffers.get(type);
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

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write(meshBound, "modelBound", null);
        out.write(vertCount, "vertCount", -1);
        out.write(elementCount, "elementCount", -1);
        out.write(mode, "mode", Mode.Triangles);
        out.write(buffers.size(), "numBuffers", 0);
        if (buffers.size() > 0){
            int i = 0;
            for (VertexBuffer vb : buffers.values()){
                out.write(vb, "buf"+i, null);
                i++;
            }
        }
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        meshBound = (BoundingVolume) in.readSavable("modelBound", null);
        vertCount = in.readInt("vertCount", -1);
        elementCount = in.readInt("elementCount", -1);
        mode = in.readEnum("mode", Mode.class, Mode.Triangles);
        int numBufs = in.readInt("numBuffers", -1);
        if (numBufs > 0){
            for (int i = 0; i < numBufs; i++){
                VertexBuffer vb = (VertexBuffer) in.readSavable("buf"+i, null);
                buffers.put(vb.getBufferType(), vb);
            }
        }
    }

}
