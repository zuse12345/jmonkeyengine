package com.g3d.renderer;

import com.g3d.light.LightList;
import com.g3d.material.Material;
import com.g3d.material.RenderState;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix4f;
import com.g3d.renderer.queue.RenderQueue;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer;
import com.g3d.shader.Shader;
import com.g3d.shader.Shader.ShaderSource;
import com.g3d.shader.Uniform;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Texture;
import java.util.Collection;
import java.util.List;

public interface Renderer {

    /**
     * @return The capabilities of the renderer.
     */
    public Collection<Caps> getCaps();

    /**
     * Clears certain channels of the current bound framebuffer.
     *
     * @param color True if to clear colors (RGBA)
     * @param depth True if to clear depth/z
     * @param stencil True if to clear stencil buffer (if available, otherwise
     * ignored)
     */
    public void clearBuffers(boolean color, boolean depth, boolean stencil);

    /**
     * Sets the background (aka clear) color.
     * @param color
     */
    public void setBackgroundColor(ColorRGBA color);

    /**
     * Applies the given renderstate, making the neccessary
     * GL calls so that the state is applied.
     */
    public void applyRenderState(RenderState state);

    /**
     * Called when a new frame has been rendered.
     */
    public void onFrame();

    /**
     * @param cam The camera to use for rendering. This changes the
     * view and projection matrices given in the shaders.
     */
    public void setCamera(Camera cam);

    /**
     * @return The camera set with <code>setCamera</code>.
     */
    public Camera getCamera();

    /**
     * @param transform The world transform to use. This changes
     * the world matrix given in the shader.
     */
    public void setWorldMatrix(Matrix4f worldMatrix);

    public void setViewPort(int x, int y, int width, int height);

    /**
     * Updates uniforms that are bound to world parameters.
     */
    public void updateWorldParameters(List<Uniform> params);

    public void setLighting(LightList lights);

    /**
     * Updates the shader source, creating an ID and registering
     * with the object manager.
     * @param source
     */
    public void updateShaderSourceData(ShaderSource source);

    /**
     * Uploads the shader source code and prepares it for use.
     * @param shader
     */
    public void updateShaderData(Shader shader);

    /**
     * @param shader Sets the shader to use for rendering, uploading it
     * if neccessary.
     */
    public void setShader(Shader shader);

    /**
     * @param shader The shader to delete. This method also deletes
     * the attached shader sources.
     */
    public void deleteShader(Shader shader);

    /**
     * Deletes the provided shader source.
     * @param source
     */
    public void deleteShaderSource(ShaderSource source);

    /**
     * Copies contents from src to dst, scaling if neccessary.
     */
    public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst);

    /**
     * Sets the framebuffer that will be drawn to.
     */
    public void setFrameBuffer(FrameBuffer fb);

    /**
     * Initializes the framebuffer, creating it if neccessary and allocating
     * requested renderbuffers.
     */
    public void updateFrameBuffer(FrameBuffer fb);

    /**
     * Deletes a framebuffer and all attached renderbuffers
     */
    public void deleteFrameBuffer(FrameBuffer fb);

    /**
     * Prepares the texture for use and uploads its image data if neceessary.
     */
    public void updateTextureData(Texture tex);

    /**
     * Sets the texture to use for the given texture unit.
     */
    public void setTexture(int unit, Texture tex);

    /**
     * Clears all set texture units
     * @see #setTexture
     */
    public void clearTextureUnits();

    /**
     * Deletes a texture from the GPU.
     * @param tex
     */
    public void deleteTexture(Texture tex);

    /**
     * Uploads the vertex buffer's data onto the GPU, assiging it an ID if
     * needed.
     */
    public void updateBufferData(VertexBuffer vb);

    /**
     * Deletes a vertex buffer from the GPU.
     * @param vb The vertex buffer to delete
     */
    public void deleteBuffer(VertexBuffer vb);

    /**
     * Sets the vertex attrib. This data is exposed in the shader depending
     * on type, e.g Type.Position would be given as inPosition, Type.Tangent is
     * given as inTangent, etc.
     *
     * @param vb
     * @throws InvalidArgumentException If the given vertex buffer is an
     * index buffer.
     */
    public void setVertexAttrib(VertexBuffer vb);

    /**
     * Draws the list of triangles given in the index buffer.
     * Each triangle is composed of 3 indices to a vertex, the attribs of which
     * are supplied using <code>setVertexAttrib</code>.
     * The int variable gl_VertexID can be used to access the current
     * vertex index inside the vertex shader.
     *
     * @param count The number of instances to draw
     */
    public void drawTriangleList(VertexBuffer indexBuf, Mesh.Mode mode, int count, int vertCount);

    /**
     * Clears all vertex attributes set with <code>setVertexAttrib</code>.
     */
    public void clearVertexAttribs();

    /**
     * Renders all geometry objects that are currently in the render queue.
     * Use addToQueue() to add objects to the render queue.
     */
    public void renderQueue();

    /**
     * Renders all geometry objects in the specified shadow queue.
     * Use addToShadowQueue() to add objects to shadow queues.
     * @param shadBucket
     */
    public void renderShadowQueue(RenderQueue.ShadowMode shadBucket);

    /**
     * @return the render queue
     */
    public RenderQueue getRenderQueue();

    /**
     * Adds an element to the queue.
     * 
     * @param geom
     * @param bucket The bucket into which to place the goemetry.
     */
    public void addToQueue(Geometry geom, RenderQueue.Bucket bucket);

    /**
     * Adds an element to the specified shadow queue.
     * @param geom
     * @param shadBucket 
     */
    public void addToShadowQueue(Geometry geom, RenderQueue.ShadowMode shadBucket);

    /**
     * Set the material to use to render all future objects.
     * This overrides the material set on the geometry and renders
     * with the provided material instead.
     * Use null to clear the material and return renderer to normal
     * functionality.
     * @param mat
     */
    public void setForcedMaterial(Material mat);

    /**
     * Renders <code>count</code> meshes, with the geometry data supplied.
     * The shader which is currently set with <code>setShader</code> is
     * responsible for transforming the input verticies into clip space
     * and shading it based on the given vertex attributes.
     * The int variable gl_InstanceID can be used to access the current
     * instance of the mesh being rendered inside the vertex shader.
     *
     * @param mesh
     * @param count
     */
    public void renderMesh(Mesh mesh, int count);

    /**
     * Renders the given mesh contained in the geometry, after applying
     * the world transform and the material contained in the geometry.
     */
    public void renderGeometry(Geometry geom);

    /**
     * Called when the display is restarted to delete
     * all created GL objects.
     */
    public void cleanup();
    
}
