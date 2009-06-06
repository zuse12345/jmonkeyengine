package com.g3d.res;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 * A <code>ContentCache</code> allows storage of loaded resources in order
 * to improve their access time if they are requested again in a short period
 * of time. The ContentCache stores weak references to the resources, allowing
 * Java's garbage collector to request deletion of rarely used resources
 * when heap memory is low.
 */
public class ContentCache {

    private final ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
    private final Hashtable<String, Object> loadedObjects = new Hashtable<String, Object>();

    private class ContentRef extends WeakReference<Object>{
        private String key;
        public ContentRef(String key, Object obj){
            super(obj, refQueue);
            this.key = key;
        }
    }

    /**
     * Adds a resource to the cache.
     *
     * For resource retrieval,
     * @see #getFromCache(java.lang.String)
     */
    public void addToCache(String key, Object obj){
        synchronized (loadedObjects){
            deleteUnused();
            new ContentRef(key, obj);
            loadedObjects.put(key, obj);
        }
    }

    public Object getFromCache(String key){
        synchronized (loadedObjects){
            deleteUnused();
            return loadedObjects.get(key);
        }
    }

    private void deleteUnused(){
        for (ContentRef ref = (ContentRef) refQueue.poll(); ref != null;){
            // remove from cache..
            loadedObjects.remove(ref.key);
        }
    }

    public void deleteAllContent(){
        synchronized (loadedObjects){
            loadedObjects.clear();
        }
    }
}
