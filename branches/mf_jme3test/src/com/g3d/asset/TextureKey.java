package com.g3d.asset;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import java.io.IOException;

public class TextureKey extends AssetKey {

    private boolean generateMips;
    private boolean flipY;
    private boolean asCube;
    private int anisotropy;

    public TextureKey(String name, boolean flipY){
        super(name);
        this.flipY = flipY;
    }

    public TextureKey(String name){
        super(name);
        this.flipY = true;
    }

    public TextureKey(){
    }

    public boolean isFlipY() {
        return flipY;
    }

    public int getAnisotropy() {
        return anisotropy;
    }

    public void setAnisotropy(int anisotropy) {
        this.anisotropy = anisotropy;
    }

    public boolean isAsCube() {
        return asCube;
    }

    public void setAsCube(boolean asCube) {
        this.asCube = asCube;
    }

    public boolean isGenerateMips() {
        return generateMips;
    }

    public void setGenerateMips(boolean generateMips) {
        this.generateMips = generateMips;
    }

    public void write(G3DExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(flipY, "flip_y", false);
        oc.write(generateMips, "generate_mips", false);
        oc.write(asCube, "as_cubemap", false);
        oc.write(anisotropy, "anisotropy", 0);
    }

    @Override
    public void read(G3DImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        flipY = ic.readBoolean("flip_y", false);
        generateMips = ic.readBoolean("generate_mips", false);
        asCube = ic.readBoolean("as_cubemap", false);
        anisotropy = ic.readInt("anisotropy", 0);
    }
}
