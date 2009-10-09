package com.g3d.asset;

import java.io.InputStream;

/**
 * Provides basic information about the asset 
 * @author Kirill
 */
public abstract class AssetInfo {

    protected AssetManager manager;
    protected AssetKey key;

    public AssetInfo(AssetManager manager, AssetKey key) {
        this.manager = manager;
        this.key = key;
    }

    public AssetKey getKey() {
        return key;
    }

    public AssetManager getManager() {
        return manager;
    }

    public String toString(){
        return getClass().getName() + "[" + "key=" + key + "]";
    }

    public abstract InputStream openStream();

}
