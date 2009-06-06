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
    private SpatialList guiList;
    private SpatialList transparentList;
    private SpatialList skyList;

    public RenderQueue(Renderer renderer){
        this.renderer = renderer;
        this.opaqueList =  new SpatialList(new OpaqueComparator(renderer));
        this.guiList = new SpatialList(new GuiComparator());
        this.transparentList = new SpatialList(new TransparentComparator(renderer));
        this.skyList = new SpatialList(new NullComparator());
    }

    public enum Bucket {
        Gui,
        Opaque,
        Sky,
        Transparent,
    }

    public Camera getCamera(){
        return renderer.getCamera();
    }

    public void addToQueue(Geometry g, Bucket bucket) {
        switch (bucket) {
            case Gui:
                guiList.add(g);
                break;
            case Opaque:
                opaqueList.add(g);
                break;
            case Sky:
                skyList.add(g);
                break;
            case Transparent:
                transparentList.add(g);
                break;
            default:
                throw new UnsupportedOperationException("Unknown bucket type: "+bucket);
        }
    }

    public void renderQueue(Bucket bucket){
        SpatialList list = null;
        switch (bucket){
            case Gui:
                list = guiList;
                break;
            case Opaque:
                list = opaqueList;
                break;
            case Sky:
                list = skyList;
                break;
            case Transparent:
                list = transparentList;
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
        guiList.clear();
        transparentList.clear();
        skyList.clear();
    }

}
