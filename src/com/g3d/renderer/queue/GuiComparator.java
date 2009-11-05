package com.g3d.renderer.queue;

import com.g3d.renderer.Camera;
import com.g3d.scene.Geometry;
import com.g3d.ui.UIElement;

public class GuiComparator implements GeometryComparator {

    public int compare(Geometry o1, Geometry o2) {
        if (o1 instanceof UIElement && o2 instanceof UIElement){
            UIElement e1 = (UIElement) o1;
            UIElement e2 = (UIElement) o2;
            if (e1.getZOrder() > e2.getZOrder())
                return 1;
            else if (e1.getZOrder() < e2.getZOrder())
                return -1;
            else
                return 0;
        }
        return 0;
    }

    public void setCamera(Camera cam) {
    }

}
