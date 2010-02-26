package com.jme3.renderer.jogl;

import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.light.PointLight;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.renderer.Caps;
import com.jme3.renderer.GLObjectManager;
import com.jme3.renderer.RenderContext;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.shader.Shader;
import com.jme3.shader.Shader.ShaderSource;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;
import com.jme3.util.TempVars;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import javax.media.opengl.GL;

public class JoglRenderer implements Renderer {

    private static final Logger logger = Logger.getLogger(JoglRenderer.class.getName());

    protected Matrix4f worldMatrix = new Matrix4f();
    protected Matrix4f viewMatrix = new Matrix4f();
    protected Matrix4f projMatrix = new Matrix4f();
    protected FloatBuffer fb16 = BufferUtils.createFloatBuffer(16);
    protected GL gl;

    private RenderContext context = new RenderContext();
    private GLObjectManager objManager = new GLObjectManager();

    public JoglRenderer(GL gl){
        this.gl = gl;
    }

    public void setGL(GL gl){
        this.gl = gl;
    }

    public void initialize(){
        logger.info("Vendor: "+gl.glGetString(gl.GL_VENDOR));
        logger.info("Renderer: "+gl.glGetString(gl.GL_RENDERER));
        logger.info("Version: "+gl.glGetString(gl.GL_VERSION));
        
        applyRenderState(RenderState.DEFAULT);
    }

    public void setBackgroundColor(ColorRGBA color) {
        gl.glClearColor(color.r, color.g, color.b, color.a);
    }

    public void cleanup(){
        objManager.deleteAllObjects(this);
    }

    public void clearBuffers(boolean color, boolean depth, boolean stencil) {
        int bits = 0;
        if (color) bits = gl.GL_COLOR_BUFFER_BIT;
        if (depth) bits |= gl.GL_DEPTH_BUFFER_BIT;
        if (stencil) bits |= gl.GL_STENCIL_BUFFER_BIT;
        if (bits != 0) gl.glClear(bits);
    }

    public void applyRenderState(RenderState state){
        if (state.isWireframe() && !context.wireframe){
            gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_LINE);
            context.wireframe = true;
        }else if (!state.isWireframe() && context.wireframe){
            gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
            context.wireframe = false;
        }
        if (state.isDepthTest() && !context.depthTestEnabled){
            gl.glEnable(gl.GL_DEPTH_TEST);
            gl.glDepthFunc(gl.GL_LEQUAL);
            context.depthTestEnabled = true;
        }else if (!state.isDepthTest() && context.depthTestEnabled){
            gl.glDisable(gl.GL_DEPTH_TEST);
            context.depthTestEnabled = false;
        }
        if (state.isAlphaTest() && !context.alphaTestEnabled){
            gl.glEnable(gl.GL_ALPHA_TEST);
            gl.glAlphaFunc(gl.GL_GREATER, state.getAlphaFallOff());
            context.alphaTestEnabled = true;
        }else if (!state.isAlphaTest() && context.alphaTestEnabled){
            gl.glDisable(gl.GL_ALPHA_TEST);
            context.alphaTestEnabled = false;
        }
        if (state.isDepthWrite() && !context.depthWriteEnabled){
            gl.glDepthMask(true);
            context.depthWriteEnabled = true;
        }else if (!state.isDepthWrite() && context.depthWriteEnabled){
            gl.glDepthMask(false);
            context.depthWriteEnabled = false;
        }
        if (state.isColorWrite() && !context.colorWriteEnabled){
            gl.glColorMask(true,true,true,true);
            context.colorWriteEnabled = true;
        }else if (!state.isColorWrite() && context.colorWriteEnabled){
            gl.glColorMask(false,false,false,false);
            context.colorWriteEnabled = false;
        }
        if (state.isPolyOffset()){
            if (!context.polyOffsetEnabled){
                gl.glEnable(gl.GL_POLYGON_OFFSET_FILL);
                gl.glPolygonOffset(state.getPolyOffsetFactor(),
                                state.getPolyOffsetUnits());
                context.polyOffsetEnabled = true;
                context.polyOffsetFactor = state.getPolyOffsetFactor();
                context.polyOffsetUnits = state.getPolyOffsetUnits();
            }else{
                if (state.getPolyOffsetFactor() != context.polyOffsetFactor
                 || state.getPolyOffsetUnits() != context.polyOffsetUnits){
                    gl.glPolygonOffset(state.getPolyOffsetFactor(),
                                    state.getPolyOffsetUnits());
                    context.polyOffsetFactor = state.getPolyOffsetFactor();
                    context.polyOffsetUnits = state.getPolyOffsetUnits();
                }
            }
        }else{
            if (context.polyOffsetEnabled){
                gl.glDisable(gl.GL_POLYGON_OFFSET_FILL);
                context.polyOffsetEnabled = false;
                context.polyOffsetFactor = 0;
                context.polyOffsetUnits = 0;
            }
        }
        if (state.getFaceCullMode() != context.cullMode){
            if (state.getFaceCullMode() == RenderState.FaceCullMode.Off)
                gl.glDisable(gl.GL_CULL_FACE);
            else
                gl.glEnable(gl.GL_CULL_FACE);

            switch (state.getFaceCullMode()){
                case Off:
                    break;
                case Back:
                    gl.glCullFace(gl.GL_BACK);
                    break;
                case Front:
                    gl.glCullFace(gl.GL_FRONT);
                    break;
                case FrontAndBack:
                    gl.glCullFace(gl.GL_FRONT_AND_BACK);
                    break;
                default:
                    throw new UnsupportedOperationException("Unrecognized face cull mode: "+
                                                            state.getFaceCullMode());
            }

            context.cullMode = state.getFaceCullMode();
        }

