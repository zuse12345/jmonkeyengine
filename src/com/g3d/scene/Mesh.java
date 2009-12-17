package com.g3d.scene;

import com.g3d.bounding.BoundingBox;
import com.g3d.bounding.BoundingVolume;
import com.g3d.collision.Collidable;
import com.g3d.collision.CollisionResults;
import com.g3d.collision.bih.BIHTree;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.math.Matrix4f;
import com.g3d.math.Triangle;
import com.g3d.math.Vector3f;
import com.g3d.scene.VertexBuffer.*;
import com.g3d.util.BufferUtils;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

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

    private BIHTree collisionTree = null;

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
            vb.setUsage(Usage.Stream);
        }
    }

    public void setInterleaved(){
        ArrayList<VertexBuffer> vbs = new ArrayList<VertexBuffer>(buffers.values());
        // index buffer not included when interleaving
        vbs.remove(getBuffer(Type.Index));

        int stride = 0; // aka bytes per vertex
        for (VertexBuffer vb : vbs){
            if (vb.getFormat() != Format.Float){
                throw new UnsupportedOperationException("Cannot interleave vertex buffer.\n" +
                                                        "Contains not-float data.");
            }
            stride += vb.componentsLength;
            vb.getData().clear(); // reset position & limit (used later)
        }

        VertexBuffer allData = new VertexBuffer(Type.InterleavedData);
        ByteBuffer dataBuf = BufferUtils.createByteBuffer(stride * getVertexCount());
        allData.setupData(Usage.Static, -1, Format.Byte, dataBuf);
        setBuffer(allData);

        for (int vert = 0; vert < getVertexCount(); vert++){
            for (VertexBuffer vb : vbs){
                FloatBuffer fb = (FloatBuffer) vb.getData();
                for (int comp = 0; comp < vb.components; comp++){
                    dataBuf.putFloat(fb.get());
                }
            }
        }

        int offset = 0;
        for (VertexBuffer vb : vbs){
            vb.setOffset(offset);
            vb.setStride(stride);
            
            // discard old buffer
            vb.setupData(vb.usage, vb.components, vb.format, null);
            offset += vb.componentsLength;
        }
    }

    public void updateCounts(){
        if (getBuffer(Type.InterleavedData) != null)
            throw new IllegalStateException("Should update counts before interleave");

        VertexBuffer pb = getBuffer(Type.Position);
        VertexBuffer ib = getBuffer(Type.Index);
        if (pb != null){
            vertCount = pb.getData().capacity() / 3;
        }
        if (ib != null){
            switch (mode){
                case Triangles:
                    elementCount = ib.getData().capacity() / 3;
                    break;
                case TriangleFan:
                case TriangleStrip:
                    elementCount = ib.getData().capacity() - 2;
                    break;
                case Points:
                    elementCount = ib.getData().capacity();
                    break;
                case Lines:
                    elementCount = ib.getData().capacity() / 2;
                    break;
                case LineLoop:
                    elementCount = ib.getData().capacity();
                    break;
                case LineStrip:
                    elementCount = ib.getData().capacity() - 1;
                    break;
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

    public void getTriangle(int index, Vector3f v1, Vector3f v2, Vector3f v3){
        VertexBuffer pb = getBuffer(Type.Position);
        VertexBuffer ib = getBuffer(Type.Index);

        if (pb.getFormat() == Format.Float){
            FloatBuffer fpb = (FloatBuffer) pb.getData();

            if (ib.getFormat() == Format.UnsignedShort){
                // accepted format for buffers
                ShortBuffer sib = (ShortBuffer) ib.getData();

                // aquire triangle's vertex indices
                int vertIndex = index * 3;
                int vert1 = sib.get(vertIndex);
                int vert2 = sib.get(vertIndex+1);
                int vert3 = sib.get(vertIndex+2);

                BufferUtils.populateFromBuffer(v1, fpb, vert1);
                BufferUtils.populateFromBuffer(v2, fpb, vert2);
                BufferUtils.populateFromBuffer(v3, fpb, vert3);
            }
        }
    }
    
    public void getTriangle(int index, Triangle tri){
        getTriangle(index, tri.get1(), tri.get2(), tri.get3());
        tri.setIndex(index);
    }

    public void getTriangle(int index, int[] indices){
        VertexBuffer ib = getBuffer(Type.Index);
        if (ib.getFormat() == Format.UnsignedShort){
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

    public void createCollisionData(){
        collisionTree = new BIHTree(this);
        collisionTree.construct();
    }

    public int collideWith(Collidable other, 
                           Matrix4f worldMatrix,
                           BoundingVolume worldBound,
                           CollisionResults results){

        if (collisionTree == null){
            createCollisionData();
        }
        
        return collisionTree.collideWith(other, worldMatrix, worldBound, results);
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

    public FloatBuffer getFloatBuffer(Type type) {
        return (FloatBuffer) buffers.get(type).getData();
    }

    public IndexBuffer getIndexBuffer() {
        Buffer buf = buffers.get(Type.Index).getData();
        if (buf instanceof ByteBuffer) {
            return new IndexByteBuffer((ByteBuffer) buf);
        } else if (buf instanceof ShortBuffer) {
            return new IndexShortBuffer((ShortBuffer) buf);
        } else {
            return new IndexIntBuffer((IntBuffer) buf);
        }
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
        out.write(collisionTree, "collisionTree", null);

        // export bufs as list
        Collection<VertexBuffer> c = buffers.values();
        List<VertexBuffer> vbList = new ArrayList<VertexBuffer>(c);
        out.writeSavableList(vbList, "buffers", null);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        meshBound = (BoundingVolume) in.readSavable("modelBound", null);
        vertCount = in.readInt("vertCount", -1);
        elementCount = in.readInt("elementCount", -1);
        mode = in.readEnum("mode", Mode.class, Mode.Triangles);
        collisionTree = (BIHTree) in.readSavable("collisionTree", null);

        List<VertexBuffer> vbList = in.readSavableList("buffers", null);
        for (VertexBuffer vb : vbList){
            buffers.put(vb.getBufferType(), vb);
        }
    }

}
