/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package g3dtools.converters.model;

import com.g3d.bounding.BoundingBox;
import com.g3d.math.Transform;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Format;
import com.g3d.scene.VertexBuffer.Type;
import com.g3d.scene.VertexBuffer.Usage;
import com.g3d.util.BufferUtils;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class FloatToFixed {

    private static final float shortSize = Short.MAX_VALUE - Short.MIN_VALUE;
    private static final float shortOff  = (Short.MAX_VALUE + Short.MIN_VALUE) * 0.5f;

    private static final float byteSize = Byte.MAX_VALUE - Byte.MIN_VALUE;
    private static final float byteOff  = (Byte.MAX_VALUE + Byte.MIN_VALUE) * 0.5f;

    public static final void convertToFixed(Geometry geom){
        geom.updateModelBound();
        BoundingBox bbox = (BoundingBox) geom.getModelBound();
        Mesh mesh = geom.getMesh();

        VertexBuffer positions = mesh.getBuffer(Type.Position);
        VertexBuffer normals   = mesh.getBuffer(Type.Normal);
        VertexBuffer texcoords = mesh.getBuffer(Type.TexCoord);
        VertexBuffer indices   = mesh.getBuffer(Type.Index);

        // positions
        FloatBuffer fb = (FloatBuffer) positions.getData();
//        Buffer pb = BufferUtils.createShortBuffer(fb.capacity());
//        Format posFmt = Format.Short;
//        Buffer pb = BufferUtils.createByteBuffer(fb.capacity());
//        Format posFmt = Format.Byte;
//        Transform transform = convertPositions(fb, bbox, pb);
//
//        positions = new VertexBuffer(Type.Position);
//        positions.setupData(Usage.Static, 3, posFmt, pb);
//        mesh.clearBuffer(Type.Position);
//        mesh.setBuffer(positions);
//
//        geom.setTransform(transform);

        // normals
        fb = (FloatBuffer) normals.getData();
        ByteBuffer bb = BufferUtils.createByteBuffer(fb.capacity());
        convertNormals(fb, bb);

        normals = new VertexBuffer(Type.Normal);
        normals.setupData(Usage.Static, 3, Format.Byte, bb);
        normals.setNormalized(true);
        mesh.clearBuffer(Type.Normal);
        mesh.setBuffer(normals);

        // texcoords
//        fb = (FloatBuffer) texcoords.getData();
//        IntBuffer ib = BufferUtils.createIntBuffer(fb.capacity());
//        convertTexCoords2D(fb, ib);
//
//        texcoords = new VertexBuffer(Type.TexCoord);
//        texcoords.setupData(Usage.Static, 2, Format.Int, sb);
//        mesh.clearBuffer(Type.TexCoord);
//        mesh.setBuffer(texcoords);

        // indices
        if (mesh.getVertexCount() <= 255){
            ShortBuffer sb = (ShortBuffer) indices.getData();
            bb = BufferUtils.createByteBuffer(sb.capacity());
            convertIndices(sb, bb);

            indices = new VertexBuffer(Type.Index);
            indices.setupData(Usage.Static, 3, Format.UnsignedByte, bb);
            mesh.clearBuffer(Type.Index);
            mesh.setBuffer(indices);
        }
    }

    public static final void convertIndices(ShortBuffer input, ByteBuffer output){
        input.clear();
        output.clear();
        int triangleCount = input.capacity() / 3;
        for (int i = 0; i < triangleCount; i++){
            output.put((byte) input.get());
            output.put((byte) input.get());
            output.put((byte) input.get());
        }
    }

    public static final void convertNormals(FloatBuffer input, ByteBuffer output){
        if (output.capacity() < input.capacity())
            throw new RuntimeException("Output must be at least as large as input!");

        input.clear();
        output.clear();
        Vector3f temp = new Vector3f();
        int vertexCount = input.capacity() / 3;
        for (int i = 0; i < vertexCount; i++){
            BufferUtils.populateFromBuffer(temp, input, i);

            // offset and scale vector into -128 ... 127
            temp.multLocal(127).addLocal(0.5f, 0.5f, 0.5f);

            // quantize
            byte v1 = (byte) temp.getX();
            byte v2 = (byte) temp.getY();
            byte v3 = (byte) temp.getZ();

            // store
            output.put(v1).put(v2).put(v3);
        }
    }

    public static final void convertTexCoords2D(FloatBuffer input, IntBuffer output){
        if (output.capacity() < input.capacity())
            throw new RuntimeException("Output must be at least as large as input!");

        input.clear();
        output.clear();
        Vector2f temp = new Vector2f();
        int vertexCount = input.capacity() / 2;
        for (int i = 0; i < vertexCount; i++){
            BufferUtils.populateFromBuffer(temp, input, i);

            int v1 = (int) (temp.getX() * (1 << 16));
            int v2 = (int) (temp.getY() * (1 << 16));
            
            output.put(v1).put(v2);
        }
    }

    public static final Transform convertPositions(FloatBuffer input, BoundingBox bbox, Buffer output){
        if (output.capacity() < input.capacity())
            throw new RuntimeException("Output must be at least as large as input!");

        Vector3f offset = bbox.getCenter().negate();
        Vector3f size = new Vector3f(bbox.getXExtent(), bbox.getYExtent(), bbox.getZExtent());
        size.multLocal(2);

        ShortBuffer sb = null;
        ByteBuffer bb = null;
        float dataTypeSize;
        float dataTypeOffset;
        if (output instanceof ShortBuffer){
            sb = (ShortBuffer) output;
            dataTypeOffset = shortOff;
            dataTypeSize = shortSize;
        }else{
            bb = (ByteBuffer) output;
            dataTypeOffset = byteOff;
            dataTypeSize = byteSize;
        }
        Vector3f scale = new Vector3f();
        scale.set(dataTypeSize, dataTypeSize, dataTypeSize).divideLocal(size);

        Vector3f invScale = new Vector3f();
        invScale.set(size).divideLocal(dataTypeSize);

        offset.multLocal(scale);
        offset.addLocal(dataTypeOffset, dataTypeOffset, dataTypeOffset);

        // offset = (-modelOffset * shortSize)/modelSize + shortOff
        // scale = shortSize / modelSize

        input.clear();
        output.clear();
        Vector3f temp = new Vector3f();
        int vertexCount = input.capacity() / 3;
        for (int i = 0; i < vertexCount; i++){
            BufferUtils.populateFromBuffer(temp, input, i);

            // offset and scale vector into -32768 ... 32767
            // or into -128 ... 127 if using bytes
            temp.multLocal(scale);
            temp.addLocal(offset);

            // quantize and store
            if (sb != null){
                short v1 = (short) temp.getX();
                short v2 = (short) temp.getY();
                short v3 = (short) temp.getZ();
                sb.put(v1).put(v2).put(v3);
            }else{
                byte v1 = (byte) temp.getX();
                byte v2 = (byte) temp.getY();
                byte v3 = (byte) temp.getZ();
                bb.put(v1).put(v2).put(v3);
            }
        }

        Transform transform = new Transform();
        transform.setTranslation(offset.negate().multLocal(invScale));
        transform.setScale(invScale);
        return transform;
    }

}
