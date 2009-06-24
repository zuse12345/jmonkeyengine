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

    /// Shader language version
    ARBprogram,
    GLSL110,
    GLSL120,
    GLSL130,
    GLSL140,

    /// Shader features
    VertexTextureFetch,
    GeometryShader,

    /// Texture features
    TextureArray,

    /// Texture & Buffer formats
    FloatTexture,
    FloatColorBuffer,
    FloatDepthBuffer,
    PackedFloatTexture,
    SharedExponentTexture,
    PackedFloatColorBuffer,
    SharedExponentColorBuffer,

    /// Vertex Buffer features
    MeshInstancing,
    VertexBufferArray,

}
