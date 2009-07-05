package com.g3d.renderer.lwjgl;

import com.g3d.light.DirectionalLight;
import com.g3d.light.Light;
import com.g3d.light.LightList;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.material.RenderState;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix3f;
import com.g3d.math.Matrix4f;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.GLObjectManager;
import com.g3d.renderer.IDList;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Format;
import com.g3d.scene.VertexBuffer.Type;
import com.g3d.scene.VertexBuffer.Usage;
import com.g3d.renderer.RenderContext;
import com.g3d.renderer.queue.RenderQueue;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Geometry;
import com.g3d.shader.Attribute;
import com.g3d.shader.Shader;
import com.g3d.shader.Shader.ShaderSource;
import com.g3d.shader.Shader.ShaderType;
import com.g3d.shader.Uniform;
import com.g3d.system.lwjgl.LwjglContext;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.FrameBuffer.RenderBuffer;
import com.g3d.texture.Image;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture.WrapAxis;
import com.g3d.util.BufferUtils;
import com.g3d.util.TempVars;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.lwjgl.opengl.ARBGeometryShader4;
//import org.lwjgl.opengl.ARBHalfFloatVertex;
//import org.lwjgl.opengl.ARBVertexArrayObject;
//import org.lwjgl.opengl.ARBHalfFloatVertex;
//import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.EXTTextureArray;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.NVHalfFloat;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.EXTFramebufferMultisample.*;
import static org.lwjgl.opengl.EXTFramebufferBlit.*;
//import static org.lwjgl.opengl.ARBDrawInstanced.*;

public class LwjglRenderer implements Renderer {

    private static final Logger logger = Logger.getLogger(LwjglRenderer.class.getName());
    private static final boolean VALIDATE_SHADER = true;

    private ByteBuffer nameBuf = BufferUtils.createByteBuffer(250);
    private StringBuilder stringBuf = new StringBuilder(250);

    private LwjglContext owner;
    private RenderQueue queue;
    private RenderContext context = new RenderContext();
    private GLObjectManager objManager = new GLObjectManager();
    
    private final Matrix4f worldMatrix = new Matrix4f();
    private final Matrix4f orthoMatrix = new Matrix4f();

    // current state
    private Camera camera;
    private Shader boundShader;
    private Material forcedMaterial;

    private int glslVer;
    private int vertexTextureUnits;
    private int fragTextureUnits;
    private int vertexUniforms;
    private int fragUniforms;
    private int vertexAttribs;
    private int maxFBOSamples;
    private int maxFBOAttachs;
    private int maxRBSize;
    private int maxTexSize;
    private int maxCubeTexSize;
    private int maxVertCount;
    private int maxTriCount;

    public LwjglRenderer(LwjglContext owner){
        queue = new RenderQueue(this);
        this.owner = owner;
    }

    protected void updateNameBuffer(){
        int len = stringBuf.length();

        nameBuf.position(0);
        nameBuf.limit(len);
        for (int i = 0; i < len; i++)
            nameBuf.put((byte)stringBuf.charAt(i));

        nameBuf.rewind();
    }

    public void initialize(){
        TempVars vars = TempVars.get();

        String version = glGetString(GL_SHADING_LANGUAGE_VERSION);
        if (version.startsWith("1.4")){
            glslVer = 140;
        }else if (version.startsWith("1.3")){
            glslVer = 130;
        }else if (version.startsWith("1.2")){
            glslVer = 120;
        }else if (version.startsWith("1.1")){
            glslVer = 110;
        }else if (version.startsWith("1.")){
            glslVer = 100;
        }else if (version == null || version.equals("")){
            glslVer = -1;
        }

        glGetInteger(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, vars.intBuffer16);
        vertexTextureUnits = vars.intBuffer16.get(0);
        logger.log(Level.FINER, "VTF Units: {0}", vertexTextureUnits);

        glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS, vars.intBuffer16);
        fragTextureUnits = vars.intBuffer16.get(0);
        logger.log(Level.FINER, "Texture Units: {0}", fragTextureUnits);

        glGetInteger(GL_MAX_VERTEX_UNIFORM_COMPONENTS, vars.intBuffer16);
        vertexUniforms = vars.intBuffer16.get(0);
        logger.log(Level.FINER, "Vertex Uniforms: {0}", vertexUniforms);

        glGetInteger(GL_MAX_FRAGMENT_UNIFORM_COMPONENTS, vars.intBuffer16);
        fragUniforms = vars.intBuffer16.get(0);
        logger.log(Level.FINER, "Fragment Uniforms: {0}", fragUniforms);
        
        glGetInteger(GL_MAX_ELEMENTS_VERTICES, vars.intBuffer16);
        maxVertCount = vars.intBuffer16.get(0);
        logger.log(Level.FINER, "Preferred Batch Vertex Count: {0}", maxVertCount);
        
        glGetInteger(GL_MAX_ELEMENTS_INDICES, vars.intBuffer16);
        maxTriCount = vars.intBuffer16.get(0);
        logger.log(Level.FINER, "Preferred Batch Index Count: {0}", maxTriCount);

        glGetInteger(GL_MAX_TEXTURE_SIZE, vars.intBuffer16);
        maxTexSize = vars.intBuffer16.get(0);
        logger.log(Level.FINER, "Maximum Texture Resolution: {0}", maxTexSize);

        glGetInteger(GL_MAX_CUBE_MAP_TEXTURE_SIZE, vars.intBuffer16);
        maxCubeTexSize = vars.intBuffer16.get(0);
        logger.log(Level.FINER, "Maximum CubeMap Resolution: {0}", maxCubeTexSize);

