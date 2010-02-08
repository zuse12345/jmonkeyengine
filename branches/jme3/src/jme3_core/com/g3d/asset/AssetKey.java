package com.g3d.asset;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import java.io.IOException;

/**
 * <code>AssetKey</code> is a key that is used to
 * look up a resource from a cache. 
 * This class should be immutable.
 */
public class AssetKey implements Savable {

    protected String name;
    protected transient String extension;

    public AssetKey(String name){
        this.name = name;
        this.extension = getExtension(name);
    }

    public AssetKey(){
    }

    protected String getExtension(String name){
        int idx = name.lastIndexOf('.');
        if (idx <= 0 || idx == name.length() - 1)
            return "";
        else
            return name.substring(idx+1).toLowerCase();
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * Do any post-processing on the resource after it has been loaded.
     * @param asset
     */
    public Object postProcess(Object asset){
        return asset;
    }

    /**
     * Create an instance of the asset. Usually it's a special type of cloning.
     * @param asset
     * @return The asset, possibly cloned.
     */
    public Object createClonedInstance(Object asset){
        return asset;
    }

    /**
     * @return True if the asset for this key should be cached. Subclasses
     * should override this method if they want to override caching behavior.
     */
    public boolean shouldCache(){
        return true;
    }

    /**
     * @return Should return true, if the asset objects implement the "Asset"
     * interface and want to be removed from the cache when no longer
     * referenced in user-code.
     */
    public boolean useSmartCache(){
        return false;
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

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name, "name", null);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        name = ic.readString("name", null);
        extension = getExtension(name);
    }

}
