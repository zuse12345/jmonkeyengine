package com.g3d.effect;

import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Format;
import com.g3d.scene.VertexBuffer.Usage;
import com.g3d.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ParticleTriMesh extends ParticleMesh {

    private int imagesX;
    private int imagesY;
    private boolean uniqueTexCoords = false;

    @Override
    public void initParticleData(int numParticles, int imagesX, int imagesY) {
        setMode(Mode.Triangles);
        setVertexCount(numParticles * 4);
        setTriangleCount(numParticles * 2);

        this.imagesX = imagesX;
        this.imagesY = imagesY;

        // set positions
        FloatBuffer pb = BufferUtils.createVector3Buffer(numParticles * 4);
        VertexBuffer pvb = new VertexBuffer(VertexBuffer.Type.Position);
        pvb.setupData(Usage.StreamWriteOnly, 3, Format.Float, pb);
        setBuffer(pvb);

        // set colors
        ByteBuffer cb = BufferUtils.createByteBuffer(numParticles * 4 * 4);
        VertexBuffer cvb = new VertexBuffer(VertexBuffer.Type.Color);
        cvb.setupData(Usage.StreamWriteOnly, 4, Format.UnsignedByte, cb);
        cvb.setNormalized(true);
        setBuffer(cvb);

        // set texcoords
        VertexBuffer tvb = new VertexBuffer(VertexBuffer.Type.TexCoord);
        FloatBuffer tb = BufferUtils.createVector2Buffer(numParticles * 4);
        if (imagesX == 1 && imagesY == 1){
            uniqueTexCoords = false;
            for (int i = 0; i < numParticles; i++){
                tb.put(0f).put(1f);
                tb.put(1f).put(1f);
                tb.put(0f).put(0f);
                tb.put(1f).put(0f);
            }
            tb.flip();
            tvb.setupData(Usage.Static, 2, Format.Float, tb);
        }else{
            uniqueTexCoords = true;
            tvb.setupData(Usage.StreamWriteOnly, 2, Format.Float, tb);
        }
        setBuffer(tvb);

        // set indices
        ShortBuffer ib = BufferUtils.createShortBuffer(numParticles * 6);
        for (int i = 0; i < numParticles; i++){
            int startIdx = (i * 4);

            // triangle 1
            ib.put((short)(startIdx + 1))
              .put((short)(startIdx + 0))
              .put((short)(startIdx + 2));

            // triangle 2
            ib.put((short)(startIdx + 1))
              .put((short)(startIdx + 2))
              .put((short)(startIdx + 3));
        }
        ib.flip();
        
        VertexBuffer ivb = new VertexBuffer(VertexBuffer.Type.Index);
        ivb.setupData(Usage.Static, 3, Format.UnsignedShort, ib);
        setBuffer(ivb);
    }

    @Override
    public void updateParticleData(Particle[] particles, Camera cam) {
        VertexBuffer pvb = getBuffer(VertexBuffer.Type.Position);
        FloatBuffer positions = (FloatBuffer) pvb.getData();

        VertexBuffer cvb = getBuffer(VertexBuffer.Type.Color);
        ByteBuffer colors = (ByteBuffer) cvb.getData();

        VertexBuffer tvb = getBuffer(VertexBuffer.Type.TexCoord);
        FloatBuffer texcoords = (FloatBuffer) tvb.getData();

        Vector3f up = cam.getUp();
        Vector3f left = cam.getLeft();

        // update data in vertex buffers
        positions.rewind();
        colors.rewind();
        texcoords.rewind();
        for (Particle p : particles){
            positions.put(p.position.x + left.x * p.size + up.x * p.size)
                     .put(p.position.y + left.y * p.size + up.y * p.size)
                     .put(p.position.z + left.z * p.size + up.z * p.size);

            positions.put(p.position.x - left.x * p.size + up.x * p.size)
                     .put(p.position.y - left.y * p.size + up.y * p.size)
                     .put(p.position.z - left.z * p.size + up.z * p.size);

            positions.put(p.position.x + left.x * p.size - up.x * p.size)
                     .put(p.position.y + left.y * p.size - up.y * p.size)
                     .put(p.position.z + left.z * p.size - up.z * p.size);
            
            positions.put(p.position.x - left.x * p.size - up.x * p.size)
                     .put(p.position.y - left.y * p.size - up.y * p.size)
                     .put(p.position.z - left.z * p.size - up.z * p.size);

            if (uniqueTexCoords){
                int imgX = p.imageIndex % imagesX;
                int imgY = (p.imageIndex - imgX) / imagesY;

                float startX = ((float) imgX) / imagesX;
                float startY = ((float) imgY) / imagesY;
                float endX   = startX + (1f / imagesX);
                float endY   = startY + (1f / imagesY);

                texcoords.put(startX).put(endY);
                texcoords.put(endX).put(endY);
                texcoords.put(startX).put(startY);
                texcoords.put(endX).put(startY);
            }

            int rgba = p.color.asIntRGBA();
            colors.order(ByteOrder.BIG_ENDIAN);
            colors.putInt(rgba);
            colors.putInt(rgba);
            colors.putInt(rgba);
            colors.putInt(rgba);
            colors.order(ByteOrder.nativeOrder());
        }
        positions.flip();
        colors.flip();
        if (!uniqueTexCoords)
            texcoords.clear();
        else{
            texcoords.flip();
            tvb.updateData(texcoords);
        }

        // force renderer to re-send data to GPU
        pvb.updateData(positions);
        cvb.updateData(colors);
    }

}
