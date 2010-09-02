package com.jme3.renderer.queue;

import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;

public class GuiComparator implements GeometryComparator {

    public int compare(Geometry o1, Geometry o2) {
        float z1 = o1.getWorldTranslation().getZ();
        float z2 = o2.getWorldTranslation().getZ();
        if (z1 > z2)
            return 1;
        else if (z1 < z2)
            return -1;
        else
            return 0;
    }

    public void setCamera(Camera cam) {
    }

}
