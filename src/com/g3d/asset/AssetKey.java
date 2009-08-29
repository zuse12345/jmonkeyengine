package com.g3d.asset;

/**
 * This class should be immutable.
 */
public class AssetKey {

    protected final String name;
    protected transient final String extension;

    public AssetKey(String name){
        this.name = name;

        int idx = name.lastIndexOf('.');
        if (idx <= 0 || idx == name.length() - 1)
            extension = "";
        else
            extension = name.substring(idx+1).toLowerCase();
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }
    
    @Override
    public boolean equals(Object other){
        if (!(other instanceof AssetKey)){
            return false;
        }
        return name.equals(((AssetKey)other).name);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public String toString(){
        return name;
    }

}
