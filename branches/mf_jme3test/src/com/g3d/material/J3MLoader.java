package com.g3d.material;

import com.g3d.material.MaterialDef.MatParam;
import com.g3d.material.MaterialDef.MatParamType;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.res.ContentKey;
import com.g3d.res.ContentLoader;
import com.g3d.res.ContentManager;
import com.g3d.shader.Shader.ShaderType;
import com.g3d.shader.ShaderMasterKey;
import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture2D;
import com.g3d.util.BufferUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Scanner;

public class J3MLoader implements ContentLoader {

    private ContentManager owner;
    private Scanner scan;

    private MaterialDef materialDef;
    private Material material;
    private TechniqueDef technique;
    private RenderState renderState;

    private String shaderLang;
    private String vertName;
    private String fragName;

    public J3MLoader(){
    }

    private void throwIfNequal(String expected, String got) throws IOException {
        if (expected == null)
            throw new IOException("Expected a statement, got '"+got+"'!");

        if (!expected.equals(got))
            throw new IOException("Expected '"+expected+"', got '"+got+"'!");
    }

    private void nextStatement(){
        while (true){
            if (scan.hasNext("\\}")){
                break;
            }else if (scan.hasNext("[\n;]")){
                scan.next();
            }else if (scan.hasNext("//")){
                scan.useDelimiter("\n");
                scan.next();
                scan.useDelimiter("\\p{javaWhitespace}+");
            }else{
                break;
            }
        }
    }

    private String readString(String end){
        scan.useDelimiter(end);
        String str = scan.next();
        scan.useDelimiter("\\p{javaWhitespace}+");
        return str.trim();
    }

    private Image createColorTexture(ColorRGBA color){
        if (color.getAlpha() == 1.0f){
            // create RGB texture
            ByteBuffer data = BufferUtils.createByteBuffer(3);
            byte[] bytes = color.asBytesRGBA();
            data.put(bytes[0]).put(bytes[1]).put(bytes[2]);
            data.flip();

            return new Image(Format.RGB8, 1, 1, data);
        }else{
            // create RGBA texture
            ByteBuffer data = BufferUtils.createByteBuffer(4);
            data.putInt(color.asIntRGBA());
            data.flip();

            return new Image(Format.RGBA8, 1, 1, data);
        }
    }

    private void readShaderStatement(ShaderType type) throws IOException {
        String lang = readString(":");

        String word = scan.next();
        throwIfNequal(":", word);

        word = readString("[\n;(\\})]"); // new line, semicolon, comment or brace will end a statement
        // locate source code

        if (type == ShaderType.Vertex)
            vertName = word;
        else if (type == ShaderType.Fragment)
            fragName = word;

        shaderLang = lang;
    }

    private void readParam() throws IOException{
        String word = scan.next();
        MaterialDef.MatParamType type = MaterialDef.MatParamType.valueOf(word);
        
        word = readString("[\n;(//)(\\})]");
        materialDef.addMaterialParam(type, word);
    }

    private void readValueParam() throws IOException{
        String name = readString(":");
        throwIfNequal(":", scan.next());

        // parse value
        MatParam p = material.getMaterialDef().getMaterialParam(name);
        if (p == null)
            throw new IOException("The material parameter: "+name+" is undefined.");

        MatParamType type = p.getType();
        if (type.isTextureType()){
            String texturePath = readString("[\n;(//)(\\})]");
            Image img;
            if (texturePath.startsWith("color")){
                texturePath = texturePath.substring(5).trim();
                String[] split = texturePath.split(" ");
                if (split.length == 4){
                    img = createColorTexture(new ColorRGBA(Float.parseFloat(split[0]),
                                                           Float.parseFloat(split[1]),
                                                           Float.parseFloat(split[2]),
                                                           Float.parseFloat(split[3])));
                }else if (split.length == 3){
                    img = createColorTexture(new ColorRGBA(Float.parseFloat(split[0]),
                                                           Float.parseFloat(split[1]),
                                                           Float.parseFloat(split[2]),
                                                           1.0f));
                }else{
                    throw new IOException("Expected 3 or 4 floats, got '"+texturePath+"'");
                }
            }else{
                img =  owner.loadImage(texturePath);
            }

            // parse texture
            if (type == MatParamType.Texture2D){
                material.setTextureParam(name, type, new Texture2D(img));
            }else if (type == MatParamType.TextureCubeMap){
                material.setTextureParam(name, type, new Texture2D(img));
            }
        }else{
            switch (type){
                case Float:
                    material.setParam(name, type, scan.nextFloat());
                    break;
                case Vector2:
                    material.setParam(name, type, new Vector2f(scan.nextFloat(),
                                                         scan.nextFloat()));
                    break;
                case Vector3:
                    material.setParam(name, type, new Vector3f(scan.nextFloat(),
                                                         scan.nextFloat(),
                                                         scan.nextFloat()));
                    break;
                case Vector4:
                    material.setParam(name, type, new ColorRGBA(scan.nextFloat(),
                                                          scan.nextFloat(),
                                                          scan.nextFloat(),
                                                          scan.nextFloat()));
                    break;
                case Int:
                    material.setParam(name, type, scan.nextInt());
                    break;
                case Boolean:
                    material.setParam(name, type, scan.nextBoolean());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown type: "+p.getType());
            }
        }
    }

