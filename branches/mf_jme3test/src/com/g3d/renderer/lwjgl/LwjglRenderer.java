package com.g3d.renderer.lwjgl;

import com.g3d.light.DirectionalLight;
import com.g3d.light.Light;
import com.g3d.light.LightList;
import com.g3d.light.PointLight;
import com.g3d.shader.UniformBinding;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix3f;
import com.g3d.math.Matrix4f;
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
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

//import org.lwjgl.opengl.ARBGeometryShader4;
import org.lwjgl.opengl.ARBHalfFloatVertex;
import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
//import static org.lwjgl.opengl.ARBDrawInstanced.*;

public class LwjglRenderer implements Renderer {

    private static final Logger logger = Logger.getLogger(LwjglRenderer.class.getName());

    private ByteBuffer nameBuf = BufferUtils.createByteBuffer(250);
    private StringBuilder stringBuf = new StringBuilder(250);

    private RenderQueue queue;
    private RenderContext context = new RenderContext();
    private GLObjectManager objManager = new GLObjectManager();
    
    private final Matrix4f worldMatrix = new Matrix4f();
//    private final FloatBuffer floatBuf16 = BufferUtils.createFloatBuffer(16);

    // current state
    private Camera camera;
    private Shader boundShader;

    private int glslVer;
    private int vertexTextureUnits;
    private int fragTextureUnits;
    private int vertexUniforms;
    private int fragUniforms;
    private int vertexAttribs;

    public LwjglRenderer(){
        queue = new RenderQueue(this);
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

        glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS, vars.intBuffer16);
        fragTextureUnits = vars.intBuffer16.get(0);

        glGetInteger(GL_MAX_VERTEX_UNIFORM_COMPONENTS, vars.intBuffer16);
        vertexUniforms = vars.intBuffer16.get(0);

        glGetInteger(GL_MAX_FRAGMENT_UNIFORM_COMPONENTS, vars.intBuffer16);
        fragUniforms = vars.intBuffer16.get(0);
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

