package com.g3d.renderer.queue;

import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Spatial;
import java.util.Comparator;

public class OpaqueComparator implements Comparator<Spatial> {

    private Renderer renderer;

    public OpaqueComparator(Renderer renderer){
        this.renderer = renderer;
    }


    public int compare(Spatial o1, Spatial o2) {
        Camera cam = renderer.getCamera();
        return 0;


//        if (d1 == d2)
//            return 0;
//        else if (d1 < d2)
//            return 1;
//        else
//            return -1;
    }
}
