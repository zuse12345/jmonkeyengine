package com.g3d.renderer.queue;

import com.g3d.scene.Spatial;
import com.g3d.ui.UIElement;
import java.util.Comparator;

public class GuiComparator implements Comparator<Spatial> {

    public int compare(Spatial o1, Spatial o2) {
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

}
