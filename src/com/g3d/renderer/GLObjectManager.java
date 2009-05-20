package com.g3d.renderer;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * GLObjectManager tracks all GLObjects. If the display system is restarted,
 * all GLObjects are re-uploaded so that all data can be used again. If the
 * objects are no longer referenced, they will be automatically deleted
 * from the graphics library to avoid memory leaks.
 */
public class GLObjectManager {

    /**
     * The queue will receive notifications of GLObjects which are no longer
     * referenced.
     */
    private ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();

    /**
     * List of currently active GLObjects.
     */
    private List<GLObject> objectList = new ArrayList<GLObject>();

    private class GLObjectRef extends PhantomReference<Object>{
        private GLObject obj;

        public GLObjectRef(GLObject obj){
            super(obj.handleRef, refQueue);
            this.obj = obj;
        }
    }

    /**
     * Register a GLObject with the manager.
     */
    public void registerForCleanup(GLObject obj){
        new GLObjectRef(obj);
        objectList.add(obj);
        //System.out.println("Registered for cleanup: "+obj);
    }

    /**
     * Deletes unused GLObjects
     */
    public void deleteUnused(Renderer r){
        for (GLObjectRef ref = (GLObjectRef) refQueue.poll(); ref != null;){
            ref.obj.deleteObject(r);
            objectList.remove(ref.obj);
            System.out.println("Deleted: "+ref.obj);
        }
    }

    /**
     * Deletes all objects. Must only be called when display is destroyed.
     */
    public void deleteAllObjects(Renderer r){
        for (GLObject obj : objectList){
            obj.deleteObject(r);
        }
        objectList.clear();
    }

    /**
     * Resets all GLObjects.
     */
    public void resetObjects(){
        for (GLObject obj : objectList){
            obj.resetObject();
            System.out.println("Reset: "+obj);
        }
        objectList.clear();
    }
}