    private void readMaterialParams() throws IOException{
        nextStatement();

        String word = scan.next();
        throwIfNequal("{", word);

        nextStatement();

        while (true){
            if (scan.hasNext("\\}")){
                scan.next();
                break;
            }

            readParam();
            nextStatement();
        }
    }

    private void readExtendingMaterialParams() throws IOException{
        nextStatement();

        String word = scan.next();
        throwIfNequal("{", word);

        nextStatement();

        while (true){
            if (scan.hasNext("\\}")){
                scan.next();
                break;
            }

            readValueParam();
            nextStatement();
        }
    }

    private void readWorldParams() throws IOException{
        nextStatement();

        String word = scan.next();
        throwIfNequal("{", word);

        nextStatement();

        while (true){
            if (scan.hasNext("\\}")){
                scan.next();
                break;
            }

            word = readString("[\n;(//)(\\})]");
            if (word != null && !word.equals("")){
                technique.addWorldParam(word);
            }
            nextStatement();
        }
    }

    private boolean parseBoolean(String word){
        return word != null && word.equals("On");
    }

    private void readRenderStateStatement() throws IOException{
        String word = scan.next();
        if (word.equals("Wireframe")){
            renderState.wireframe = parseBoolean(scan.next());
        }else if (word.equals("FaceCull")){
            renderState.cullMode = RenderState.FaceCullMode.valueOf(scan.next());
        }else if (word.equals("DepthWrite")){
            renderState.depthWrite = parseBoolean(scan.next());
        }else if (word.equals("DepthTest")){
            renderState.depthTest = parseBoolean(scan.next());
        }else if (word.equals("Blend")){
            renderState.blendMode = RenderState.BlendMode.valueOf(scan.next());
        }else if (word.equals("AlphaTestFalloff")){
            renderState.alphaTest = true;
            renderState.alphaFallOff = scan.nextFloat();
        }else if (word.equals("PolyOffset")){
            float factor = scan.nextFloat();
            float units = scan.nextFloat();
            renderState.setPolyOffset(factor, units);
        }else if (word.equals("ColorWrite")){
            renderState.colorWrite = parseBoolean(scan.next());
        }else{
            throwIfNequal(null, word);
        }
    }

    private void readRenderState() throws IOException{
        nextStatement();

        String word = scan.next();
        throwIfNequal("{", word);

        nextStatement();

        renderState = new RenderState();

        while (true){
            if (scan.hasNext("\\}")){
                scan.next();
                break;
            }

            readRenderStateStatement();
            nextStatement();
        }
        
        technique.setRenderState(renderState);
        renderState = null;
    }
    
    private void readDefine(){
        // stops at either next statement or colon
        // ways to end a statement:
        /*
        Block {
            Statement<new line>
            Statement;
            Statement //comment
            Statement }
        */
        String defineName = readString("[\n;:(//)(\\})]");
        if (defineName.equals(""))
            return;

        String matParamName = null;
        if (scan.hasNext(":")){
            scan.next();
            // this time without colon
            matParamName = readString("[\n;(//)(\\})]");
            // add define <-> param mapping
            technique.addShaderParamDefine(matParamName, defineName);
        }else{
            // add preset define
            technique.addShaderPresetDefine(defineName, "1");
        }
    }

