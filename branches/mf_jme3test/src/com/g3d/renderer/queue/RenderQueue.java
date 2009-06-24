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
    private SpatialList shadowRecv;
    private SpatialList shadowCast;

    public RenderQueue(Renderer renderer){
        this.renderer = renderer;
        this.opaqueList =  new SpatialList(new OpaqueComparator(renderer));
        this.guiList = new SpatialList(new GuiComparator());
        this.transparentList = new SpatialList(new TransparentComparator(renderer));
        this.skyList = new SpatialList(new NullComparator());
        
        this.shadowRecv = new SpatialList(new OpaqueComparator(renderer));
        this.shadowCast = new SpatialList(new OpaqueComparator(renderer));
    }

    public enum Bucket {
        Gui,
        Opaque,
        Sky,
        Transparent,
        Inherit,
    }

    public enum ShadowMode {
        Off,
        Cast,
        Recieve,
        CastAndRecieve,
        Inherit
    }

    public Camera getCamera(){
        return renderer.getCamera();
    }

    public void addToShadowQueue(Geometry g, ShadowMode shadBucket){
        switch (shadBucket){
            case Inherit: break;
            case Off: break;
            case Cast:
                shadowCast.add(g);
                break;
            case Recieve:
                shadowRecv.add(g);
                break;
            case CastAndRecieve:
                shadowCast.add(g);
                shadowRecv.add(g);
                break;
            default:
                throw new UnsupportedOperationException("Unrecognized shadow bucket type: "+shadBucket);
        }
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

    public SpatialList getShadowQueueContent(ShadowMode shadBucket){
        switch (shadBucket){
            case Cast:
                return shadowCast;
            case Recieve:
                return shadowRecv;
            default:
                return null;
        }
    }

    private void renderSpatialList(SpatialList list){
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

    public void renderShadowQueue(ShadowMode shadBucket){
        switch (shadBucket){
            case Cast:
                renderSpatialList(shadowCast);
                break;
            case Recieve:
                renderSpatialList(shadowRecv);
                break;
        }
    }

    public void renderQueue(Bucket bucket){
        switch (bucket){
            case Gui:
                renderSpatialList(guiList);
                break;
            case Opaque:
                renderSpatialList(opaqueList);
                break;
            case Sky:
                renderSpatialList(skyList);
                break;
            case Transparent:
                renderSpatialList(transparentList);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported bucket type: "+bucket);
        }
    }

    public void clear(){
        opaqueList.clear();
        guiList.clear();
        transparentList.clear();
        skyList.clear();
        shadowCast.clear();
        shadowRecv.clear();
    }

}
