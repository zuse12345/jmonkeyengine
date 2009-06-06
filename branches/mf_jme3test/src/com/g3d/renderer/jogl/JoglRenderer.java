package com.g3d.renderer.jogl;

import com.g3d.math.Matrix4f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer;
import com.g3d.shader.Shader;
import com.g3d.shader.Uniform;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Texture;
import java.util.List;
import javax.media.opengl.GL;

public class JoglRenderer implements Renderer {

    protected Camera camera;
    protected GL gl;

    public void setGL(GL gl){
        this.gl = gl;
    }

    public void clearBuffers(boolean color, boolean depth, boolean stencil) {
    }

    public void setDepthTest(boolean enabled) {
        gl.glEnable(gl.GL_DEPTH_TEST);
        gl.glDepthFunc(gl.GL_LESS);
    }

    public void setBackfaceCulling(boolean enabled) {
        gl.glCullFace(gl.GL_BACK);
    }

    public void onFrame() {
        // etc
    }

    public void setCamera(Camera cam) {
        this.camera = cam;
    }

    public Camera getCamera() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWorldMatrix(Matrix4f worldMatrix) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateWorldParameters(List<Uniform> params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateLightListUniforms(Shader shader, Geometry geom, int numLights) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateShaderData(Shader shader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setShader(Shader shader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deleteShader(Shader shader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateTextureData(Texture tex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFrameBuffer(FrameBuffer fb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateFrameBuffer(FrameBuffer fb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deleteFrameBuffer(FrameBuffer fb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTexture(int unit, Texture tex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clearTextureUnits() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deleteTexture(Texture tex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateBufferData(VertexBuffer vb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deleteBuffer(VertexBuffer vb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setVertexAttrib(VertexBuffer vb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void drawTriangleList(VertexBuffer indexBuf, int count) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clearVertexAttribs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void renderQueue() {
    }

    public void addToQueue(Geometry geom, Bucket bucket) {
    }

    public void renderMesh(Mesh mesh, int count) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void renderGeometry(Geometry geom) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void cleanup() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