        if (GLContext.getCapabilities().GL_EXT_framebuffer_object){
            glGetInteger(GL_MAX_RENDERBUFFER_SIZE_EXT, vars.intBuffer16);
            maxRBSize = vars.intBuffer16.get(0);
            logger.log(Level.FINER, "FBO RB Max Size: {0}", maxRBSize);

            glGetInteger(GL_MAX_COLOR_ATTACHMENTS_EXT, vars.intBuffer16);
            maxFBOAttachs = vars.intBuffer16.get(0);
            logger.log(Level.FINER, "FBO Max renderbuffers: {0}", maxFBOAttachs);
            
            if (GLContext.getCapabilities().GL_EXT_framebuffer_multisample){
                glGetInteger(GL_MAX_SAMPLES_EXT, vars.intBuffer16);
                maxFBOSamples = vars.intBuffer16.get(0);
                logger.log(Level.FINER, "FBO Max Samples: {0}", maxFBOSamples);
            }
        }
    }

    public void cleanup(){
        queue.clear();
        objManager.deleteAllObjects(this);
    }

    /*********************************************************************\
    |* Render State                                                      *|
    \*********************************************************************/
    public void clearBuffers(boolean color, boolean depth, boolean stencil){
        int bits = 0;
        if (color) bits = GL_COLOR_BUFFER_BIT;
        if (depth) bits |= GL_DEPTH_BUFFER_BIT;
        if (stencil) bits |= GL_STENCIL_BUFFER_BIT;
        if (bits != 0) glClear(bits);
    }

    public void setBackgroundColor(ColorRGBA color){
        glClearColor(color.r, color.g, color.b, color.a);
    }

    public void applyRenderState(RenderState state){
        if (state.isWireframe() && !context.wireframe){
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            context.wireframe = true;
        }else if (!state.isWireframe() && context.wireframe){
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            context.wireframe = false;
        }
        if (state.isDepthTest() && !context.depthTestEnabled){
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            context.depthTestEnabled = true;
        }else if (!state.isDepthTest() && context.depthTestEnabled){
            glDisable(GL_DEPTH_TEST);
            context.depthTestEnabled = false;
        }
        if (state.isAlphaTest() && !context.alphaTestEnabled){
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, state.getAlphaFallOff());
            context.alphaTestEnabled = true;
        }else if (!state.isAlphaTest() && context.alphaTestEnabled){
            glDisable(GL_ALPHA_TEST);
            context.alphaTestEnabled = false;
        }
        if (state.isDepthWrite() && !context.depthWriteEnabled){
            glDepthMask(true);
            context.depthWriteEnabled = true;
        }else if (!state.isDepthWrite() && context.depthWriteEnabled){
            glDepthMask(false);
            context.depthWriteEnabled = false;
        }
        if (state.isColorWrite() && !context.colorWriteEnabled){
            glColorMask(true,true,true,true);
            context.colorWriteEnabled = true;
        }else if (!state.isColorWrite() && context.colorWriteEnabled){
            glColorMask(false,false,false,false);
            context.colorWriteEnabled = false;
        }
        if (state.isPolyOffset()){
            if (!context.polyOffsetEnabled){
                glEnable(GL_POLYGON_OFFSET_FILL);
                glPolygonOffset(state.getPolyOffsetFactor(),
                                state.getPolyOffsetUnits());
                context.polyOffsetEnabled = true;
                context.polyOffsetFactor = state.getPolyOffsetFactor();
                context.polyOffsetUnits = state.getPolyOffsetUnits();
            }else{
                if (state.getPolyOffsetFactor() != context.polyOffsetFactor
                 || state.getPolyOffsetUnits() != context.polyOffsetUnits){
                    glPolygonOffset(state.getPolyOffsetFactor(),
                                    state.getPolyOffsetUnits());
                    context.polyOffsetFactor = state.getPolyOffsetFactor();
                    context.polyOffsetUnits = state.getPolyOffsetUnits();
                }
            }
        }else{
            if (context.polyOffsetEnabled){
                glDisable(GL_POLYGON_OFFSET_FILL);
                context.polyOffsetEnabled = false;
                context.polyOffsetFactor = 0;
                context.polyOffsetUnits = 0;
            }
        }
        if (state.getFaceCullMode() != context.cullMode){
            if (state.getFaceCullMode() == RenderState.FaceCullMode.Off)
                glDisable(GL_CULL_FACE);
            else
                glEnable(GL_CULL_FACE);

            switch (state.getFaceCullMode()){
                case Off:
                    break;
                case Back:
                    glCullFace(GL_BACK);
                    break;
                case Front:
                    glCullFace(GL_FRONT);
                    break;
                case FrontAndBack:
                    glCullFace(GL_FRONT_AND_BACK);
                    break;
                default:
                    throw new UnsupportedOperationException("Unrecognized face cull mode: "+
                                                            state.getFaceCullMode());
            }

            context.cullMode = state.getFaceCullMode();
        }

        if (state.getBlendMode() != context.blendMode){
            if (state.getBlendMode() == RenderState.BlendMode.Off)
                glDisable(GL_BLEND);
            else
                glEnable(GL_BLEND);

            switch (state.getBlendMode()){
                case Off:
                    break;
                case Additive:
                    glBlendFunc(GL_ONE, GL_ONE);
                    break;
                case Alpha:
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    break;
                case PremultAlpha:
                    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
                    break;
                case Modulate:
                    glBlendFunc(GL_DST_COLOR, GL_ZERO);
                    break;
                case ModulateX2:
                    glBlendFunc(GL_DST_COLOR, GL_SRC_COLOR);
                    break;
                default:
                    throw new UnsupportedOperationException("Unrecognized blend mode: "+
                                                            state.getBlendMode());
            }

            context.blendMode = state.getBlendMode();
        }
    }

    public void setupDepthPass(){
        glEnable(GL_DEPTH_TEST);
        glStencilMask(0x0);
        glDepthMask(true);
        glColorMask(false, false, false, false);
        glDepthFunc(GL_LESS);
        context.depthTestEnabled = true;
    }

    public void setupColorPass(){
        glEnable(GL_DEPTH_TEST);
        glStencilMask(0x0);
        glDepthMask(false);
        glColorMask(true, true, true, true);
        glDepthFunc(GL_EQUAL);
        context.depthTestEnabled = false;
    }

    /*********************************************************************\
    |* Camera and World transforms                                       *|
    \*********************************************************************/

    public void setCamera(Camera cam) {
        if (this.camera == cam)
            return;
        
        this.camera = cam;
        // the GL state needs an update in viewport
        updateViewPort(cam.getWidth(), cam.getHeight());
    }

    public Camera getCamera(){
        return camera;
    }

    private void updateViewPort(float width, float height){
        int x = (int) (camera.getViewPortLeft() * width);
        int y = (int) (camera.getViewPortBottom() * height);
        int w = (int) ((camera.getViewPortRight() - camera.getViewPortLeft()) * width);
        int h = (int) ((camera.getViewPortTop() - camera.getViewPortBottom()) * height);
        glViewport(x, y, w, h);

        //also update ortho matrix
        orthoMatrix.loadIdentity();

        float tx = -(w+x)/(w-x);
        float ty = -(h+y)/(h-y);
        float tz = 0;
        orthoMatrix.setTranslation(tx, ty, tz);

        float m00 = 2f / (w-x);
        float m11 = 2f / (h-y);
        float m22 = -1f;
        orthoMatrix.setScale(new Vector3f(m00,m11,m22));
    }

    public void onFrame(){
        objManager.deleteUnused(this);

        if (camera.isViewportChanged()){
            updateViewPort(camera.getWidth(), camera.getHeight());
            camera.clearViewportChanged();
        }
    }

    public void setWorldMatrix(Matrix4f worldMatrix){
        this.worldMatrix.set(worldMatrix);
    }

    /*********************************************************************\
    |* Shaders                                                           *|
    \*********************************************************************/

    protected void updateUniform(Shader shader, Uniform uniform){
        if (uniform.getName() == null)
            throw new IllegalArgumentException("Uniform must have a name!");

        int shaderId = shader.getId();
        if (shaderId == -1)
            throw new IllegalArgumentException("Shader has not been uploaded yet.");

        if (context.boundShaderProgram != shaderId){
            glUseProgram(shaderId);
            boundShader = shader;
            context.boundShaderProgram = shaderId;
        }

        int loc = uniform.getLocation();
        if (loc == -1)
            return;
        
        if (loc == -2){
            // get uniform location
            stringBuf.setLength(0);
            stringBuf.append(uniform.getName()).append('\0');
            updateNameBuffer();
            loc = glGetUniformLocation(shader.getId(), nameBuf);
            if (loc < 0){
                uniform.setLocation(-1);
                uniform.clearUpdateNeeded();
                // uniform is not declared in shader
                logger.warning("Uniform "+uniform.getName()+" is not declared in shader.");
                return;
            }
            uniform.setLocation(loc);
        }

        uniform.clearUpdateNeeded();
        FloatBuffer fb;
        switch (uniform.getDataType()){
            case Float:
                Float f = (Float)uniform.getValue();
                glUniform1f(loc, f.floatValue());
                break;
            case Vector2:
                Vector2f v2 = (Vector2f)uniform.getValue();
                glUniform2f(loc, v2.getX(), v2.getY());
                break;
            case Vector3:
                Vector3f v3 = (Vector3f)uniform.getValue();
                glUniform3f(loc, v3.getX(), v3.getY(), v3.getZ());
                break;
            case Vector4:
                ColorRGBA c = (ColorRGBA)uniform.getValue();
                glUniform4f(loc, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
                break;
            case Boolean:
                Boolean b = (Boolean)uniform.getValue();
                glUniform1i(loc, b.booleanValue() ? GL_TRUE : GL_FALSE);
                break;
            case Matrix3:
                fb = (FloatBuffer)uniform.getValue();
                assert fb.remaining() == 9;
                glUniformMatrix3(loc, false, fb);
                break;
            case Matrix4:
                fb = (FloatBuffer)uniform.getValue();
                assert fb.remaining() == 16;
                glUniformMatrix4(loc, false, fb);
                break;
            case FloatArray:
                fb = (FloatBuffer)uniform.getValue();
                glUniform1(loc, fb);
                break;
            case Vector2Array:
                fb = (FloatBuffer)uniform.getValue();
                glUniform2(loc, fb);
                break;
            case Vector3Array:
                fb = (FloatBuffer)uniform.getValue();
                glUniform3(loc, fb);
                break;
            case Vector4Array:
                fb = (FloatBuffer)uniform.getValue();
                glUniform4(loc, fb);
                break;
            case Int:
                Integer i = (Integer)uniform.getValue();
                glUniform1i(loc, i.intValue());
                break;
        }
    }

    protected void updateShaderUniforms(Shader shader){
        for (Uniform uniform : shader.getUniforms()){
            if (uniform.isUpdateNeeded())
                updateUniform(shader, uniform);
        }
    }

    public void updateLightListUniforms(Shader shader, Geometry g, int numLights){
        if (numLights == 0) // this shader does not do lighting, ignore.
            return;

        LightList lightList = g.getWorldLightList();
        Uniform lightColor = shader.getUniform("g_LightColor");
        Uniform lightPos = shader.getUniform("g_LightPosition");
        lightColor.setVector4Length(numLights);
        lightPos.setVector4Length(numLights);
        for (int i = 0; i < numLights; i++){
            if (lightList.size() <= i){
                lightColor.setVector4InArray(0f, 0f, 0f, 0f, i);
                lightPos.setVector4InArray(0f, 0f, 0f, 0f, i);
            }else{
                Light l = lightList.get(i);
                ColorRGBA color = l.getColor();
                lightColor.setVector4InArray(color.getRed(),
                                             color.getGreen(),
                                             color.getBlue(),
                                             l.getType().getId(),
                                             i);

                switch (l.getType()){
                    case Directional:
                        DirectionalLight dl = (DirectionalLight) l;
                        Vector3f dir = dl.getDirection();
                        lightPos.setVector4InArray(dir.getX(), dir.getY(), dir.getZ(), -1, i);
                        break;
                    case Point:
                        PointLight pl = (PointLight) l;
                        Vector3f pos = pl.getPosition();
                        float invRadius = pl.getRadius();
                        if (invRadius != 0){
                            invRadius = 1f / invRadius;
                        }
                        lightPos.setVector4InArray(pos.getX(), pos.getY(), pos.getZ(), invRadius, i);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown type of light: "+l.getType());
                }
            }
        }
    }

    /* (Non-Javadoc)
     * TODO: Should really implement this in another class.
     */
    public void updateWorldParameters(List<Uniform> params){
        // assums worldMatrix is properly set.
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();
        Matrix4f viewProjMatrix = camera.getViewProjectionMatrix();

        TempVars vars = TempVars.get();
        Matrix4f tempMat4 = vars.tempMat4;
        Matrix3f tempMat3 = vars.tempMat3;
        Vector3f tempVec3 = vars.vect1;
        Vector2f tempVec2 = vars.vect2d;
        Quaternion tempVec4 = vars.quat1;

        for (int i = 0; i < params.size(); i++){
            Uniform u = params.get(i);
            switch (u.getBinding()){
                case WorldMatrix:
                    u.setMatrix4(worldMatrix);
                    break;
                case ViewMatrix:
                    u.setMatrix4(viewMatrix);
                    break;
                case ProjectionMatrix:
                    u.setMatrix4(projMatrix);
                    break;
                case WorldViewMatrix:
//                    tempMat4.loadIdentity();
                    tempMat4.set(viewMatrix);
                    tempMat4.multLocal(worldMatrix);
                    u.setMatrix4(tempMat4);
                    break;
                case NormalMatrix:
//                    tempMat4.loadIdentity();
                    tempMat4.set(viewMatrix);
                    tempMat4.multLocal(worldMatrix);
                    tempMat4.toRotationMatrix(tempMat3);
                    tempMat3.invertLocal();
                    tempMat3.transposeLocal();
                    
                    u.setMatrix3(tempMat3);
                    break;
                case OrthoMatrix:
                    u.setMatrix4(orthoMatrix);
                    break;
                case WorldOrthoMatrix:
//                    tempMat4.loadIdentity();
                    tempMat4.set(orthoMatrix);
                    tempMat4.multLocal(worldMatrix);
                    u.setMatrix4(tempMat4);
                    break;
                case WorldViewProjectionMatrix:
//                    tempMat4.loadIdentity();
                    tempMat4.set(viewProjMatrix);
                    tempMat4.multLocal(worldMatrix);
                    u.setMatrix4(tempMat4);
                    break;
                case ViewMatrixInverse:
                    tempMat4.set(viewMatrix);
                    tempMat4.invertLocal();
                    u.setMatrix4(tempMat4);
                    break;
                case ViewPort:
                    tempVec4.set(camera.getViewPortLeft(),
                                 camera.getViewPortTop(),
                                 camera.getViewPortRight(),
                                 camera.getViewPortBottom());
                    u.setVector4(tempVec4);
                    break;
                case Resolution:
                    tempVec2.set(camera.getWidth(), camera.getHeight());
                    u.setVector2(tempVec2);
                    break;
                case Aspect:
                    float aspect = ((float) camera.getWidth()) / camera.getHeight();
                    u.setFloat(aspect);
                    break;
                case CameraPosition:
                    u.setVector3(camera.getLocation());
                    break;
                case CameraDirection:
                    camera.getDirection(tempVec3);
                    u.setVector3(tempVec3);
                    break;
                case CameraLeft:
                    camera.getLeft(tempVec3);
                    u.setVector3(tempVec3);
                    break;
                case CameraUp:
                    camera.getUp(tempVec3);
                    u.setVector3(tempVec3);
                    break;
                case Time:
                    u.setFloat(owner.getTimer().getTimeInSeconds());
                    break;
                case Tpf:
                    u.setFloat(owner.getTimer().getTimePerFrame());
                    break;
                case FrameRate:
                    u.setFloat(owner.getTimer().getFrameRate());
                    break;
            }
        }
    }

    public int convertShaderType(ShaderType type){
        switch (type){
            case Fragment:
                return GL_FRAGMENT_SHADER;
            case Vertex:
                return GL_VERTEX_SHADER;
//            case Geometry:
//                return ARBGeometryShader4.GL_GEOMETRY_SHADER_ARB;
            default:
                throw new RuntimeException("Unrecognized shader type.");
        }
    }

    public void updateShaderSourceData(ShaderSource source){
        int id = source.getId();
        if (id == -1){
            // create id
            id = glCreateShader(convertShaderType(source.getType()));
            assert id >= 0;
            source.setId(id);
        }

        // upload shader source
        // merge the defines and source code
        byte[] definesCodeData = source.getDefines().getBytes();
        byte[] sourceCodeData = source.getSource().getBytes();
        ByteBuffer codeBuf = BufferUtils.createByteBuffer(definesCodeData.length
                                                        + sourceCodeData.length);
        codeBuf.put(definesCodeData);
        codeBuf.put(sourceCodeData);
        codeBuf.flip();

        // debug stuff:
//        try{
//            File f = new File(source.getType().name() + source.hashCode() + ".txt");
//            System.out.println(f.getAbsoluteFile());
//            FileChannel chn = new FileOutputStream(f).getChannel();
//            chn.write(codeBuf);
//            chn.close();
//            codeBuf.rewind();
//        } catch (Throwable ex){
//        }

        glShaderSource(id, codeBuf);
        glCompileShader(id);

        IntBuffer temp = TempVars.get().intBuffer1;
        glGetShader(id, GL_COMPILE_STATUS, temp);

        boolean compiledOK = temp.get(0) == GL_TRUE;
        String infoLog = null;

        if (VALIDATE_SHADER || !compiledOK){
            // even if compile succeeded, check
            // log for warnings
            glGetShader(id, GL_INFO_LOG_LENGTH, temp);
            int length = temp.get(0);
            if (length > 3){
                // get infos
                ByteBuffer logBuf = BufferUtils.createByteBuffer(length);
                glGetShaderInfoLog(id, null, logBuf);
                byte[] logBytes = new byte[length];
                logBuf.get(logBytes, 0, length);
                 // convert to string, etc
                infoLog = new String(logBytes);
            }
        }

        if (compiledOK){
            if (infoLog != null){
                logger.info(source.getName()+" compile success\n" + infoLog);
            }else{
                logger.fine(source.getName()+" compile success");
            }
        }else{
            if (infoLog != null){
                logger.warning(source.getName()+" compile error: "+infoLog);
            }else{
                logger.warning(source.getName()+" compile error: ?");
            }
        }

        source.clearUpdateNeeded();
        // only usable if compiled
        source.setUsable(compiledOK);
        if (!compiledOK){
            // make sure to dispose id cause all program's
            // shaders will be cleared later.
            glDeleteShader(id);
        }else{
            // register for cleanup since the ID is usable
            objManager.registerForCleanup(source);
        }
    }

    public void updateShaderData(Shader shader){
        int id = shader.getId();
        boolean needRegister = false;
        if (id == -1){
            // create program
            id = glCreateProgram();
            assert id >= 0;
            shader.setId(id);
            needRegister = true;
        }

        for (ShaderSource source : shader.getSources()){
            if (source.isUpdateNeeded()){
                updateShaderSourceData(source);
                // shader has been compiled here
            }

            if (!source.isUsable()){
                // it's useless.. just forget about everything..
                shader.setUsable(false);
                shader.clearUpdateNeeded();
                return;
            }
            glAttachShader(id, source.getId());
        }
        // link shaders to program
        glLinkProgram(id);

        IntBuffer temp = TempVars.get().intBuffer1;

        glGetProgram(id, GL_LINK_STATUS, temp);
        boolean linkOK = temp.get(0) == GL_TRUE;
        String infoLog = null;
        
        if (VALIDATE_SHADER || !linkOK){
            glGetProgram(id, GL_INFO_LOG_LENGTH, temp);
            int length = temp.get(0);
            if (length > 3){
                // get infos
                ByteBuffer logBuf = BufferUtils.createByteBuffer(length);
                glGetProgramInfoLog(id, null, logBuf);

                // convert to string, etc
                byte[] logBytes = new byte[length];
                logBuf.get(logBytes, 0, length);
                infoLog = new String(logBytes);
            }
        }

        if (linkOK){
            if (infoLog != null){
                logger.info("shader link success. \n"+infoLog);
            }else{
                logger.fine("shader link success");
            }
        }else{
            if (infoLog != null){
                logger.warning("shader link failure. \n"+infoLog);
            }else{
                logger.warning("shader link failure");
            }
        }

        shader.clearUpdateNeeded();
        if (!linkOK){
            // failure.. forget about everything
            shader.resetSources();
            shader.setUsable(false);
            deleteShader(shader);
        }else{
            shader.setUsable(true);
            if (needRegister)
                objManager.registerForCleanup(shader);
        }
    }

    public void setShader(Shader shader){
        if (shader == null){
            if (context.boundShaderProgram > 0){
                glUseProgram(0);
                context.boundShaderProgram = 0;
                boundShader = null;
            }
        } else {
            if (shader.isUpdateNeeded())
                updateShaderData(shader);
            // NOTE: might want to check if any of the 
            // sources need an update?
            
            if (!shader.isUsable())
                return;

            updateShaderUniforms(shader);
            if (context.boundShaderProgram != shader.getId()){
                if (VALIDATE_SHADER){
                    // check if shader can be used
                    // with current state
                    glValidateProgram(shader.getId());
                    glGetProgram(shader.getId(), GL_VALIDATE_STATUS, TempVars.get().intBuffer1);
                    boolean validateOK = TempVars.get().intBuffer1.get(0) == GL_TRUE;
                    if (validateOK){
                        logger.fine("shader validate success");
                    }else{
                        logger.warning("shader validate failure");
                    }
                }

                glUseProgram(shader.getId());
                context.boundShaderProgram = shader.getId();
                boundShader = shader;
            }
        }
    }

    public void deleteShaderSource(ShaderSource source){
        if (source.getId() < 0){
            logger.warning("Shader source is not uploaded to GPU, cannot delete.");
            return;
        }
        source.setUsable(false);
        source.clearUpdateNeeded();
        glDeleteShader(source.getId());
    }

    public void deleteShader(Shader shader){
        if (shader.getId() == -1){
            logger.warning("Shader is not uploaded to GPU, cannot delete.");
            return;
        }
        for (ShaderSource source : shader.getSources()){
            if (source.getId() != -1){
                glDetachShader(shader.getId(), source.getId());
                // the next part is done by the GLObjectManager automatically
//                glDeleteShader(source.getId());
            }
        }
        // kill all references so sources can be collected
        // if needed.
        shader.resetSources();
        glDeleteProgram(shader.getId());
    }

    /*********************************************************************\
    |* Framebuffers                                                      *|
    \*********************************************************************/
    public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst){
        if (GLContext.getCapabilities().GL_EXT_framebuffer_blit){
            int srcW = 0;
            int srcH = 0;
            int dstW = 0;
            int dstH = 0;
            int prevFBO = context.boundFBO;

            if (src != null && src.isUpdateNeeded())
                updateFrameBuffer(src);

            if (dst != null && dst.isUpdateNeeded())
                updateFrameBuffer(dst);

            if (src == null){
                glBindFramebufferEXT(GL_READ_FRAMEBUFFER_EXT, 0);
                srcW = camera.getWidth();
                srcH = camera.getHeight();
            }else{  
                glBindFramebufferEXT(GL_READ_FRAMEBUFFER_EXT, src.getId());
                srcW = src.getWidth();
                srcH = src.getHeight();
            }
            if (dst == null){
                glBindFramebufferEXT(GL_DRAW_FRAMEBUFFER_EXT, 0);
                dstW = camera.getWidth();
                dstH = camera.getHeight();
            }else{
                glBindFramebufferEXT(GL_DRAW_FRAMEBUFFER_EXT, dst.getId());
                dstW = dst.getWidth();
                dstH = dst.getHeight();
            }
            glBlitFramebufferEXT(0, 0, srcW, srcH,
                                 0, 0, dstW, dstH,
                                 GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT,
                                 GL_NEAREST);
            
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, prevFBO);
            checkFrameBufferError();
        }else{
            throw new UnsupportedOperationException("EXT_framebuffer_blit required.");
              // TODO: support non-blit copies?
        }
    }

    private void checkFrameBufferError() {
        int status = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
        switch (status) {
            case GL_FRAMEBUFFER_COMPLETE_EXT:
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED_EXT:
                //Choose different formats
                throw new IllegalStateException("Framebuffer object format is " +
                                                "unsupported by the video hardware.");
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                throw new IllegalStateException("Framebuffer has erronous attachment.");
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                throw new IllegalStateException("Framebuffer is missing required attachment.");
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                throw new IllegalStateException("Framebuffer attachments must have same dimensions.");
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                throw new IllegalStateException("Framebuffer attachments must have same formats.");
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                throw new IllegalStateException("Incomplete draw buffer.");
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                throw new IllegalStateException("Incomplete read buffer.");
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_EXT:
                throw new IllegalStateException("Incomplete multisample buffer.");
            default:
                //Programming error; will fail on all hardware
                throw new IllegalStateException("Some video driver error " +
                                                "or programming error occured. " +
                                                "Framebuffer object status is invalid. ");
        }
    }

    private void updateRenderBuffer(FrameBuffer fb, RenderBuffer rb){
        int id = rb.getId();
        if (id == -1){
            glGenRenderbuffersEXT(TempVars.get().intBuffer1);
            id = TempVars.get().intBuffer1.get(0);
            rb.setId(id);
        }

        if (context.boundRB != id){
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, id);
            context.boundRB = id;
        }

        if (fb.getWidth() > maxRBSize || fb.getHeight() > maxRBSize)
            throw new UnsupportedOperationException("Resolution "+fb.getWidth()+
                                                    ":"+fb.getHeight()+" is not supported.");
       
        if (fb.getSamples() > 0 && GLContext.getCapabilities().GL_EXT_framebuffer_multisample){
            int samples = fb.getSamples();
            if (maxFBOSamples < samples){
                samples = maxFBOSamples;
            }
            glRenderbufferStorageMultisampleEXT(GL_RENDERBUFFER_EXT,
                                                samples,
                                                TextureUtil.convertTextureFormat(rb.getFormat()),
                                                fb.getWidth(),
                                                fb.getHeight());
        }else{
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT,
                                     TextureUtil.convertTextureFormat(rb.getFormat()),
                                     fb.getWidth(),
                                     fb.getHeight());
        }
    }

    private int convertAttachmentSlot(int attachmentSlot){
        // can also add support for stencil here
        if (attachmentSlot == -100){
            return GL_DEPTH_ATTACHMENT_EXT;
        }else if (attachmentSlot < 0 || attachmentSlot >= 16){
            throw new UnsupportedOperationException("Invalid FBO attachment slot: "+attachmentSlot);
        }

        return GL_COLOR_ATTACHMENT0_EXT + attachmentSlot;
    }

    public void updateRenderTexture(FrameBuffer fb, RenderBuffer rb){
        Texture tex = rb.getTexture();
        if (tex.isUpdateNeeded())
            updateTextureData(tex);

        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT,
                                  convertAttachmentSlot(rb.getSlot()),
                                  convertTextureType(tex.getType()),
                                  tex.getId(),
                                  0);
    }

    public void updateFrameBufferAttachment(FrameBuffer fb, RenderBuffer rb){
        boolean needAttach;
        if (rb.getTexture() == null){
            // if it hasn't been created yet, then attach is required.
            needAttach = rb.getId() == -1;
            updateRenderBuffer(fb, rb);
        }else{
            needAttach = false;
            updateRenderTexture(fb, rb);
        }
        if (needAttach){
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT,
                                         convertAttachmentSlot(rb.getSlot()),
                                         GL_RENDERBUFFER_EXT,
                                         rb.getId());
        }
    }

    public void updateFrameBuffer(FrameBuffer fb) {
        int id = fb.getId();
        if (id == -1){
            // create FBO
            glGenFramebuffersEXT(TempVars.get().intBuffer1);
            id = TempVars.get().intBuffer1.get(0);
            fb.setId(id);
            objManager.registerForCleanup(fb);
        }

        if (context.boundFBO != id){
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, id);
            // binding an FBO automatically sets draw buf to GL_COLOR_ATTACHMENT0
            context.boundDrawBuf = 0;
            context.boundFBO = id;
        }

        FrameBuffer.RenderBuffer depthBuf = fb.getDepthBuffer();
        if (depthBuf != null){
            updateFrameBufferAttachment(fb, depthBuf);
        }

        FrameBuffer.RenderBuffer colorBuf = fb.getColorBuffer();
        if (colorBuf != null){
            updateFrameBufferAttachment(fb, colorBuf);
        }

        fb.clearUpdateNeeded();
    }

    public void setFrameBuffer(FrameBuffer fb) {
        // make sure all prior render commands have been sent
        renderQueue();

        if (fb == null){
            // unbind any fbos
            if (context.boundFBO != 0){
                glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

                // update viewport to reflect framebuffer's resolution
                updateViewPort(camera.getWidth(), camera.getHeight());

                context.boundFBO = 0;
            }
            // select back buffer
//            if (context.boundDrawBuf != -1){
//                glDrawBuffer(GL_BACK);
//                context.boundDrawBuf = -1;
//            }
        }else{
            if (fb.isUpdateNeeded())
               updateFrameBuffer(fb);

            if (context.boundFBO != fb.getId()){
                glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fb.getId());
                
                // update viewport to reflect framebuffer's resolution
                if (fb.getWidth() != camera.getWidth() || fb.getHeight() != camera.getHeight())
                    updateViewPort(fb.getWidth(), fb.getHeight());

                context.boundFBO = fb.getId();
            }
            if (fb.getColorBuffer() == null){
                // make sure to select NONE as draw buf
                // no color buffer attached. select NONE
                if (context.boundDrawBuf != -2){
                    glReadBuffer(GL_NONE);
                    glDrawBuffer(GL_NONE);
                    context.boundDrawBuf = -2;
                }
            }else{
                RenderBuffer rb = fb.getColorBuffer();
                // select this draw buffer
                if (context.boundDrawBuf != rb.getSlot()){
                    glDrawBuffer(GL_COLOR_ATTACHMENT0_EXT + rb.getSlot());
                    context.boundDrawBuf = rb.getSlot();
                }
            }


            assert fb.getId() >= 0;
            assert context.boundFBO == fb.getId();
        }

        checkFrameBufferError();
    }

    private void deleteRenderBuffer(FrameBuffer fb, RenderBuffer rb){
        TempVars.get().intBuffer1.put(0, rb.getId());
        glDeleteRenderbuffersEXT(TempVars.get().intBuffer1);
    }

    public void deleteFrameBuffer(FrameBuffer fb) {
        if (fb.getId() != -1){
            if (context.boundFBO == fb.getId()){
                glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
                context.boundFBO = 0;
            }
            
            if (fb.getDepthBuffer() != null){
                deleteRenderBuffer(fb, fb.getDepthBuffer());
            }
            if (fb.getColorBuffer() != null){
                deleteRenderBuffer(fb, fb.getColorBuffer());
            }

            TempVars.get().intBuffer1.put(0, fb.getId());
            glDeleteFramebuffersEXT(TempVars.get().intBuffer1);
            fb.resetObject();
        }
    }


    /*********************************************************************\
    |* Textures                                                          *|
    \*********************************************************************/

    private int convertTextureType(Texture.Type type){
        switch (type){
            case OneDimensional:
                return GL_TEXTURE_1D;
            case TwoDimensional:
                return GL_TEXTURE_2D;
            case TwoDimensionalArray:
                return EXTTextureArray.GL_TEXTURE_2D_ARRAY_EXT;
            case ThreeDimensional:
                return GL_TEXTURE_3D;
            case CubeMap:
                return GL_TEXTURE_CUBE_MAP;
            default:
                throw new UnsupportedOperationException("Unknown texture type: "+type);
        }
    }

    private int convertMagFilter(Texture.MagFilter filter){
        switch (filter){
            case Bilinear:
                return GL_LINEAR;
            case Nearest:
                return GL_NEAREST;
            default:
                throw new UnsupportedOperationException("Unknown mag filter: "+filter);
        }
    }

    private int convertMinFilter(Texture.MinFilter filter){
        switch (filter){
            case Trilinear:
                return GL_LINEAR_MIPMAP_LINEAR;
            case BilinearNearestMipMap:
                return GL_LINEAR_MIPMAP_NEAREST;
            case NearestLinearMipMap:
                return GL_NEAREST_MIPMAP_LINEAR;
            case NearestNearestMipMap:
                return GL_NEAREST_MIPMAP_NEAREST;
            case BilinearNoMipMaps:
                return GL_LINEAR;
            case NearestNoMipMaps:
                return GL_NEAREST;
            default:
                throw new UnsupportedOperationException("Unknown min filter: "+filter);
        }
    }

    private int convertWrapMode(Texture.WrapMode mode){
        switch (mode){
            case BorderClamp:
                return GL_CLAMP_TO_BORDER;
            case Clamp:
                return GL_CLAMP;
            case EdgeClamp:
                return GL_CLAMP_TO_EDGE;
            case Repeat:
                return GL_REPEAT;
            case MirroredRepeat:
                return GL_MIRRORED_REPEAT;
            default:
                throw new UnsupportedOperationException("Unknown wrap mode: "+mode);
        }
    }

    public void updateTextureData(Texture tex){
        int texId = tex.getId();
        if (texId == -1){
            // create texture
            glGenTextures(TempVars.get().intBuffer1);
            texId = TempVars.get().intBuffer1.get(0);
            tex.setId(texId);
            objManager.registerForCleanup(tex);
        }

        // bind texture
        int target = convertTextureType(tex.getType());
        if (context.boundTextures[0] != tex){
            if (context.boundTextureUnit != 0){
                glActiveTexture(GL_TEXTURE0);
                context.boundTextureUnit = 0;
            }

            glBindTexture(target, texId);
            context.boundTextures[0] = tex;
        }

        // filter things
        int minFilter = convertMinFilter(tex.getMinFilter());
        int magFilter = convertMagFilter(tex.getMagFilter());
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, minFilter);
		glTexParameteri(target, GL_TEXTURE_MAG_FILTER, magFilter);

        if (tex.getAnisotropicFilter() > 1){
            if (GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic){
                glTexParameterf(target,
                                EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                                tex.getAnisotropicFilter());
            }
        }
        // repeat modes
        switch (tex.getType()){
            case ThreeDimensional:
                glTexParameteri(target, GL_TEXTURE_WRAP_R, convertWrapMode(tex.getWrap(WrapAxis.R)));
            case TwoDimensional:
            case TwoDimensionalArray:
            case CubeMap:
                glTexParameteri(target, GL_TEXTURE_WRAP_T, convertWrapMode(tex.getWrap(WrapAxis.T)));
                // fall down here is intentional..
            case OneDimensional:
                glTexParameteri(target, GL_TEXTURE_WRAP_S, convertWrapMode(tex.getWrap(WrapAxis.S)));
                break;
            default:
                throw new UnsupportedOperationException("Unknown texture type: "+tex.getType());
        }

        // R to Texture compare mode
        if (tex.getShadowCompareMode() != Texture.ShadowCompareMode.Off){
            glTexParameteri(target, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
            glTexParameteri(target, GL_DEPTH_TEXTURE_MODE, GL_INTENSITY);
            if (tex.getShadowCompareMode() == Texture.ShadowCompareMode.GreaterOrEqual){
                glTexParameteri(target, GL_TEXTURE_COMPARE_FUNC, GL_GEQUAL);
            }else{
                glTexParameteri(target, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
            }
        }

        Image img = tex.getImage();
        if (img != null){
            if (img.getData().size() > 0 && !img.hasMipmaps() && tex.getMinFilter().usesMipMapLevels()){
                // No pregenerated mips available,
                // generate from base level if required
                glTexParameteri(target, GL_GENERATE_MIPMAP, GL_TRUE);
            }
            if (target == GL_TEXTURE_CUBE_MAP){
                List<ByteBuffer> data = img.getData();
                if (data.size() != 6){
                    logger.warning("Invalid texture: "+tex+
                                   "Cubemap textures must contain 6 data units.");
                    return;
                }
                for (int i = 0; i < 6; i++){
                    TextureUtil.uploadTexture(img, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, i, 0);
                }
            }else if (target == EXTTextureArray.GL_TEXTURE_2D_ARRAY_EXT){
                List<ByteBuffer> data = img.getData();
                // -1 index specifies prepare data for 2D Array
                TextureUtil.uploadTexture(img, target, -1, 0);
                for (int i = 0; i < data.size(); i++){
                    // upload each slice of 2D array in turn
                    // this time with the appropriate index
                     TextureUtil.uploadTexture(img, target, i, 0);
                }
            }else{
                TextureUtil.uploadTexture(img, target, 0, 0);
            }
        }

        tex.clearUpdateNeeded();
    }

    public void setTexture(int unit, Texture tex){
         if (tex.isUpdateNeeded())
            updateTextureData(tex);

         int texId = tex.getId();
         assert texId != -1;

         Texture[] textures = context.boundTextures;
//         if (textures[unit] == tex)
//             return;

         int type = convertTextureType(tex.getType());
         if (!context.textureIndexList.moveToNew(unit)){
             if (context.boundTextureUnit != unit){
                glActiveTexture(GL_TEXTURE0 + unit);
                context.boundTextureUnit = unit;
             }

             glEnable(type);
             //System.out.println("Enabled TEX UNIT: "+unit);
         }

         if (textures[unit] != tex){
             if (context.boundTextureUnit != unit){
                glActiveTexture(GL_TEXTURE0 + unit);
                context.boundTextureUnit = unit;
             }

             glBindTexture(type, texId);
             textures[unit] = tex;
         }
    }

    public void clearTextureUnits(){
        IDList textureList = context.textureIndexList;
        Texture[] textures = context.boundTextures;
        for (int i = 0; i < textureList.oldLen; i++){
            int idx = textureList.oldList[i];

            if (context.boundTextureUnit != idx){
                glActiveTexture(GL_TEXTURE0 + idx);
                context.boundTextureUnit = idx;
            }
            glDisable(convertTextureType(textures[idx].getType()));

            //System.out.println("Disabled TEX UNIT: "+idx);
            textures[idx] = null;
        }
        context.textureIndexList.copyNewToOld();
    }

    public void deleteTexture(Texture tex){
        int texId = tex.getId();
        if (texId != -1){
            IntBuffer temp = TempVars.get().intBuffer1;
            temp.put(0, texId);
            temp.position(0);
            temp.limit(1);
            glDeleteTextures(temp);
            tex.resetObject();
        }
    }

    /*********************************************************************\
    |* Vertex Buffers and Attributes                                     *|
    \*********************************************************************/

    private int convertUsage(Usage usage){
        switch (usage){
            case Static:
                return GL_STATIC_DRAW;
            case Dynamic:
            case DynamicWriteOnly:
                return GL_DYNAMIC_DRAW;
            case Stream:
            case StreamWriteOnly:
                return GL_STREAM_DRAW;
            default:
                throw new RuntimeException("Unknown usage type.");
        }
    }

    private int convertFormat(Format format){
        switch (format){
            case Byte:
                return GL_BYTE;
            case UnsignedByte:
                return GL_UNSIGNED_BYTE;
            case Short:
                return GL_SHORT;
            case UnsignedShort:
                return GL_UNSIGNED_SHORT;
            case Int:
                return GL_INT;
            case UnsignedInt:
                return GL_UNSIGNED_INT;
            case Half:
                return NVHalfFloat.GL_HALF_FLOAT_NV;
//                return ARBHalfFloatVertex.GL_HALF_FLOAT;
            case Float:
                return GL_FLOAT;
            case Double:
                return GL_DOUBLE;
            default:
                throw new RuntimeException("Unknown buffer format.");

        }
    }

    public void updateBufferData(VertexBuffer vb){
        int bufId = vb.getId();
        if (bufId == -1){
            // create buffer
            glGenBuffers(TempVars.get().intBuffer1);
            bufId = TempVars.get().intBuffer1.get(0);
            vb.setId(bufId);
            objManager.registerForCleanup(vb);
        }

        // bind buffer
        int target;
        if (vb.getBufferType() == VertexBuffer.Type.Index){
            target = GL_ELEMENT_ARRAY_BUFFER;
            if (context.boundElementArrayVBO != bufId){
                glBindBuffer(target, bufId);
                context.boundElementArrayVBO = bufId;
            }
        }else{
            target = GL_ARRAY_BUFFER;
            if (context.boundArrayVBO != bufId){
                glBindBuffer(target, bufId);
                context.boundArrayVBO = bufId;
            }
        }

        int usage = convertUsage(vb.getUsage());
        vb.getData().rewind();
        // upload data based on format
        switch (vb.getFormat()){
            case Byte:
            case UnsignedByte:
            case Half:
                glBufferData(target, (ByteBuffer) vb.getData(), usage);
                break;
            case Short:
            case UnsignedShort:
                glBufferData(target, (ShortBuffer) vb.getData(), usage);
                break;
            case Int:
            case UnsignedInt:
                glBufferData(target, (IntBuffer) vb.getData(), usage);
                break;
            case Float:
                glBufferData(target, (FloatBuffer) vb.getData(), usage);
                break;
            case Double:
                glBufferData(target, (DoubleBuffer) vb.getData(), usage);
                break;
            default:
                throw new RuntimeException("Unknown buffer format.");
        }

        vb.clearUpdateNeeded();
    }

    public void deleteBuffer(VertexBuffer vb) {
        int bufId = vb.getId();
        if (bufId != -1){
            // delete buffer
            IntBuffer temp = TempVars.get().intBuffer1;
            temp.put(0, bufId);
            temp.position(0);
            temp.limit(1);
            glDeleteBuffers(temp);
            vb.resetObject();

            //System.out.println(vb + ": Deleted");
        }
    }

    public void clearVertexAttribs(){
        IDList attribList = context.attribIndexList;
        for (int i = 0; i < attribList.oldLen; i++){
            int idx = attribList.oldList[i];
            glDisableVertexAttribArray(idx);
            //System.out.println("Disabled ATTRIB IDX: "+idx);
            context.boundAttribs[idx] = null;
        }
        context.attribIndexList.copyNewToOld();
//        context.attribIndexList.print();
    }

    public void setVertexAttrib(VertexBuffer vb){
        if (vb.getBufferType() == VertexBuffer.Type.Index)
            throw new IllegalArgumentException("Index buffers not allowed to be set to vertex attrib");

        if (vb.isUpdateNeeded())
            updateBufferData(vb);

        int bufId = vb.getId();
        assert bufId != -1;

        int programId = context.boundShaderProgram;
        if (programId > 0){
            Attribute attrib = boundShader.getAttribute(vb.getBufferType().name());
            int loc = attrib.getLocation();
            if (loc == -1)
                return; // not defined

            if (loc == -2){
                stringBuf.setLength(0);
                stringBuf.append("in").append(vb.getBufferType().name()).append('\0');
                updateNameBuffer();
                loc = glGetAttribLocation(programId, nameBuf);

                // not really the name of it in the shader (inPosition\0) but
                // the internal name of the enum (Position).
                if (loc < 0){
                    attrib.setLocation(-1);
                    return; // not available in shader.
                }else{
                    attrib.setLocation(loc);
                }
            }

            VertexBuffer[] attribs = context.boundAttribs;
            if (!context.attribIndexList.moveToNew(loc)){
                glEnableVertexAttribArray(loc);
                //System.out.println("Enabled ATTRIB IDX: "+loc);
            }
            if (attribs[loc] != vb){
                if (context.boundArrayVBO != bufId){
                    glBindBuffer(GL_ARRAY_BUFFER, bufId);
                    context.boundArrayVBO = bufId;
                }

                glVertexAttribPointer(loc, vb.getNumComponents(), convertFormat(vb.getFormat()), false, 0, 0);
                attribs[loc] = vb;
            }
        }else{
            throw new IllegalStateException("Cannot render mesh without shader bound");
        }
    }

    public void drawTriangleList(VertexBuffer indexBuf, Mesh.Mode mode, int count){
        if (indexBuf.getBufferType() != VertexBuffer.Type.Index)
            throw new IllegalArgumentException("Only index buffers are allowed as triangle lists.");

        if (indexBuf.isUpdateNeeded())
            updateBufferData(indexBuf);

        int bufId = indexBuf.getId();
        assert bufId != -1;

        if (context.boundElementArrayVBO != bufId){
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufId);
            context.boundElementArrayVBO = bufId;
        }
//        if (count > 1){
//            glDrawElementsInstancedARB(mode,
//                                       indexBuf.getData().capacity(),
//                                       convertFormat(indexBuf.getFormat()),
//                                       0,
//                                       count);
//        }else{
            glDrawElements(convertElementMode(mode),
                           indexBuf.getData().capacity(),
                           convertFormat(indexBuf.getFormat()),
                           0);
//        }
    }

    /*********************************************************************\
    |* Render Calls                                                      *|
    \*********************************************************************/
    public void renderQueue(){
        queue.renderQueue(Bucket.Opaque);
        queue.renderQueue(Bucket.Sky);
        
        // transparent is last because it require blending with the 
        // rest of the scene's objects
        queue.renderQueue(Bucket.Transparent);
        queue.renderQueue(Bucket.Gui);
    }

    public void renderShadowQueue(RenderQueue.ShadowMode shadBucket){
        queue.renderShadowQueue(shadBucket);
    }

    public void addToQueue(Geometry geom, RenderQueue.Bucket bucket){
        queue.addToQueue(geom, bucket);
    }

    public void addToShadowQueue(Geometry geom, RenderQueue.ShadowMode shadBucket){
        queue.addToShadowQueue(geom, shadBucket);
    }
    
    public RenderQueue getRenderQueue(){
        return queue;
    }

    public void setForcedMaterial(Material mat){
        forcedMaterial = mat;
    }

    public int convertElementMode(Mesh.Mode mode){
        switch (mode){
            case Points:
                return GL_POINTS;
            case Lines:
                return GL_LINES;
            case LineLoop:
                return GL_LINE_LOOP;
            case LineStrip:
                return GL_LINE_STRIP;
            case Triangles:
                return GL_TRIANGLES;
            case TriangleFan:
                return GL_TRIANGLE_FAN;
            case TriangleStrip:
                return GL_TRIANGLE_STRIP;
            default:
                throw new UnsupportedOperationException("Unrecognized mesh mode: "+mode);
        }
    }

    public void renderMesh(Mesh mesh, int count) {
        VertexBuffer indices = null;
        for (VertexBuffer vb : mesh.getBuffers()){
            if (vb.getBufferType() == Type.Index){
                indices = vb;
            }else{
                setVertexAttrib(vb);
            }
        }
        if (indices != null){
            drawTriangleList(indices, mesh.getMode(), 1);
        }else{
            glDrawArrays(convertElementMode(mesh.getMode()), 0, mesh.getVertexCount());
        }
        clearVertexAttribs();
        clearTextureUnits();
    }

