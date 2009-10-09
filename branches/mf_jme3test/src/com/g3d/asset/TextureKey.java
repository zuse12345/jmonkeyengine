package com.g3d.asset;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import java.io.IOException;

public class TextureKey extends AssetKey {

    private boolean flipY;

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

    public void write(G3DExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(flipY, "flip_y", false);
    }

    @Override
    public void read(G3DImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        flipY = ic.readBoolean("flip_y", false);
    }
}
