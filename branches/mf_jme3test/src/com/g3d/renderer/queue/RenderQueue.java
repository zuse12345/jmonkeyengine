package com.g3d.renderer.queue;

import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;

/**
 * Doesn't work at the moment.
 */
public class RenderQueue {

    private Renderer renderer;
    private SpatialList opaqueList;

    public RenderQueue(Renderer renderer){
        this.renderer = renderer;
        this.opaqueList =  new SpatialList(new OpaqueComparator(renderer));
    }

    public enum Bucket {
        Pre,
        Gui,
        Opaque,
        Sky,
        Transparent,
        Post;
    }

    public Camera getCamera(){
        return renderer.getCamera();
    }

    public void addToQueue(Geometry g, Bucket bucket) {
        switch (bucket) {
            case Opaque:
                opaqueList.add(g);
                break;
            default:
                throw new UnsupportedOperationException("Unknown bucket type: "+bucket);
        }
    }

    public void renderQueue(Bucket bucket){
        SpatialList list = null;
        switch (bucket){
            case Opaque:
                list = opaqueList;
                break;
        }

        if (list == null)
            throw new UnsupportedOperationException("Unknown bucket type: "+bucket);

        list.sort();
        for (int i = 0; i < list.size(); i++){
            Spatial obj = list.get(i);
            assert obj != null;

            if (obj instanceof Geometry){
                Geometry g = (Geometry) obj;
                renderer.renderGeometry(g);
            }
            // make sure to reset queue distance
            obj.queueDistance = Float.NEGATIVE_INFINITY;
        }
        list.clear();
    }

    public void clear(){
        opaqueList.clear();
    }

}
