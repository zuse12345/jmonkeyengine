package com.jme3.renderer;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GLObjectManager tracks all GLObjects used by the Renderer. Using a
 * <code>ReferenceQueue</code> the <code>GLObjectManager</code> can delete
 * unused objects from GPU when their counterparts on the CPU are no longer used.
 *
 * On restart, the renderer may request the objects to be reset, thus allowing
 * the GLObjects to re-initialize with the new display context.
 */
public class GLObjectManager {

    private static final Logger logger = Logger.getLogger(GLObjectManager.class.getName());

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

        public GLObjectRef(GLObject obj, Object handleRef){
            super(handleRef, refQueue);
            assert handleRef != null;
            this.obj = obj;
        }
    }

    /**
     * Register a GLObject with the manager.
     */
    public void registerForCleanup(GLObject obj){
        GLObject objClone = obj.createDestructableClone();
        GLObjectRef ref = new GLObjectRef(objClone, obj.handleRef);
        objectList.add(objClone);
        if (logger.isLoggable(Level.FINEST))
            logger.log(Level.FINEST, "Registered: {0}", new String[]{obj.toString()});
    }

    /**
     * Deletes unused GLObjects
     */
    public void deleteUnused(Renderer r){
        for (GLObjectRef ref = (GLObjectRef) refQueue.poll(); ref != null;){
            ref.obj.deleteObject(r);
            objectList.remove(ref.obj);
            if (logger.isLoggable(Level.FINEST))
                logger.log(Level.FINEST, "Deleted: {0}", ref.obj);
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
            if (logger.isLoggable(Level.FINEST))
                logger.log(Level.FINEST, "Reset: {0}", obj);
        }
        objectList.clear();
    }

    public void printObjects(){
        System.out.println(" ------------------- ");
        System.out.println(" GL Object count: "+ objectList.size());
        for (GLObject obj : objectList){
            System.out.println(obj);
        }
    }
}
