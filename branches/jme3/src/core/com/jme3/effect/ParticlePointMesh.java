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

package com.jme3.effect;

import com.jme3.renderer.Camera;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ParticlePointMesh extends ParticleMesh {

    private ParticleEmitter emitter;

    @Override
    public void initParticleData(ParticleEmitter emitter, int numParticles, int imagesX, int imagesY) {
        setMode(Mode.Points);
        setVertexCount(numParticles);
        setTriangleCount(numParticles);

        this.emitter = emitter;

        // set positions
        FloatBuffer pb = BufferUtils.createVector3Buffer(numParticles);
        VertexBuffer pvb = new VertexBuffer(VertexBuffer.Type.Position);
        pvb.setupData(Usage.Stream, 3, Format.Float, pb);
        setBuffer(pvb);

        // set colors
        ByteBuffer cb = BufferUtils.createByteBuffer(numParticles * 4);
        VertexBuffer cvb = new VertexBuffer(VertexBuffer.Type.Color);
        cvb.setupData(Usage.Stream, 4, Format.UnsignedByte, cb);
        cvb.setNormalized(true);
        setBuffer(cvb);

        // set sizes
        ByteBuffer sb = BufferUtils.createByteBuffer(numParticles);
        VertexBuffer svb = new VertexBuffer(VertexBuffer.Type.Size);
        svb.setupData(Usage.Stream, 1, Format.UnsignedByte, sb);
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