        if (state.getBlendMode() != context.blendMode){
            if (state.getBlendMode() == RenderState.BlendMode.Off)
                gl.glDisable(gl.GL_BLEND);
            else
                gl.glEnable(gl.GL_BLEND);

            switch (state.getBlendMode()){
                case Off:
                    break;
                case Additive:
                    gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE);
                    break;
                case Alpha:
                    gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
                    break;
                case PremultAlpha:
                    gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE_MINUS_SRC_ALPHA);
                    break;
                case Modulate:
                    gl.glBlendFunc(gl.GL_DST_COLOR, gl.GL_ZERO);
                    break;
                case ModulateX2:
                    gl.glBlendFunc(gl.GL_DST_COLOR, gl.GL_SRC_COLOR);
                    break;
                default:
                    throw new UnsupportedOperationException("Unrecognized blend mode: "+
                                                            state.getBlendMode());
            }

            context.blendMode = state.getBlendMode();
        }
    }

    public void onFrame() {
        objManager.deleteUnused(this);
    }

    public void setDepthRange(float start, float end) {
        gl.glDepthRange(start, end);
    }

    public void setViewPort(int x, int y, int width, int height){
        gl.glViewport(x, y, width, height);
    }

    private FloatBuffer storeMatrix(Matrix4f matrix, FloatBuffer store){
        store.rewind();
        matrix.fillFloatBuffer(store,true);
        store.rewind();
        return store;
    }

    private void updateModelView(){
        assert TempVars.get().lock();
        FloatBuffer store = TempVars.get().floatBuffer16;

        //update modelview
        if (context.matrixMode != gl.GL_MODELVIEW){
            gl.glMatrixMode(gl.GL_MODELVIEW);
            context.matrixMode = gl.GL_MODELVIEW;
        }

        gl.glLoadMatrixf(storeMatrix(viewMatrix,store));
        gl.glMultMatrixf(storeMatrix(worldMatrix,store));
        assert TempVars.get().unlock();
    }

    private void updateProjection(){
        assert TempVars.get().lock();
        FloatBuffer store = TempVars.get().floatBuffer16;

        //update projection
        if (context.matrixMode != gl.GL_PROJECTION){
            gl.glMatrixMode(gl.GL_PROJECTION);
            context.matrixMode = gl.GL_PROJECTION;
        }

        gl.glLoadMatrixf(storeMatrix(projMatrix,store));
        assert TempVars.get().unlock();
    }

    public void setViewProjectionMatrices(Matrix4f viewMatrix, Matrix4f projMatrix) {
        this.viewMatrix.set(viewMatrix);
        this.projMatrix.set(projMatrix);
    }

    public void setWorldMatrix(Matrix4f worldMatrix) {
        this.worldMatrix.set(worldMatrix);
        updateModelView();
    }

    public void setLighting(LightList list) {
        if (list.size() == 0) {
            // turn off lighting
            gl.glDisable(gl.GL_LIGHTING);
            return;
        }

        gl.glEnable(gl.GL_LIGHTING);
        gl.glShadeModel(gl.GL_SMOOTH);

        float[] temp = new float[4];

        // reset model view to specify
        // light positions in world space
        // instead of model space
        gl.glPushMatrix();
        gl.glLoadIdentity();

        for (int i = 0; i < list.size()+1; i++){
            if (list.size() <= i){
                // goes beyond the num lights we need
                // disable it
                gl.glDisable(gl.GL_LIGHT0 + i);
                break;
            }
            
            Light l = list.get(i);
            int lightId = gl.GL_LIGHT0 + i;

            ColorRGBA color = l.getColor();
            color.toArray(temp);

            gl.glEnable(lightId);
            gl.glLightfv(lightId, gl.GL_DIFFUSE,  temp, 0);
            gl.glLightfv(lightId, gl.GL_SPECULAR, temp, 0);
            
            ColorRGBA.Black.toArray(temp);
            gl.glLightfv(lightId, gl.GL_AMBIENT,  temp, 0);

            switch (l.getType()){
                case Directional:
                    DirectionalLight dl = (DirectionalLight) l;
                    dl.getDirection().toArray(temp);
                    temp[3] = 0f; // marks to GL its a directional light
                    gl.glLightfv(lightId, gl.GL_POSITION, temp, 0);
                    break;
                case Point:
                    PointLight pl = (PointLight) l;
                    pl.getPosition().toArray(temp);
                    temp[3] = 1f; // marks to GL its a point light
                    gl.glLightfv(lightId, gl.GL_POSITION, temp, 0);
                    break;
            }

        }

        // restore modelview to original value
        gl.glPopMatrix();
    }

    public void updateShaderSourceData(ShaderSource source) {
    }

    public void deleteShaderSource(ShaderSource source) {
    }

    public void updateShaderData(Shader shader) {
    }

    public void setShader(Shader shader) {
    }

    public void deleteShader(Shader shader) {
    }

    public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst) {
    }

    public void setFrameBuffer(FrameBuffer fb) {
    }

    public void updateFrameBuffer(FrameBuffer fb) {
    }

    public void deleteFrameBuffer(FrameBuffer fb) {
    }

    private int convertTextureType(Texture.Type type){
        switch (type){
            case TwoDimensional:
                return gl.GL_TEXTURE_2D;
            case TwoDimensionalArray:
                return gl.GL_TEXTURE_2D_ARRAY_EXT;
            case ThreeDimensional:
                return gl.GL_TEXTURE_3D;
            case CubeMap:
                return gl.GL_TEXTURE_CUBE_MAP;
            default:
                throw new UnsupportedOperationException("Unknown texture type: "+type);
        }
    }

    private int convertMagFilter(Texture.MagFilter filter){
        switch (filter){
            case Bilinear:
                return gl.GL_LINEAR;
            case Nearest:
                return gl.GL_NEAREST;
            default:
                throw new UnsupportedOperationException("Unknown mag filter: "+filter);
        }
    }

    private int convertMinFilter(Texture.MinFilter filter){
        switch (filter){
            case Trilinear:
                return gl.GL_LINEAR_MIPMAP_LINEAR;
            case BilinearNearestMipMap:
                return gl.GL_LINEAR_MIPMAP_NEAREST;
            case NearestLinearMipMap:
                return gl.GL_NEAREST_MIPMAP_LINEAR;
            case NearestNearestMipMap:
                return gl.GL_NEAREST_MIPMAP_NEAREST;
            case BilinearNoMipMaps:
                return gl.GL_LINEAR;
            case NearestNoMipMaps:
                return gl.GL_NEAREST;
            default:
                throw new UnsupportedOperationException("Unknown min filter: "+filter);
        }
    }

    private int convertWrapMode(Texture.WrapMode mode){
        switch (mode){
            case BorderClamp:
                return gl.GL_CLAMP_TO_BORDER;
            case Clamp:
                return gl.GL_CLAMP;
            case EdgeClamp:
                return gl.GL_CLAMP_TO_EDGE;
            case Repeat:
                return gl.GL_REPEAT;
            case MirroredRepeat:
                return gl.GL_MIRRORED_REPEAT;
            default:
                throw new UnsupportedOperationException("Unknown wrap mode: "+mode);
        }
    }

    public void updateTextureData(Texture tex) {
    }

    public void setTexture(int unit, Texture tex) {
    }

    public void clearTextureUnits() {
    }

    public void deleteTexture(Texture tex) {
    }

    public void updateBufferData(VertexBuffer vb) {
    }

    public void deleteBuffer(VertexBuffer vb) {
    }

    private int convertArrayType(VertexBuffer.Type type){
        switch (type){
            case Position:
                return gl.GL_VERTEX_ARRAY;
            case Normal:
                return gl.GL_NORMAL_ARRAY;
            case TexCoord:
                return gl.GL_TEXTURE_COORD_ARRAY;
            case Color:
                return gl.GL_COLOR_ARRAY;
            default:
                return -1; // unsupported
        }
    }

    private int convertVertexFormat(VertexBuffer.Format fmt){
        switch (fmt){
            case Byte:
                return gl.GL_BYTE;
            case Double:
                return gl.GL_DOUBLE;
            case Float:
                return gl.GL_FLOAT;
            case Half:
                return gl.GL_HALF_FLOAT_ARB;
            case Int:
                return gl.GL_INT;
            case Short:
                return gl.GL_SHORT;
            case UnsignedByte:
                return gl.GL_UNSIGNED_BYTE;
            case UnsignedInt:
                return gl.GL_UNSIGNED_INT;
            case UnsignedShort:
                return gl.GL_UNSIGNED_SHORT;
            default:
                throw new UnsupportedOperationException("Unrecognized vertex format: "+fmt);
        }
    }

    public void setVertexAttrib(VertexBuffer vb) {
        int arrayType = convertArrayType(vb.getBufferType());
        if (arrayType == -1)
            return; // unsupported

        gl.glEnableClientState(arrayType);
        context.boundAttribs[vb.getBufferType().ordinal()] = vb;

        Buffer data = vb.getData();
        int comps = vb.getNumComponents();
        int type = convertVertexFormat(vb.getFormat());
        data.rewind();

        switch (vb.getBufferType()){
            case Position:
                gl.glVertexPointer(comps, type, 0, data);
                break;
            case Normal:
                gl.glNormalPointer(type, 0, data);
                break;
            case Color:
                gl.glColorPointer(comps, type, 0, data);
                break;
            case TexCoord:
                gl.glTexCoordPointer(comps, type, 0, data);
                break;
        }
    }

    public void clearVertexAttribs() {
        for (int i = 0; i < 16; i++){
            VertexBuffer vb = context.boundAttribs[i];
            if (vb != null){
                int arrayType = convertArrayType(vb.getBufferType());
                gl.glDisableClientState(arrayType);
                context.boundAttribs[vb.getBufferType().ordinal()] = null;
            }
        }
    }

    public void renderMeshDefault(Mesh mesh, int count) {
        VertexBuffer indices = null;
        IntMap<VertexBuffer> bufs = mesh.getBuffers();
        for (Entry<VertexBuffer> entry : bufs){
            VertexBuffer vb = entry.getValue();
            if (vb.getBufferType() == Type.Index){
                indices = vb;
            }else{
                setVertexAttrib(vb);
            }
        }
        if (indices != null){
            drawTriangleList(indices, mesh, 1);
        }else{
            gl.glDrawArrays(convertElementMode(mesh.getMode()), 0, mesh.getVertexCount());
        }
        clearVertexAttribs();
        clearTextureUnits();
    }

    private void updateDisplayList(Mesh mesh){
        if (mesh.getId() != -1){
            // delete list first
            gl.glDeleteLists(mesh.getId(), mesh.getId());
            mesh.setId(-1);
        }

        // create new display list
        // first set state to NULL
        applyRenderState(RenderState.NULL);

        // disable lighting
        setLighting(null);

        int id = gl.glGenLists(1);
        mesh.setId(id);
        gl.glNewList(id, gl.GL_COMPILE);
        renderMeshDefault(mesh, 1);
        gl.glEndList();
    }

    private int convertElementMode(Mesh.Mode mode){
        switch (mode){
            case Points:
                return gl.GL_POINTS;
            case Lines:
                return gl.GL_LINES;
            case LineLoop:
                return gl.GL_LINE_LOOP;
            case LineStrip:
                return gl.GL_LINE_STRIP;
            case Triangles:
                return gl.GL_TRIANGLES;
            case TriangleFan:
                return gl.GL_TRIANGLE_FAN;
            case TriangleStrip:
                return gl.GL_TRIANGLE_STRIP;
            default:
                throw new UnsupportedOperationException("Unrecognized mesh mode: "+mode);
        }
    }

    public void drawTriangleList(VertexBuffer indexBuf, Mesh mesh, int count) {
        Mesh.Mode mode = mesh.getMode();
        indexBuf.getData().rewind();
        gl.glDrawElements(convertElementMode(mode),
                          indexBuf.getData().capacity(),
                          convertVertexFormat(indexBuf.getFormat()),
                          indexBuf.getData());
    }

    private void renderMeshDisplayList(Mesh mesh){
        if (mesh.getId() == -1){
            updateDisplayList(mesh);
        }
        gl.glCallList(mesh.getId());
    }

    public void renderMesh(Mesh mesh, int lod, int count){
//        if (!geom.getLocalScale().equals(Vector3f.UNIT_XYZ)){
//            // enable normalize option
//            gl.glEnable(gl.GL_NORMALIZE);
//        }else{
//            gl.glDisable(gl.GL_NORMALIZE);
//        }
        updateProjection();
        
        boolean dynamic = false;
        IntMap<VertexBuffer> bufs = mesh.getBuffers();
        for (Entry<VertexBuffer> entry : bufs){
            if (entry.getValue().getUsage() != VertexBuffer.Usage.Static){
                dynamic = true;
            }
        }

        if (!dynamic){
            // dealing with a static object, generate display list
            renderMeshDisplayList(mesh);
        }else{
            renderMeshDefault(mesh, count);
        }
    }

    public Collection<Caps> getCaps() {
        return new ArrayList<Caps>();
    }

    

}
