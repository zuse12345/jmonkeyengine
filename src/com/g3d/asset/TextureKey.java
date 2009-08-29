package com.g3d.asset;

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

    public boolean isFlipY() {
        return flipY;
    }
    
}
