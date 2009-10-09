package com.g3d.renderer;

public enum Caps {

    /// Framebuffer features
    FrameBuffer,
    FrameBufferMRT,
    FrameBufferMultisample,

    /// API Version
    OpenGL20,
    OpenGL21,
    OpenGL30,
    OpenGL31,
    OpenGL32,

    /// Shader language version
    ARBprogram,
    GLSL100,
    GLSL110,
    GLSL120,
    GLSL130,
    GLSL140,
    GLSL150,

    /**
     * Supports reading from textures inside the vertex shader.
     */
    VertexTextureFetch,

    /**
     * Supports geometry shader.
     */
    GeometryShader,

    /**
     * Supports texture arrays
     */
    TextureArray,

    /**
     * Supports floating point textures (Format.RGB16F)
     */
    FloatTexture,

    /**
     * Supports floating point FBO color buffers (Format.RGB16F)
     */
    FloatColorBuffer,

    /**
     * Supports floating point depth buffer
     */
    FloatDepthBuffer,

    /**
     * Supports Format.RGB111110F for textures
     */
    PackedFloatTexture,

    /**
     * Supports Format.RGB9E5 for textures
     */
    SharedExponentTexture,

    /**
     * Supports Format.RGB111110F for FBO color buffers
     */
    PackedFloatColorBuffer,

    /**
     * Supports Format.RGB9E5 for FBO color buffers
     */
    SharedExponentColorBuffer,

    /// Vertex Buffer features
    MeshInstancing,

    /**
     * Supports VAO, or vertex buffer arrays
     */
    VertexBufferArray,

}
