package com.g3d.renderer;

import com.g3d.scene.VertexBuffer;
import com.g3d.texture.Texture;

/**
 * Represents the current state of the graphics library. This class is used
 * internally to reduce state changes. NOTE: This class is specific to OpenGL.
 */
public class RenderContext {
    /**
     * If back-face culling is enabled.
     */
    public boolean cullingEnabled = false;

    /**
     * If Depth testing is enabled.
     */
    public boolean depthTestEnabled = false;

    /**
     * The currently bound shader program.
     */
    public int boundShaderProgram;

    /**
     * Currently bound element array vertex buffer.
     */
    public int boundElementArrayVBO;

    public int boundVertexArray;

    /**
     * Currently bound array vertex buffer.
     */
    public int boundArrayVBO;

//    public int boundTexture;

    public int numTexturesSet = 0;

    /**
     * Current bound texture IDs for each texture unit.
     */
    public Texture[] boundTextures = new Texture[16];

    public IDList textureIndexList = new IDList();

    public int boundTextureUnit = 0;

    /**
     * Vertex attribs currently bound and enabled. If a slot is null, then
     * it is disabled.
     */
    public VertexBuffer[] boundAttribs = new VertexBuffer[16];

    public IDList attribIndexList = new IDList();
}
