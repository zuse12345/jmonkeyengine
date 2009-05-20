package com.g3d.shader;

import com.g3d.renderer.GLObject;
import com.g3d.renderer.Renderer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shader extends GLObject {

    public static final Shader DEFAULT_GLSL = new Shader("GLSL100");

    static {
        DEFAULT_GLSL.addSource(ShaderType.Vertex,
                          "uniform mat4 g_WorldViewProjectionMatrix;\n" +
                          "in vec4 inPosition;\n" +
                          "\n" +
                          "void main(){\n" +
                          "    gl_Position = g_WorldViewProjectionMatrix * inPosition;\n" +
                          "}\n");
        DEFAULT_GLSL.addSource(ShaderType.Fragment,
                          "void main(){\n" +
                          "   gl_FragColor = vec4(1.0);\n" +
                          "}\n");
    }

    private String language;

    /**
     * True if the shader is fully compiled & linked.
     * (e.g no GL error will be invoked if used).
     */
    private boolean usable = false;

    /**
     * A list of all shaders currently attached.
     */
    private List<ShaderSource> shaderList = new ArrayList<ShaderSource>();

    /**
     * Maps uniform name to the uniform variable.
     */
    private Map<String, Uniform> uniforms = new HashMap<String, Uniform>();

    /**
     * Maps attribute name to the location of the attribute in the shader.
     */
    private Map<String, Attribute> attribs = new HashMap<String, Attribute>();

    /**
     * Type of shader. The shader will control the pipeline of it's type.
     */
    public static enum ShaderType {
        /**
         * Control fragment rasterization. (e.g color of pixel).
         */
        Fragment,

        /**
         * Control vertex processing. (e.g transform of model to clip space)
         */
        Vertex,

        /**
         * Control geometry assembly. (e.g compile a triangle list from input data)
         */
        Geometry;
    }

    /**
     * Shader source describes a shader object in OpenGL. Each shader source
     * is assigned a certain pipeline which it controls (described by it's type).
     */
    public class ShaderSource {

        final ShaderType shaderType;
//        final Shader parent;

        boolean usable = false;
        String source = null;
//        String name = "Untitled";

        int id = -1;

        public ShaderSource(Shader parent, ShaderType type){
            this.shaderType = type;
            if (type == null)
                throw new NullPointerException("The shader type must be specified");
//            this.parent = parent;
        }

        public int getId(){
            return id;
        }

        public void setId(int id){
            this.id = id;
        }

        public ShaderType getType() {
            return shaderType;
        }

        public void setSource(String source){
            if (source == null)
                throw new NullPointerException("Shader source cannot be null");

            this.source = source;
            updateNeeded = true;
        }

//        public void setName(String name){
//            this.name = name;
//        }

        public String getSource(){
            return source;
        }
        
        public boolean isUsable(){
            return usable;
        }

        public void setUsable(boolean usable){
            this.usable = usable;
        }
        
        public void reset(){
            id = -1;
            usable = false;
        }
    }

    /**
     * Create an empty shader.
     */
    public Shader(String language){
        super(Type.Shader);
        this.language = language;
    }

    /**
     * Adds source code to a certain pipeline.
     *
     * @param type The pipeline to control
     * @param source The shader source code (in GLSL).
     */
    public void addSource(ShaderType type, String source){
        ShaderSource shader = new ShaderSource(this, type);

        shader.setSource(source);
        shaderList.add(shader);
        updateNeeded = true;
    }

    public Uniform getUniform(String name){
        Uniform uniform = uniforms.get(name);
        if (uniform == null){
            uniform = new Uniform();
            uniform.name = name;
            uniforms.put(name, uniform);
        }
        return uniform;
    }

    public Attribute getAttribute(String name){
        Attribute attrib = attribs.get(name);
        if (attrib == null){
            attrib = new Attribute();
            attrib.name = name;
            attribs.put(name, attrib);
        }
        return attrib;
    }

    public Collection<Uniform> getUniforms(){
        return uniforms.values();
    }

    public Collection<ShaderSource> getSources(){
        return shaderList;
    }

    /**
     * Clears all sources. Assuming that they have already been detached and
     * removed on the GL side.
     */
    public void resetSources(){
        shaderList.clear();
    }

    /**
     * Returns true if this program and all it's shaders have been compiled,
     * linked and validated successfuly.
     * @return
     */
    public boolean isUsable(){
        return usable;
    }

    /**
     * Sets if the program can be used. Should only be called by the Renderer.
     * @param usable
     */
    public void setUsable(boolean usable){
        this.usable = usable;
    }

    /**
     * Called by the object manager to reset all object IDs. This causes
     * the shader to be reuploaded to the GPU incase the display was restarted.
     * @param r
     */
    @Override
    public void resetObject() {
        this.id = -1;
        this.usable = false;
        setUpdateNeeded();
        for (ShaderSource source : shaderList){
            source.id = -1;
            source.usable = false;
        }
        for (Uniform uniform : uniforms.values()){
            uniform.location = -1;
        }
        for (Attribute attrib : attribs.values()){
            attrib.location = -1;
        }
    }

    @Override
    public void deleteObject(Renderer r) {
        r.deleteShader(this);
    }

}