    public void setDepthTest(boolean enabled) {
        if (enabled && !context.depthTestEnabled){
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
//            glColorMask(false, false, false, false);
//            glDepthMask(false);
//            glStencilMask(0x00);
            context.depthTestEnabled = true;
        }else if (!enabled && context.depthTestEnabled){
            glDisable(GL_DEPTH_TEST);
            context.depthTestEnabled = false;
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

    public void setBackfaceCulling(boolean enabled) {
        if (enabled && !context.cullingEnabled){
            glCullFace(GL_BACK);
            context.cullingEnabled = true;
        }else if (!enabled && context.cullingEnabled){
            glCullFace(GL_NONE);
            context.cullingEnabled = false;
        }
    }

    /*********************************************************************\
    |* Support checks                                                    *|
    \*********************************************************************/
    public void checkShaderSupport(String lang, int attribs, int uniforms){
        if (attribs > 0){
            // check attribute size
            // TODO: Finish shader support check
        }
    }


    /*********************************************************************\
    |* Camera and World transforms                                       *|
    \*********************************************************************/

    public void setCamera(Camera cam) {
        this.camera = cam;
    }

    public Camera getCamera(){
        return camera;
    }

    public void onFrame(){
        objManager.deleteUnused(this);

        if (camera.isViewportChanged()){
            int x = (int) (camera.getViewPortLeft() * camera.getWidth());
            int y = (int) (camera.getViewPortBottom() * camera.getHeight());
            int w = (int) ((camera.getViewPortRight() - camera.getViewPortLeft()) * camera.getWidth());
            int h = (int) ((camera.getViewPortTop() - camera.getViewPortBottom()) * camera.getHeight());
            glViewport(x, y, w, h);

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
                //logger.warning("Uniform "+uniform.getName()+" is not declared in shader.");
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
            case Matrix2:
                fb = (FloatBuffer)uniform.getValue();
                assert fb.remaining() == 4;
                glUniformMatrix2(loc, false, fb);
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

    public void updateWorldParameters(EnumMap<UniformBinding, Uniform> params){
        // assums worldMatrix is properly set.
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();
        Matrix4f viewProjMatrix = camera.getViewProjectionMatrix();

        Matrix4f tempMat4 = TempVars.get().tempMat4;
        Matrix3f tempMat3 = TempVars.get().tempMat3;

        for (Map.Entry<UniformBinding, Uniform> param  : params.entrySet()){
            Uniform u = param.getValue();
            switch (param.getKey()){
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
                    tempMat4.loadIdentity();
                    tempMat4.set(viewMatrix);
                    tempMat4.multLocal(worldMatrix);
                    u.setMatrix4(tempMat4);
                    break;
                case NormalMatrix:
                    tempMat4.loadIdentity();
                    tempMat4.set(viewMatrix);
                    tempMat4.multLocal(worldMatrix);
                    tempMat4.toRotationMatrix(tempMat3);
                    // TODO: NormalMatrix = transpose(mat3(g_WorldViewMatrix))??
                    tempMat3.invertLocal();
                    tempMat3.transposeLocal();
                    
                    u.setMatrix3(tempMat3);
                    break;
                case WorldViewProjectionMatrix:
                    tempMat4.loadIdentity();
                    tempMat4.set(viewProjMatrix);
                    tempMat4.multLocal(worldMatrix);
                    u.setMatrix4(tempMat4);
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
                throw new RuntimeException("Unknown shader type.");
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
        // convert string to bytebuffer
        ByteBuffer sourceBuf = BufferUtils.createByteBuffer(source.getSource());
        glShaderSource(id, sourceBuf);
        glCompileShader(id);

        IntBuffer temp = TempVars.get().intBuffer1;
        glGetShader(id, GL_COMPILE_STATUS, temp);
        boolean compiledOK = temp.get(0) == GL_TRUE;

        // check for errors, etc
        glGetShader(id, GL_INFO_LOG_LENGTH, temp);
        int length = temp.get(0);
        if (length > 0){
            // get infos
            ByteBuffer logBuf = BufferUtils.createByteBuffer(length);
            glGetShaderInfoLog(id, null, logBuf);

            // convert to string, etc
            byte[] logBytes = new byte[length];
            logBuf.get(logBytes, 0, length);
            String infoLog = new String(logBytes);
            if (compiledOK){
                // we dont care much about the info log if all still compiled..
                // send as FINE
                logger.fine(source.getType().name()+" compile success: "+infoLog);
            }else{
                // send as WARNING
                logger.warning(source.getType().name()+" compile error: "+infoLog);
            }
        }else if (!compiledOK){
            logger.warning(source.getType().name()+" compile error: ?");
        }else{
            logger.fine(source.getType().name()+" compile success");
        }

        // only usable if compiled
        source.setUsable(compiledOK);
        if (!compiledOK){
            // make sure to dispose id cause all program's
            // shaders will be cleared later.
            glDeleteShader(id);
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
            if (source.getId() == -1){
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

        // XXX: Apparently VALIDATE_STATUS is only for use with
        // glValidateProgram call.
//        glGetProgram(id, GL_VALIDATE_STATUS, temp);
//        boolean validateOK = temp.get(0) == GL_TRUE;

        glGetProgram(id, GL_INFO_LOG_LENGTH, temp);
        int length = temp.get(0);

        if (length > 0){
            // get infos
            ByteBuffer logBuf = BufferUtils.createByteBuffer(length);
            glGetProgramInfoLog(id, null, logBuf);

            // convert to string, etc
            byte[] logBytes = new byte[length];
            logBuf.get(logBytes, 0, length);
            String infoLog = new String(logBytes);

            if (linkOK){
                // we dont care much about the info log if all still compiled..
                // send as FINE
                logger.fine("shader link success: "+infoLog);
            }else{
                // send as WARNING
                logger.warning("shader link failure: "+infoLog);
            }
        }else if (linkOK){
            logger.fine("shader link success");
        }else if (!linkOK){
            logger.fine("shader link failure");
        }

        shader.clearUpdateNeeded();
        if (!linkOK){
            // failure.. forget about everything
            shader.resetSources();
            shader.setUsable(false);
            deleteShader(shader);
        }else{
            shader.setUsable(true);
            shader.clearUpdateNeeded();
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
            
            if (!shader.isUsable())
                return;

//            updatePredefinedUniforms(shader);
            updateShaderUniforms(shader);
            if (context.boundShaderProgram != shader.getId()){
                glUseProgram(shader.getId());
                context.boundShaderProgram = shader.getId();
                boundShader = shader;
            }
        }
    }

    public void deleteShader(Shader shader){
        if (shader.getId() == -1){
            logger.warning("Shader is not uploaded to GPU, cannot delete.");
            return;
        }
        for (ShaderSource source : shader.getSources()){
            if (source.getId() != -1){
                glDetachShader(shader.getId(), source.getId());
                glDeleteShader(source.getId());
            }
        }
        glDeleteProgram(shader.getId());
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
            glTexParameterf(target,
                            EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                            tex.getAnisotropicFilter());
        }
        // repeat modes
        switch (tex.getType()){
            case ThreeDimensional:
                glTexParameteri(target, GL_TEXTURE_WRAP_R, convertWrapMode(tex.getWrap(WrapAxis.R)));
            case TwoDimensional:
                glTexParameteri(target, GL_TEXTURE_WRAP_T, convertWrapMode(tex.getWrap(WrapAxis.T)));
                // fall down here is intentional..
            case OneDimensional:
                glTexParameteri(target, GL_TEXTURE_WRAP_S, convertWrapMode(tex.getWrap(WrapAxis.S)));
                break;
            default:
                throw new UnsupportedOperationException("Unknown texture type: "+tex.getType());
        }

        Image img = tex.getImage();
        if (img != null){
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            if (!img.hasMipmaps() && tex.getMinFilter().usesMipMapLevels()){
                // No pregenerated mips available,
                // generate from base level if required
                glTexParameteri(target, GL_GENERATE_MIPMAP, GL_TRUE);
            }
            TextureUtil.uploadTexture(img, target, 0);
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

            glActiveTexture(GL_TEXTURE0 + idx);
            glDisable(convertTextureType(textures[idx].getType()));

            //System.out.println("Disabled TEX UNIT: "+idx);
            textures[idx] = null;
        }
        context.textureIndexList.copyNewToOld();
        
//        Texture[] boundTextures = context.boundTextures;
//        for (int i = 0; i < boundTextures.length; i++){
//            if (boundTextures[i] != null){
//                glActiveTexture(GL_TEXTURE0 + i);
//                int type = convertTextureType(boundTextures[i].getType());
//                glDisable(type);
//                glBindTexture(type, 0);
//                boundTextures[i] = null;
//            }
//        }
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
                return ARBHalfFloatVertex.GL_HALF_FLOAT;
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
//            if (bufId == context.boundArrayVBO){
//                glBindBuffer(GL_ARRAY_BUFFER, 0);
//                context.boundArrayVBO = 0;
//            }
//            if (bufId == context.boundElementArrayVBO){
//                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
//                context.boundElementArrayVBO = 0;
//            }
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
//        int index;
//        if (vb.getType() == VertexBuffer.Type.MiscAttrib){
//            // must alloc a new index from the render context.
//            index = context.allocAttribIndex();
//        }else{
//            index = vb.getType().getIndex();
//        }

        //glVertexAttribPointer(index, vb.getNumComponents(), convertFormat(vb.getFormat()), false, 0, 0);

//        glEnable(GL_VERTEX_ARRAY);
//        if (vb.getType() == Type.Position){
//            glVertexPointer(vb.getNumComponents(), convertFormat(vb.getFormat()), 0, 0);
//        }

        //context.boundAttribs[index] = vb;
    }

    public void drawTriangleList(VertexBuffer indexBuf, int count){
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
//            glDrawElementsInstancedARB(GL_TRIANGLES,
//                                       indexBuf.getData().capacity(),
//                                       convertFormat(indexBuf.getFormat()),
//                                       0,
//                                       count);
//        }else{
            glDrawElements(GL_TRIANGLES,
                           indexBuf.getData().capacity(),
                           convertFormat(indexBuf.getFormat()),
                           0);
//        }
//        Util.checkGLError();
    }

    public void drawTriangleStrip(int length){
        glDrawArrays(GL_TRIANGLE_STRIP, 0, length);
        Util.checkGLError();
    }

    /*********************************************************************\
    |* Render Calls                                                      *|
    \*********************************************************************/
    public void renderQueue(){
        queue.renderQueue(Bucket.Opaque);
    }

    public void addToQueue(Geometry geom, RenderQueue.Bucket bucket){
        queue.addToQueue(geom, bucket);
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
        if (indices == null){
            logger.warning("Index buffer not specified for mesh.");
            return;
        }

        drawTriangleList(indices, count);
        clearVertexAttribs();
        clearTextureUnits();
    }

    public void updateVertexArray(Mesh mesh){
        int id = mesh.getId();
        if (id == -1){
            IntBuffer temp = TempVars.get().intBuffer1;
            ARBVertexArrayObject.glGenVertexArrays(temp);
            id = temp.get(0);
            mesh.setId(id);
        }

        if (context.boundVertexArray != id){
            ARBVertexArrayObject.glBindVertexArray(id);
            context.boundVertexArray = id;
        }

        for (VertexBuffer vb : mesh.getBuffers()){
            if (vb.getBufferType() != Type.Index){
                setVertexAttrib(vb);
            }
        }

//        ARBVertexArrayObject.glBindVertexArray(0);
//        clearVertexAttribs();
    }

    public void renderGeometry(Geometry geom){
        setWorldMatrix(geom.getWorldMatrix());
        if (geom.getMaterial() == null){
            logger.warning("Unable to render geometry "+geom+". No material defined!");
            return;
        }
        geom.getMaterial().apply(geom, this);

        Mesh mesh = geom.getMesh();
        if (GLContext.getCapabilities().GL_ARB_vertex_array_object){
            if (mesh.getId() == -1){
                updateVertexArray(mesh);
            }

            if (context.boundVertexArray != mesh.getId()){
                ARBVertexArrayObject.glBindVertexArray(mesh.getId());
                context.boundVertexArray = mesh.getId();
            }

            drawTriangleList(mesh.getBuffer(Type.Index), 1);
            clearTextureUnits();
        }else{
            renderMesh(geom.getMesh(), 1);
        }
    }

//    @Override
//    public void renderMesh(Mesh mesh) {
//        VertexBuffer indices = null;
//        for (VertexBuffer vb : mesh.getBuffers()){
//            if (vb.isUpdateNeeded()){
//                // update buffer
//                updateBufferData(vb);
//                assert vb.getId() >= 0;
//            }
//
//            if (vb.getType() == Type.Index){
//                indices = vb;
//            }else{
//                if (context.boundArrayVBO != vb.getId()){
//                    glBindBuffer(GL_ARRAY_BUFFER, vb.getId());
//                    context.boundArrayVBO = vb.getId();
//                }
//                glEnableClientState(GL_VERTEX_ARRAY);
//                // no stride, buffer offset = 0
//                glVertexPointer(vb.getNumComponents(), convertFormat(vb.getFormat()), 0, 0);
//            }
//        }
//        if (indices == null){
//            logger.warning("Index buffer not specified for mesh.");
//            return;
//        }
//
//        if (context.boundElementArrayVBO != indices.getId()){
//            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices.getId());
//            context.boundElementArrayVBO = indices.getId();
//        }
//
//        glDrawElements(GL_TRIANGLES, indices.getData().capacity(), convertFormat(indices.getFormat()), 0);
//        glDisableClientState(GL_VERTEX_ARRAY);
//
//        // check if any GL errors occured.
//        Util.checkGLError();
//    }
}
