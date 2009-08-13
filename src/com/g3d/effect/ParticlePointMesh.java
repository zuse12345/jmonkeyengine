package com.g3d.effect;

import com.g3d.renderer.Camera;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Format;
import com.g3d.scene.VertexBuffer.Usage;
import com.g3d.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ParticlePointMesh extends ParticleMesh {

    @Override
    public void initParticleData(int numParticles, int imagesX, int imagesY) {
        setMode(Mode.Points);
        setVertexCount(numParticles);
        setTriangleCount(numParticles);

        // set positions
        FloatBuffer pb = BufferUtils.createVector3Buffer(numParticles);
        VertexBuffer pvb = new VertexBuffer(VertexBuffer.Type.Position);
        pvb.setupData(Usage.StreamWriteOnly, 3, Format.Float, pb);
        setBuffer(pvb);

        // set colors
        ByteBuffer cb = BufferUtils.createByteBuffer(numParticles * 4);
        VertexBuffer cvb = new VertexBuffer(VertexBuffer.Type.Color);
        cvb.setupData(Usage.StreamWriteOnly, 4, Format.UnsignedByte, cb);
        cvb.setNormalized(true);
        setBuffer(cvb);

        // set sizes
        ByteBuffer sb = BufferUtils.createByteBuffer(numParticles);
        VertexBuffer svb = new VertexBuffer(VertexBuffer.Type.Size);
        svb.setupData(Usage.StreamWriteOnly, 1, Format.UnsignedByte, sb);
        svb.setNormalized(true);
        setBuffer(svb);

        // set indices
        ShortBuffer ib = BufferUtils.createShortBuffer(numParticles);
        for (int i = 0; i < numParticles; i++){
            ib.put((short) i);
        }
        ib.flip();

        VertexBuffer ivb = new VertexBuffer(VertexBuffer.Type.Index);
        ivb.setupData(Usage.Static, 1, Format.UnsignedShort, ib);
        setBuffer(ivb);
    }

    @Override
    public void updateParticleData(Particle[] particles, Camera cam) {
        VertexBuffer pvb = getBuffer(VertexBuffer.Type.Position);
        FloatBuffer positions = (FloatBuffer) pvb.getData();

        VertexBuffer cvb = getBuffer(VertexBuffer.Type.Color);
        ByteBuffer colors = (ByteBuffer) cvb.getData();

        VertexBuffer svb = getBuffer(VertexBuffer.Type.Size);
        ByteBuffer sizes = (ByteBuffer) svb.getData();

        // update data in vertex buffers
        positions.rewind();
        colors.rewind();
        sizes.rewind();
        for (Particle p : particles){
            positions.put(p.position.x)
                     .put(p.position.y)
                     .put(p.position.z);
            sizes.put((byte) ((int) (p.size * 255) & 0xFF));
            colors.putInt(p.color.asIntRGBA());
        }
        positions.flip();
        colors.flip();
        sizes.flip();

        // force renderer to re-send data to GPU
        pvb.updateData(positions);
        cvb.updateData(colors);
        svb.updateData(sizes);
    }

}
