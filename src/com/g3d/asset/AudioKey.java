package com.g3d.asset;

public class AudioKey extends AssetKey {

    private boolean stream;

    public AudioKey(String name, boolean stream){
        super(name);
        this.stream = stream;
    }

    public AudioKey(String name){
        super(name);
        this.stream = false;
    }

    public boolean isStream() {
        return stream;
    }

}
