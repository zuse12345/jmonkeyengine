package com.g3d.renderer;

import com.g3d.light.LightList;
import com.g3d.material.Technique;
import com.g3d.math.Matrix4f;
import com.g3d.math.Transform;
import com.g3d.renderer.queue.RenderQueue;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer;
import com.g3d.shader.Shader;
import com.g3d.shader.Uniform;
import com.g3d.shader.UniformBinding;
import com.g3d.texture.Texture;
import java.util.EnumMap;

public interface Renderer {

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
     * @param enabled Enable/disable depth testing (passing closer primitives).
     */
    public void setDepthTest(boolean enabled);

    /**
     * @param enabled Enable/disable back-face culling.
     */
    public void setBackfaceCulling(boolean enabled);

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

    public void updateWorldParameters(EnumMap<UniformBinding, Uniform> params);

    /**
     * Uploads the lights in the light list as two uniform arrays.<br/><br/>
     *      * <p>
     * <code>uniform vec4 g_LightColor[numLights];</code><br/>
     * // g_LightColor.rgb is the diffuse/specular color of the light.<br/>
     * // g_Lightcolor.a is the type of light, 0 = Directional, 1 = Point, <br/>
     * // 2 = Spot. <br/>
     * <br/>
     * <code>uniform vec4 g_LightPosition[numLights];</code><br/>
     * // g_LightPosition.xyz is the position of the light (for point lights)<br/>
     * // or the direction of the light (for directional lights).<br/>
     * // g_LightPosition.w is the inverse radius of the light, <br/>
     * </p>
     * 
     * @param shader
     * @param lightList
     */
    public void updateLightListUniforms(Shader shader, Geometry geom, int numLights);

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
     * @param count The number of triangles to draw from the buffer
     */
    public void drawTriangleList(VertexBuffer indexBuf, int count);

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
     * Adds an element to the queue.
     * 
     * @param geom
     * @param bucket The bucket into which to place the goemetry.
     */
    public void addToQueue(Geometry geom, RenderQueue.Bucket bucket);

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
