package com.g3d.asset;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 * An <code>AssetCache</code> allows storage of loaded resources in order
 * to improve their access time if they are requested again in a short period
 * of time. The AssetCache stores weak references to the resources, allowing
 * Java's garbage collector to request deletion of rarely used resources
 * when heap memory is low.
 */
public class AssetCache {

    private final ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
    private final Hashtable<AssetKey, Object> loadedObjects = new Hashtable<AssetKey, Object>();

    private class ContentRef extends WeakReference<Object>{
        private AssetKey key;
        public ContentRef(AssetKey key, Object obj){
            super(obj, refQueue);
            this.key = key;
        }
    }

    /**
     * Adds a resource to the cache.
     * <br/><br/>
     * <font color="red">Thread-safe.</font>
     * @see #getFromCache(java.lang.String)
     */
    public void addToCache(AssetKey key, Object obj){
        synchronized (loadedObjects){
            deleteUnused();
            new ContentRef(key, obj);
            loadedObjects.put(key, obj);
        }
    }

    /**
     * Gets an object from the cache given an asset key.
     * <br/><br/>
     * <font color="red">Thread-safe.</font>
     * @param key
     * @return
     */
    public Object getFromCache(AssetKey key){
        synchronized (loadedObjects){
            deleteUnused();
            return loadedObjects.get(key);
        }
    }

    /**
     * Removes unused references from the cache.
     */
    private void deleteUnused(){
        for (ContentRef ref = (ContentRef) refQueue.poll(); ref != null;){
            // remove from cache..
            loadedObjects.remove(ref.key);
        }
    }

    /**
     * Deletes all the assets.
     */
    public void deleteAllAssets(){
        synchronized (loadedObjects){
            loadedObjects.clear();
        }
    }
}
