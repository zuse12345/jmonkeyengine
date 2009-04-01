package com.g3d.renderer.queue;

import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;

/**
 * Doesn't work at the moment.
 */
public class RenderQueue {

    private Renderer renderer;

    private SpatialList opaqueList = new SpatialList(new OpaqueComparator(renderer));

    public RenderQueue(Renderer renderer){
        this.renderer = renderer;
    }

    public enum Bucket {
        Opaque,
        Transparent,
        Ortho;
    }

    public Camera getCamera(){
        return renderer.getCamera();
    }

    public void addToQueue(Geometry g, int bucket) {
//        switch (bucket) {
//
//        }
    }

    public void renderQueue(Bucket bucket){
        switch (bucket){
            case Opaque:
                opaqueList.sort();
        }
    }

}
