package com.g3d.asset;

public interface AssetLocator {
    public void setRootPath(String rootPath);
    public AssetInfo locate(AssetManager manager, AssetKey key);
}
