package com.g3d.renderer.queue;

import com.g3d.renderer.Camera;
import com.g3d.scene.Geometry;
import java.util.Comparator;

public interface GeometryComparator extends Comparator<Geometry> {
    public void setCamera(Camera cam);
}