    private void readDefines() throws IOException{
        nextStatement();

        String word = scan.next();
        throwIfNequal("{", word);

        nextStatement();

        while (true){
            if (scan.hasNext("\\}")){
                scan.next();
                break;
            }

            readDefine();
            nextStatement();
        }

    }

//    private void readAttributes() throws IOException{
//        nextStatement();
//
//        String word = scan.next();
//        throwIfNequal("{", word);
//
//        nextStatement();
//
//        while (true){
//            if (scan.hasNext("\\}")){
//                scan.next();
//                break;
//            }
//
//            Param p = readParam(true);
//            technique.addAttribute(p);
//            nextStatement();
//        }
//    }

    private void readTechniqueStatement() throws IOException{
        String word = scan.next();
        if (word.equals("VertexShader")){
            readShaderStatement(ShaderType.Vertex);
        }else if (word.equals("FragmentShader")){
            readShaderStatement(ShaderType.Fragment);
        }else if (word.equals("UseLighting")){
            technique.setUsesLighting(true);
        }else if (word.equals("WorldParameters")){
            readWorldParams();
        }else if (word.equals("RenderState")){
            readRenderState();
        }else if (word.equals("Defines")){
            readDefines();
//        }else if (word.equals("Attributes")){
//            readAttributes();
        }else{
            throwIfNequal(null, word);
        }
        nextStatement();
    }

    private void readTechnique() throws IOException{
        String name = null;
        if (!scan.hasNext("\\{")){
            name = scan.next();
        }
        technique = new TechniqueDef(name);

        String word = scan.next();
        throwIfNequal("{", word);

        nextStatement();

        while (true){
            if (scan.hasNext("\\}")){
                scan.next();
                break;
            }

            readTechniqueStatement();
        }

        if (vertName != null && fragName != null){
            technique.setShaderFile(vertName, fragName, shaderLang);
        }
        
        materialDef.addTechniqueDef(technique);
        technique = null;
        vertName = null;
        fragName = null;
        shaderLang = null;
    }

    public Object load(ContentManager owner, InputStream stream, String extension, ContentKey key) throws IOException {
        this.owner = owner;
        load(stream);
        if (material != null){
            // material implementation
            return material;
        }else{
            // material definition
            return materialDef;
        }
    }

    public void load(InputStream in) throws IOException{
        scan = new Scanner(in);
        scan.useLocale(Locale.US);

        nextStatement();

        boolean extending = false;
        String name = null;
        String word = scan.next();
        if (word.equals("Material")){
            extending = true;
        }else if (word.equals("MaterialDef")){
            extending = false;
        }else{
            throw new IOException("Specified file is not a Material file");
        }

        nextStatement();

        word = readString("[(\\{)(//)\n:]");
        if (word == null || word.equals(""))
            throw new IOException("Material name cannot be empty");

        name = word;

        nextStatement();

        if (scan.hasNext(":")){
            if (!extending){
                throw new IOException("Must use 'Material' when extending.");
            }

            scan.next(); // skip colon
            String extendedMat = readString("\\{");

            MaterialDef def = owner.loadMaterialDef(extendedMat);
            if (def == null)
                throw new IOException("Extended material "+extendedMat+" cannot be found.");

            material = new Material(def);
        }else if (scan.hasNext("\\{")){
            if (extending){
                throw new IOException("Expected ':', got '{'");
            }
            materialDef = new MaterialDef(owner,name);
        }
        scan.next(); // skip {

        nextStatement();

        while (true){
            if (scan.hasNext("\\}")){
                scan.next();
                break;
            }

            word = scan.next();
            if (extending){
                if (word.equals("MaterialParameters")){
                    readExtendingMaterialParams();
                    nextStatement();
                }
            }else{
                if (word.equals("Technique")){
                    readTechnique();
                    nextStatement();
                }else if (word.equals("MaterialParameters")){
                    readMaterialParams();
                    nextStatement();
                }else{
                    throw new IOException("Expected material statement, got '"+scan.next()+"'");
                }
            }
        }
    }

}
