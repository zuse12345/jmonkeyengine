package com.g3d.scene.plugins.ogre;

import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetLoader;
import com.g3d.asset.AssetManager;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture.WrapMode;
import com.g3d.texture.TextureCubeMap;
import java.io.IOException;
import java.util.Scanner;

public class MaterialLoader implements AssetLoader {

    private AssetManager assetManager;
    private Scanner scan;
    private ColorRGBA diffuse, specular;
    private Texture texture;
    private String texName;
    private float shinines;

    private String readString(String end){
        scan.useDelimiter(end);
        String str = scan.next();
        scan.useDelimiter("\\p{javaWhitespace}+");
        return str.trim();
    }

    private ColorRGBA readColor(){
        ColorRGBA color = new ColorRGBA();
        color.r = scan.nextFloat();
        color.g = scan.nextFloat();
        color.b = scan.nextFloat();
        if (scan.hasNextFloat()){
            color.a = scan.nextFloat();
        }
        return color;
    }

    private void readTextureImage(){
        // texture image def
        String path = scan.next();
        String mips = null;
        String type = null;
        if (!scan.hasNext("\n")){
            // more params
            type = scan.next();
            if (!scan.hasNext("\n")){
                mips = scan.next();
                if (!scan.hasNext("\n")){
                    // even more params..
                    // will have to ignore
                    readString("\n");
                }
            }
        }

        boolean genMips = true;
        if (type != null && type.equals("0"))
            genMips = false;

        texture = assetManager.loadTexture(path, genMips, false, 0);
        if (type != null){
            if (type.equals("cubic")){
                texture = new TextureCubeMap(texture.getImage());
            }
        }
        texture.setWrap(WrapMode.Repeat);
        if (texName != null){
            texture.setName(texName);
            texName = null;
        }
    }

    private void readTextureUnitStatement(){
        String keyword = scan.next();
        if (keyword.equals("texture")){
            readTextureImage();
        }else if (keyword.equals("texture_alias")){
            texture.setName(scan.next());
        }else if (keyword.equals("tex_address_mode")){
            String mode = scan.next();
            if (mode.equals("wrap")){
                texture.setWrap(WrapMode.Repeat);
            }else if (mode.equals("clamp")){
                texture.setWrap(WrapMode.Clamp);
            }else if (mode.equals("mirror")){
                texture.setWrap(WrapMode.MirroredRepeat);
            }else if (mode.equals("border")){
                texture.setWrap(WrapMode.BorderClamp);
            }
        }else if (keyword.equals("filtering")){
            // ignored.. only anisotrpy is considered
            readString("\n");
        }else if (keyword.equals("max_anisotropy")){
            int amount = scan.nextInt();
            texture.setAnisotropicFilter(amount);
        }
    }

    private void readTextureUnit(){
        // name is optional
        if (!scan.hasNext("\\{")){
            texName = readString("\\{");
        }else{
            texName = null;
        }
        scan.next(); // skip "{"
        while (!scan.hasNext("\\}")){
            readTextureUnitStatement();
        }
        scan.next(); // skip "}"
    }

    private void readPassStatement(){
        // read until newline
        String keyword = scan.next();
        if (keyword.equals(""))
            return;

        if (keyword.equals("diffuse")){
            diffuse = readColor();
        }else if (keyword.equals("specular")){
            specular = new ColorRGBA();
            specular.r = scan.nextFloat();
            specular.g = scan.nextFloat();
            specular.b = scan.nextFloat();
            float unknown = scan.nextFloat();
            if (scan.hasNextFloat()){
                // using 5 float values
                specular.a = unknown;
                shinines = scan.nextFloat();
            }else{
                // using 4 float values
                specular.a = 1f;
                shinines = unknown;
            }
        }else if (keyword.equals("texture_unit")){
            readTextureUnit();
        }
    }

    private void readPass(){
        scan.next(); // skip "pass"
        // name is optional
        String name;
        if (scan.hasNext("\\{")){
            // no name
            name = null;
        }else{
            name = readString("\\{");
        }
        scan.next(); // skip "{"
        while (!scan.hasNext("\\}")){
            readPassStatement();
        }
        scan.next(); // skip "}"
    }

    private void readTechnique(){
        scan.next(); // skip "technique"
        // name is optional
        String name;
        if (scan.hasNext("\\{")){
            // no name
            name = null;
        }else{
            name = readString("\\{");
        }
        scan.next(); // skip "{"
        while (scan.hasNext("pass")){
            readPass();
        }
        scan.next(); // skip "}"
    }

    private String readMaterial(){
        scan.next(); // skip "material"
        // read name
        String name = readString("\\{");
        scan.next(); // skip "{"
        while (scan.hasNext("technique")){
            readTechnique();
        }
         scan.next(); // skip "}"
        return name;
    }

    private Material compileMaterial(int i){
//        Material mat = new Material(assetManager, "plain_texture.j3md");
//        Material mat = new Material(assetManager, "phong_lighting.j3md");
        //TODO hack, how to define materials ? within ogre file ? meta-file ? implicitly with naming conventions (-> not as powerful) ?
        //  assuming for now, that the only bump-mapped tex is the first tex
        Material mat;
        if (i == 0)
            mat = assetManager.loadMaterial("elephant.j3m");//new Material(assetManager, "phong_lighting.j3md");
        else
        {
            mat = new Material(assetManager, "plain_texture.j3md");
            mat.setTexture("m_ColorMap", texture);
        }
//        mat.setTexture("m_DiffuseMap", texture);
        diffuse = null;
        specular = null;
        texture = null;
        shinines = 0f;
        return mat;
    }

    public Object load(AssetInfo info) throws IOException {
        assetManager = info.getManager();
        OgreMaterialList list = new OgreMaterialList();
        scan = new Scanner(info.openStream());
        int ind = 0;
        while (scan.hasNext("material")){
            String matName = readMaterial();
            Material mat = compileMaterial(ind++);
            list.put(matName, mat);
        }
        return list;
    }

}
