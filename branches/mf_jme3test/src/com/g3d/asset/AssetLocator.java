package com.g3d.asset;

/**
 * <code>AssetLocator</code> is used to locate a resource based on an AssetKey.
 * @author Kirill
 */
public interface AssetLocator {
    public void setRootPath(String rootPath);
    public AssetInfo locate(AssetManager manager, AssetKey key);
}
