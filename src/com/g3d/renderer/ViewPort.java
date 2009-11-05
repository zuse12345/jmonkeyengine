package com.g3d.renderer;

import com.g3d.renderer.queue.RenderQueue;
import com.g3d.scene.Spatial;
import com.g3d.texture.FrameBuffer;
import java.util.ArrayList;
import java.util.List;

public class ViewPort {

    protected Camera cam;
    protected RenderQueue queue = new RenderQueue();
    protected List<Spatial> sceneList = new ArrayList<Spatial>();
    protected FrameBuffer out = null;

    public ViewPort(String name, Camera cam) {
        this.cam = cam;
    }

    public FrameBuffer getOutputFrameBuffer() {
        return out;
    }

    public void setOutputFrameBuffer(FrameBuffer out) {
        this.out = out;
    }

    public Camera getCamera() {
        return cam;
    }

    public RenderQueue getQueue() {
        return queue;
    }

    public void attachScene(Spatial scene){
        sceneList.add(scene);
    }

    public List<Spatial> getScenes(){
        return sceneList;
    }

}