//    public void updateVertexArray(Mesh mesh){
//        int id = mesh.getId();
//        if (id == -1){
//            IntBuffer temp = TempVars.get().intBuffer1;
//            ARBVertexArrayObject.glGenVertexArrays(temp);
//            id = temp.get(0);
//            mesh.setId(id);
//        }
//
//        if (context.boundVertexArray != id){
//            ARBVertexArrayObject.glBindVertexArray(id);
//            context.boundVertexArray = id;
//        }
//
//        for (VertexBuffer vb : mesh.getBuffers()){
//            if (vb.getBufferType() != Type.Index){
//                setVertexAttrib(vb);
//            }
//        }
//
//    }

    public void renderGeometry(Geometry geom){
        setWorldMatrix(geom.getWorldMatrix());
        if (geom.getMaterial() == null){
            logger.warning("Unable to render geometry "+geom+". No material defined!");
            return;
        }
        if (forcedMaterial != null){
            // use forced material
            forcedMaterial.apply(geom, this);
        }else{
            // use geometry's material
            geom.getMaterial().apply(geom, this);
        }

        Mesh mesh = geom.getMesh();
//        if (GLContext.getCapabilities().GL_ARB_vertex_array_object){
//            if (mesh.getId() == -1){
//                updateVertexArray(mesh);
//            }
//
//            if (context.boundVertexArray != mesh.getId()){
//                ARBVertexArrayObject.glBindVertexArray(mesh.getId());
//                context.boundVertexArray = mesh.getId();
//            }
//
//            VertexBuffer indices = mesh.getBuffer(Type.Index);
//            if (indices != null){
//                drawTriangleList(indices, mesh.getMode(), 1);
//            }else{
//                glDrawArrays(convertElementMode(mesh.getMode()), 0, mesh.getVertexCount());
//            }
//            clearTextureUnits();
//        }else{
            renderMesh(mesh, 1);
//        }
    }

}
