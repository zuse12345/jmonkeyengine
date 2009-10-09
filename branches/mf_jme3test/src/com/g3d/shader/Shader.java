package com.g3d.shader;

import com.g3d.renderer.GLObject;
import com.g3d.renderer.Renderer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shader extends GLObject {

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
    public class ShaderSource extends GLObject {

        final ShaderType shaderType;

        boolean usable = false;
        String name = null;
        String source = null;
        String defines = null;

        public ShaderSource(ShaderType type){
            super(Type.ShaderSource);
            this.shaderType = type;
            if (type == null)
                throw new NullPointerException("The shader type must be specified");
        }
        
        protected ShaderSource(ShaderSource ss){
            super(Type.ShaderSource, ss.id);
            this.shaderType = ss.shaderType;
            usable = false;
            name = ss.name;
            // forget source & defines
        }

        public void setName(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }

        public ShaderType getType() {
            return shaderType;
        }

        public void setSource(String source){
            if (source == null)
                throw new NullPointerException("Shader source cannot be null");

            this.source = source;
            setUpdateNeeded();
        }

        public void setDefines(String defines){
            if (defines == null)
                throw new NullPointerException("Shader defines cannot be null");

            this.defines = defines;
            setUpdateNeeded();
        }

        public String getSource(){
            return source;
        }

        public String getDefines(){
            return defines;
        }
        
        public boolean isUsable(){
            return usable;
        }

        public void setUsable(boolean usable){
            this.usable = usable;
        }

        @Override
        public String toString(){
            String nameTxt = "";
            if (name != null)
                nameTxt = "name="+name+", ";
            if (defines != null)
                nameTxt += "defines, ";
            

            return getClass().getSimpleName() + "["+nameTxt+"type="
                                              + shaderType.name()+"]";
        }

        public void resetObject(){
            id = -1;
            usable = false;
            setUpdateNeeded();
        }

        public void deleteObject(Renderer r){
            r.deleteShaderSource(this);
        }

        public GLObject createDestructableClone(){
            return new ShaderSource(this);
        }
    }

    /**
     * Create an empty shader.
     */
    public Shader(String language){
        super(Type.Shader);
        this.language = language;
    }

    protected Shader(Shader s){
        super(Type.Shader, s.id);
        for (ShaderSource source : shaderList){
            this.addSource((ShaderSource) source.createDestructableClone());
        }
    }

    /**
     * Creates a deep clone of the shader, where the sources are available
     * but have not been compiled yet. Does not copy the uniforms or attribs.
     * @return
     */
//    public Shader createDeepClone(String defines){
//        Shader newShader = new Shader(language);
//        for (ShaderSource source : shaderList){
//            if (!source.getDefines().equals(defines)){
//                // need to clone the shadersource so
//                // the correct defines can be placed
//                ShaderSource newSource = new ShaderSource(source.getType());
//                newSource.setSource(source.getSource());
//                newSource.setDefines(defines);
//                newShader.addSource(newSource);
//            }else{
//                // no need to clone source, also saves
//                // having to compile the shadersource
//                newShader.addSource(source);
//            }
//        }
//        return newShader;
//    }

    /**
     * Adds source code to a certain pipeline.
     *
     * @param type The pipeline to control
     * @param source The shader source code (in GLSL).
     */
    public void addSource(ShaderType type, String name, String source, String defines){
        ShaderSource shader = new ShaderSource(type);
        shader.setSource(source);
        shader.setName(name);
        if (defines != null)
            shader.setDefines(defines);
        
        shaderList.add(shader);
        setUpdateNeeded();
    }

    public void addSource(ShaderType type, String source, String defines){
        addSource(type, null, source, defines);
    }

    public void addSource(ShaderType type, String source){
        addSource(type, source, null);
    }

    /**
     * Adds an existing shader source to this shader.
     * @param source
     */
    public void addSource(ShaderSource source){
        shaderList.add(source);
        setUpdateNeeded();
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

    public Collection<Attribute> getAttributes() {
        return attribs.values();
    }

    public Collection<ShaderSource> getSources(){
        return shaderList;
    }

    public String toString(){
        return getClass().getSimpleName() + "[language="+language
                                           + ", numSources="+shaderList.size()
                                           + ", numUniforms="+uniforms.size()+"]";
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
     * Usually called when the shader itself changes or during any
     * time when the var locations need to be refreshed.
     */
    public void resetLocations(){
        // NOTE: Shader sources will be reset seperately from the shader itself.
        for (Uniform uniform : uniforms.values()){
            // fixed mistake: was -1 which was incorrect
            // would cause shader to not work after reset
            uniform.location = -2;
        }
        for (Attribute attrib : attribs.values()){
            attrib.location = -2;
        }
    }

    @Override
    public void setUpdateNeeded(){
        super.setUpdateNeeded();
        resetLocations();
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
        resetLocations();
    }

    @Override
    public void deleteObject(Renderer r) {
        r.deleteShader(this);
    }

    public GLObject createDestructableClone(){
        return new Shader(this);
    }

}
